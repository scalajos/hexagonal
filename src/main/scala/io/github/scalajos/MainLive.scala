package io.github.scalajos

import io.github.scalajos.business.api.{CustomerServiceAPI, CustomerServiceAPIMock}
import io.github.scalajos.business.core.CustomerService
import io.github.scalajos.business.spi.CustomerRepositorySPI
import io.github.scalajos.adapters.inbound.{CustomerServiceRestAdapter, RestServer}
import io.github.scalajos.adapters.outbound.CustomerRepositoryInMemoryAdapter
import zio.{Exit, FiberRef, Layer, LogLevel, Runtime, URIO, Unsafe, ZEnvironment, ZIO, ZLayer}
import org.slf4j.LoggerFactory
import zio.logging.backend.SLF4J
import zio.config.*
import zio.config.ConfigDescriptor.*
import zio.config.ReadError

import java.net.URL

extension [A](effect: ZIO[Any, ?, A])
  def unsafeRun(runtime: zio.Runtime[Any]): A = Unsafe.unsafe(runtime.unsafe.run(effect).getOrThrowFiberFailure())

// ------------------------------------------------------------------------------------------------
// ISSUES :
// (ubuntu 22.04, openjdk 17.0.3)
// 1) some ZIO.logDebug are silent (why ?)
//    see class io.github.scalajos.adapters.inbound.CustomerServiceRestAdapter (method create() for instance)
//    ZIO.logDebug don't log anything, they should. IO is triggered as printLine shows
//    in at least one other class (io.github.scalajos.adapters.inbound.RestServer), ZIO.logDebug is working
//
// 0) fixed redundant logger, layer SLF4J.slf4j(format = zio.logging.LogFormat.colored) was also added in dependencies layers, I removed it

object MainLive:
  private val runtime2: Runtime.Scoped[Unit] = {
    val logger = Runtime.removeDefaultLoggers >>> SLF4J.slf4j(
      format = zio.logging.LogFormat.colored
    )
    Unsafe.unsafe {
      Runtime.unsafe.fromLayer(logger)
    }
  }

  private val dependencies: ZLayer[Any, Nothing, CustomerServiceRestAdapter] = ZLayer.make[CustomerServiceRestAdapter](
    // SPI ADAPTER : IN-MEMORY DB
    ZLayer.succeed[CustomerRepositorySPI](CustomerRepositoryInMemoryAdapter), // ZLayer[Any, Nothing, CustomerRepositorySPI] <=> ULayer[CustomerRepositorySPI]
    // ^
    // |
    // BUSINESS SERVICE
    ZLayer.fromFunction(CustomerService(_)), // ZLayer[CustomerRepositorySPI, Nothing, CustomerService]
    // ^
    // |
    // API ADAPTER
    ZLayer.fromFunction(CustomerServiceRestAdapter(_)), // ZLayer[CustomerServiceAPI, Nothing, RestCustomerServiceAPI]
  )

  // START ALL ACTIVE INBOUND ENDPOINTS (JMS, HTTP, KAFKA, ...)
  // RestAPIServer    <- RestCustomerServiceAPI <- CustomerServiceAPI
  // <adapter server>         <adapter>              <api port> (MockCustomerServiceAPI)
  @main
  def mainLive(): Unit =
    val program = for {
      _      <- ZIO.logDebug("start zhttp server on port 8090")
      _      <- RestServer.runtHttpServer(8090)
    }
    yield ()

    Unsafe.unsafe {
      runtime2.unsafe.run(program.provideLayer(dependencies)).getOrThrowFiberFailure()
    }