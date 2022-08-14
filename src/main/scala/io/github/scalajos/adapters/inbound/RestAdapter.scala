package io.github.scalajos.adapters.inbound

import zhttp.http.{Http, Request, Response}

trait RestAdapter:
  def endpoints: Http[Any, Nothing, Request, Response]


