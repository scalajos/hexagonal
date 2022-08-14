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
// 1) default logger is not removed
//    we see that ZIO.logDebug in MainLive generates too logs, one for the default, one for slf4j/logback
// 2) some ZIO.logDebug are silent (why ?)
//    see class io.github.scalajos.adapters.inbound.CustomerServiceRestAdapter (method create() for instance)
//    ZIO.logDebug don't log anything, they should. IO is triggered as printLine shows
//    in at least one other class (io.github.scalajos.adapters.inbound.RestServer), ZIO.logDebug is working

object MainLive:
  private val runtime: zio.Runtime[Any] = zio.Runtime.default
  private val slf4jlogger: ZLayer[Any, Nothing, Unit] = zio.Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  // SET GIVEN DEPENDENCIES
  private val loggingBehavior: ZLayer[Any, Nothing, Unit] = SLF4J.slf4j(
    format = zio.logging.LogFormat.colored
  )

  private val dependencies: ZLayer[Any, Nothing, CustomerServiceRestAdapter] = ZLayer.make[CustomerServiceRestAdapter](
    slf4jlogger,
    loggingBehavior,

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

    program.provideLayer(dependencies)
           .unsafeRun(runtime)

/*
Unsafe.unsafe(Runtime.default.unsafe.run(logic))
Runtime.unsafeRun  is Runtime.unsafe.run(x).getOrThrowFiberFailure()

//Unsafe.unsafe(runtime.unsafe.run(ZIO.attempt(println("AAA"))))

In Scala 2, you have to mark your code as unsafe like this:

import zio.Unsafe.unsafe

unsafe { implicit u =>
  ...
}



or use one of the new constructors in ZIO like ZIO.succeedUnsafe { implicit u => ... }

In Scala 3 you don't need to write the implicit u part:

import zio.Unsafe.unsafe

unsafe {
   ...
}



and in Scala 3 the usual constructors like ZIO.succeed, ZIO.attempt etc are also providing you the implicit scope
------

Unsafe.unsafe { implicit unsafe =>
  runtime.unsafe.run(...)
}


You can do something like:

      implicit class RunSyntax[A](io: ZIO[Any, Any, A]) {
        def unsafeRun: A =
          Unsafe.unsafeCompat { implicit u =>
            Runtime.default.unsafe.run(io).getOrThrowFiberFailure()
          }
      }
*/