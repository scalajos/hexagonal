package io.github.scalajos.adapters.inbound

import io.github.scalajos.business.api.CustomerServiceAPI
import io.github.scalajos.business.core.{Currency, MoneyAmount}
import io.github.scalajos.business.spi.CustomerRepositoryErrors
import io.netty.buffer.{ByteBuf, ByteBufUtil}
import io.netty.util.AsciiString
import sun.nio.cs.UTF_8
import zhttp.http.*
import zhttp.service.Server
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
import zio.{IO, Task, UIO, ZIO}

import java.nio.charset.{Charset, StandardCharsets}
import scala.util.control.NoStackTrace

final case class JsonDecoderError(description: String) extends NoStackTrace

case class MoneyAmountDTO(amount:Int, currency: String)

// JSON ENCODING
object MoneyAmountDTO:
  given moneyAmountDTODecoder:JsonDecoder[MoneyAmountDTO] = DeriveJsonDecoder.gen[MoneyAmountDTO]
  given moneyAmountDTOEncoder:JsonEncoder[MoneyAmountDTO] = DeriveJsonEncoder.gen[MoneyAmountDTO]

final case class CustomerServiceRestAdapter(customerServiceAPI: CustomerServiceAPI) extends RestAdapter:
  import zio.json.*
  import io.github.scalajos.adapters.common.TaskByteBufUtil.*
  import MoneyAmountDTO.*

  // debit, ...

  private def credit(customerName: String, req: Request): IO[CustomerRepositoryErrors.NotFound, Response] =
    for {
      _          <- ZIO.logDebug("\n--------------------\nENTERING TECHNICAL INBOUND ADAPTER\n--------------------\nCalling CustomerServiceRestAdapter:credit\nExtracting Request Body raw data")
      // if body is invalid, it's a fault
      body       <- req.data.toByteBuf.toText().orDie
      _          <- ZIO.logDebug("\nConverting JSON body to DTO")
      // if body can not be decoded, it's a fault
      dto        <- ZIO.fromEither(body.fromJson[MoneyAmountDTO]).mapError(errorText => JsonDecoderError(errorText)).orDie
      _          <- ZIO.logDebug("\nJSON Conversion successful\nCalling BUSINESS API PORT CustomerServiceAPI:creditCustomer")
      // NotFound is a business error
      newBalance <- customerServiceAPI.creditCustomer(customerName, MoneyAmount(dto.amount, Currency.valueOf(dto.currency)))
      _          <- ZIO.logDebug("\n--------------------\nLEAVING BUSINESS CORE\n--------------------\nCustomerServiceRestAdapter:credit is successfully completed -> Status 200\n--------------------\nLEAVING TECHNICAL INBOUND ADAPTER\n--------------------")
    }
    yield
      val dto = MoneyAmountDTO(newBalance.amount.toInt, newBalance.currency.toString)
      Response.text(s"""${dto.toJson}""").setStatus(Status.Ok)

  private def create(customerName: String, req: Request): IO[CustomerRepositoryErrors.AlreadyExisting, Response] =
    for {
      // THIS LINE IS PRINTED ON CONSOLE AS EXPECTED
      _    <- zio.Console.printLine("\n--------------------\nENTERING TECHNICAL INBOUND ADAPTER\n--------------------\nCalling CustomerServiceRestAdapter:create\nExtracting Request Body raw data").ignore
      // THIS DEBUG IS SILENT WHICH IS UNEXPECTED !
      _    <- ZIO.logDebug("\n--------------------\nENTERING TECHNICAL INBOUND ADAPTER\n--------------------\nCalling CustomerServiceRestAdapter:create\nExtracting Request Body raw data")
      // if body is invalid, it's a fault
      body <- req.data.toByteBuf.toText().orDie
      _    <- ZIO.logDebug("\nConverting JSON body to DTO")
      // if body can not be decoded, it's a fault
      dto  <- ZIO.fromEither(body.fromJson[MoneyAmountDTO]).mapError(errorText => JsonDecoderError(errorText)).orDie
      _    <- ZIO.logDebug("\nJSON Conversion successful\nCalling BUSINESS API PORT CustomerServiceAPI.createCustomer")
      // AlreadyExisting is a business error
      _    <- customerServiceAPI.createCustomer(customerName, MoneyAmount(dto.amount, Currency.valueOf(dto.currency)))
      _    <- ZIO.logDebug("\n--------------------\nLEAVING BUSINESS CORE\n--------------------\nCustomerServiceRestAdapter:create is successfully completed -> Status 201\n--------------------\nLEAVING TECHNICAL INBOUND ADAPTER\n--------------------")
    }
    yield
      Response.status(Status.Created)

  /*
  val endpoints: Http[Any, Throwable, Request, Response] = Http.collectZIO[Request] {
    // curl -URI http://localhost:8090/customer/credit/123 -Method 'POST' -Body '{"amount": 10, "currency": "CHF"}'
    // curl -X POST http://localhost:8090/customer/credit/123 -H "Content-Type: application/json" -d '{"amount": 10, "currency": "CHF"}'
    case req @ Method.POST -> !! / "customer" / "credit" / customerName =>
      credit(customerName, req) // IO[CustomerRepositoryErrors.NotFound, Response]
      // handle expected business errors
        .catchAll {
          case _: CustomerRepositoryErrors.NotFound => ZIO.logDebug("\nGot Business error : NotFound -> Status 404").as(Response.text(s"Customer $customerName not found").setStatus(Status.NotFound))
          // other Business error -> Status 406
        }
      // handle unexpected technical faults
        .catchAllDefect(onUnrecoverableFault)
    // curl -URI http://localhost:8090/customer/123 -Method 'PUT' -Body '{"amount": 10, "currency": "CHF"}'
    // curl -X PUT http://localhost:8090/customer/123 -H "Content-Type: application/json" -d '{"amount": 10, "currency": "CHF"}'
    case req @ Method.PUT -> !! / "customer" / customerName  =>
      create(customerName, req) // IO[CustomerRepositoryErrors.AlreadyExisting, Response]
        // handle expected business errors
        .catchAll {
          case _: CustomerRepositoryErrors.AlreadyExisting => ZIO.logDebug("\nGot Business error : AlreadyExisting -> Status 409").as(Response.text(s"Customer $customerName already existing").setStatus(Status.Conflict))
          // other Business error -> Status 406
        }
        // handle unexpected technical faults
        .catchAllDefect(onUnrecoverableFault)
  }
  */

  val endpoints: Http[Any, Nothing, Request, Response] =
    Http.collectZIO[Request] {
      // curl -URI http://localhost:8090/customer/credit/123 -Method 'POST' -Body '{"amount": 10, "currency": "CHF"}'
      // curl -X POST http://localhost:8090/customer/credit/123 -H "Content-Type: application/json" -d '{"amount": 10, "currency": "CHF"}'
      case req@Method.POST -> !! / "customer" / "credit" / customerName =>
        credit(customerName, req) // IO[CustomerRepositoryErrors.NotFound, Response]
          // handle expected business errors
          .catchAll {
            case _: CustomerRepositoryErrors.NotFound => ZIO.logDebug("\nGot Business error : NotFound -> Status 404").as(Response.text(s"Customer $customerName not found").setStatus(Status.NotFound))
            // other Business error -> Status 406
          }

      // curl -URI http://localhost:8090/customer/123 -Method 'PUT' -Body '{"amount": 10, "currency": "CHF"}'
      // curl -X PUT http://localhost:8090/customer/123 -H "Content-Type: application/json" -d '{"amount": 10, "currency": "CHF"}'
      case req@Method.PUT -> !! / "customer" / customerName =>
        create(customerName, req) // IO[CustomerRepositoryErrors.AlreadyExisting, Response]
          // handle expected business errors
          .catchAll {
            case _: CustomerRepositoryErrors.AlreadyExisting => ZIO.logDebug("\nGot Business error : AlreadyExisting -> Status 409").as(Response.text(s"Customer $customerName already existing").setStatus(Status.Conflict))
            // other Business error -> Status 406
          }
    }
      .catchAllDefect(onUnrecoverableFault)

  private def onUnrecoverableFault(throwable: Throwable): Http[Any, Nothing, Any, Response] = throwable match {
    case decoderError: JsonDecoderError =>
      Http.fromZIO(
        ZIO.logDebug("\nGot JSON Parser Error from request analysis -> Status 400")
          .as(Response.fromHttpError(HttpError.BadRequest(decoderError.description))))
    case other: Throwable =>
      Http.fromZIO(
        ZIO.logDebug("\nGot other Technical error -> Status 500")
        .as(Response.fromHttpError(HttpError.InternalServerError(other.getMessage))))
  }

  /*
  private def onUnrecoverableFault(throwable: Throwable): UIO[Response] = throwable match {
    case decoderError: JsonDecoderError =>
      ZIO.logDebug("\nGot JSON Parser Error from request analysis -> Status 400")
      .as(Response.fromHttpError(HttpError.BadRequest(decoderError.description)))
    case other: Throwable               =>
      ZIO.logDebug("\nGot other Technical error -> Status 500")
        .as(Response.fromHttpError(HttpError.InternalServerError(other.getMessage)))
  }
  */
