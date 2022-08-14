package io.github.scalajos.adapters.outbound

import io.github.scalajos.business.core.Currency
import io.github.scalajos.business.spi.{CustomerRepositoryErrors, CustomerRepositorySPI}
import zio.{IO, UIO, URIO, ZIO}

final case class CustomerRecord(customerName: String, amount: BigDecimal, currency: String)

// Define a service implementation module for CustomerRepositorySPI
// This module is autonomous : it's not dependant on any other services. A singleton object is enough. 
// To make a layer (injectable module through ZIO environment), use
// ZLayer.succeed[CustomerRepositorySPI](CustomerRepositoryInMemoryAdapter) : ZLayer[Any, Nothing, CustomerRepositorySPI] <=> ULayer[CustomerRepositorySPI]

object CustomerRepositoryInMemoryAdapter extends CustomerRepositorySPI :
  private val db = scala.collection.mutable.Map[String, CustomerRecord]()

  def createCustomer(customerName: String, initialAmount: BigDecimal, currency: Currency): IO[CustomerRepositoryErrors.AlreadyExisting, Unit] =
    ZIO.logDebug("\n--------------------\nENTERING TECHNICAL OUTBOUND ADAPTER THROUGH CustomerRepositorySPI\n--------------------\nCalling CustomerRepositoryInMemoryAdapter:createCustomer\nChecking if customer already exists") *>
      ZIO.attempt(db.contains(customerName)).orDie
        .flatMap(exist =>
          if (exist) ZIO.logDebug("\nBUSINESS ERROR : customer is already existing") *> ZIO.fail(CustomerRepositoryErrors.AlreadyExisting(customerName))
          else       ZIO.logDebug("\nAdding a new customer in the repository") *> ZIO.attempt(db.put(customerName, CustomerRecord(customerName, initialAmount, currency.toString))).unit.orDie
        )

  def getAccountCurrency(customerName: String): IO[CustomerRepositoryErrors.NotFound, Currency] =
    ZIO.logDebug("\n--------------------\nENTERING TECHNICAL OUTBOUND ADAPTER THROUGH CustomerRepositorySPI\n--------------------\nCalling CustomerRepositoryInMemoryAdapter:getAccountCurrency") *>
      ZIO.fromOption(db.get(customerName)).mapBoth(
        _ => CustomerRepositoryErrors.NotFound(customerName),
        customerBalance => Currency.valueOf(customerBalance.currency)
      )

  def creditCustomerBalance(customerName: String, creditAmount: BigDecimal): IO[CustomerRepositoryErrors.NotFound, BigDecimal] =
    val customer: ZIO[Any, CustomerRepositoryErrors.NotFound, CustomerRecord] =
      ZIO.logDebug("\n--------------------\nENTERING TECHNICAL OUTBOUND ADAPTER THROUGH CustomerRepositorySPI\n--------------------\nCalling CustomerRepositoryInMemoryAdapter:creditCustomerBalance") *>
        ZIO.fromOption(db.get(customerName))
          .orElseFail(CustomerRepositoryErrors.NotFound(customerName))
      
    customer.flatMap(customerBalance => {
      ZIO.logDebug("\nUpdating customer balance")
        .as {
          val newBalance = customerBalance.amount + creditAmount
          db.put(customerName, customerBalance.copy(amount = newBalance))
          newBalance
        }
    })

  def debitCustomerBalance(customerName: String, debitAmount: BigDecimal): IO[CustomerRepositoryErrors.NotFound|CustomerRepositoryErrors.Overdraft, BigDecimal] =
    val customer: ZIO[Any, CustomerRepositoryErrors.NotFound, CustomerRecord] =
      ZIO.logDebug("\n--------------------\nENTERING TECHNICAL OUTBOUND ADAPTER THROUGH CustomerRepositorySPI\n--------------------\nCalling CustomerRepositoryInMemoryAdapter:debitCustomerBalance") *>
        ZIO.fromOption(db.get(customerName))
          .orElseFail(CustomerRepositoryErrors.NotFound(customerName))

    customer.flatMap(customerBalance => {
      if ((customerBalance.amount - debitAmount) < 0)
        ZIO.logDebug("\nBUSINESS ERROR : overdraft") *>
          ZIO.fail(CustomerRepositoryErrors.Overdraft(customerBalance.customerName, customerBalance.amount, debitAmount))
      else {
        ZIO.logDebug("\nUpdating customer balance").as {
          val newBalance = customerBalance.amount - debitAmount
          db.put(customerName, customerBalance.copy(amount = newBalance))
          newBalance
        }
      }
    })

