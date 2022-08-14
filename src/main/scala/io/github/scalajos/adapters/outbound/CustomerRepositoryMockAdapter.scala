package io.github.scalajos.adapters.outbound

import io.github.scalajos.business.core.Currency
import io.github.scalajos.business.spi.CustomerRepositoryErrors
import io.github.scalajos.adapters.outbound.CustomerRepositoryInMemoryAdapter.db
import zio.{Console, IO, ZIO}

object CustomerRepositoryMockAdapter:
  def createCustomer(customerName: String, initialAmount: BigDecimal, currency: Currency): IO[CustomerRepositoryErrors.AlreadyExisting, Unit] =
    Console.printLine(s"MockCustomerRepositorySPIAdapter:createCustomer($customerName, $initialAmount, $currency)").ignore

  def getAccountCurrency(customerName: String): IO[CustomerRepositoryErrors.NotFound, Currency] =
    Console.printLine(s"MockCustomerRepositorySPIAdapter:getAccountCurrency($customerName)").ignore *> ZIO.succeed(Currency.CHF)

  def creditCustomerBalance(customerName: String, creditAmount: BigDecimal): IO[CustomerRepositoryErrors.NotFound, Unit] =
    Console.printLine(s"MockCustomerRepositorySPIAdapter:creditCustomerBalance($customerName, $creditAmount)").ignore
    
  def debitCustomerBalance(customerName: String, debitAmount: BigDecimal): IO[CustomerRepositoryErrors.NotFound|CustomerRepositoryErrors.Overdraft, Unit] =
    Console.printLine(s"MockCustomerRepositorySPIAdapter:debitCustomerBalance($customerName, $debitAmount)").ignore


