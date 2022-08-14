package io.github.scalajos.adapters.inbound

import io.github.scalajos.business.api.CustomerServiceAPI
import zhttp.service.Server
import zio.ZIO

object RestServer:
  def runtHttpServer(port: Int = 8090): ZIO[CustomerServiceRestAdapter, Throwable, Unit] =
    for {
      _      <- ZIO.logDebug("GET CustomerServiceRestAdapter ADAPTER")
      customerServiceAPI <- ZIO.service[CustomerServiceRestAdapter]
      // other endpoints ...
      allEndpoints = customerServiceAPI.endpoints // ++ otherEndpoints
      _ <- Server.start(port, allEndpoints)
    }
    yield ()
