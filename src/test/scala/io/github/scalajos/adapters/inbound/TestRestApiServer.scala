package io.github.scalajos.adapters.inbound

import io.github.scalajos.business.api.{CustomerServiceAPI, CustomerServiceAPIMock}
import zhttp.http.{Http, Method, Request, Response}
import zhttp.service.Server
import zhttp.http.{Http, Method, Request, Response}
import io.netty.buffer.{ByteBuf, ByteBufUtil}
import io.netty.util.AsciiString
import sun.nio.cs.UTF_8
import zhttp.http.*
import zhttp.service.Server
import zio.{Task, Unsafe, ZIO, ZLayer}

class TestRestApiServer extends org.scalatest.flatspec.AnyFlatSpec {
  private val runtime: zio.Runtime[Any] = zio.Runtime.default

  extension[A] (effect: ZIO[Any, ?, A])
    def unsafeRun(runtime: zio.Runtime[Any]): A = Unsafe.unsafe(runtime.unsafe.run(effect).getOrThrowFiberFailure())

  "simple test" should "trigger zhttp endpoints" in {
    import sttp.client3._
    import sttp.capabilities.zio.ZioStreams
    import sttp.client3.SttpBackend
    import sttp.client3.armeria.zio.ArmeriaZioBackend

    // https://sttp.softwaremill.com/en/latest/backends/zio.html
    val defaultBackend: Task[SttpBackend[Task, ZioStreams]] = ArmeriaZioBackend.usingDefaultClient()

    val program = for {
      backend <- defaultBackend
      response1 <- basicRequest
        .body("""{"amount": 10, "currency": "CHF"}""")
        .put(uri"http://localhost:8090/customer/123").send(backend)
      response2 <- basicRequest
        .body("""{"amount": 10, "currency": "CHF"}""")
        .post(uri"http://localhost:8090/customer/credit/123").send(backend)
    }
    yield (response1, response2)

    val res = program.unsafeRun(runtime)
    println(res._1)
    println(res._2)
  }
}
