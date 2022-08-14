package io.github.scalajos.adapters.common

import io.netty.buffer.{ByteBuf, ByteBufUtil}
import io.netty.util.AsciiString
import zio.{Task, ZIO}

import java.nio.charset.{Charset, StandardCharsets}

object TaskByteBufUtil {
  StandardCharsets.UTF_8
  extension (fromBuf: Task[ByteBuf])
    def toByteArray: Task[Array[Byte]] = fromBuf.flatMap(buf => ZIO.attempt(ByteBufUtil.getBytes(buf)).ensuring(ZIO.succeed(buf.release(buf.refCnt()))))
    def toAscii: Task[AsciiString] = fromBuf.flatMap(buf => ZIO.attempt(new AsciiString(ByteBufUtil.getBytes(buf), false)).ensuring(ZIO.succeed(buf.release(buf.refCnt()))))
    def toText(charset: Charset = StandardCharsets.UTF_8): Task[String] = fromBuf.flatMap(buf => ZIO.attempt(buf.toString(charset)).ensuring(ZIO.succeed(buf.release(buf.refCnt()))))
}
