package io.github.scalajos.business.api

import io.github.scalajos.business.core.{Currency, MoneyAmount}
import io.github.scalajos.business.spi.CustomerRepositoryErrors
import zio.IO

// API PORT (public business interface, data and types)

trait CustomerServiceAPI :
  def creditCustomer(customerName: String, moneyAmount: MoneyAmount): IO[CustomerRepositoryErrors.NotFound, MoneyAmount]
  def createCustomer(customerName: String, initialMoneyAmount: MoneyAmount): IO[CustomerRepositoryErrors.AlreadyExisting, Unit]


