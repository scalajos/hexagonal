package io.github.scalajos.business.api

import io.github.scalajos.business.core.{MoneyAmount, Currency}
import io.github.scalajos.business.spi.CustomerRepositoryErrors
import zio.{Console, IO, ZIO}

case class CustomerServiceAPIMock() extends CustomerServiceAPI {
  def creditCustomer(customerName: String, moneyAmount: MoneyAmount): IO[CustomerRepositoryErrors.NotFound, MoneyAmount] =
    Console.printLine(s"MockCustomerServiceAPI:creditCustomer($customerName, $moneyAmount)").ignore.as(MoneyAmount(42, Currency.CHF))

  def createCustomer(customerName: String, initialMoneyAmount: MoneyAmount): IO[CustomerRepositoryErrors.AlreadyExisting, Unit] =
    Console.printLine(s"MockCustomerServiceAPI:createCustomer($customerName, $initialMoneyAmount)").ignore
}
