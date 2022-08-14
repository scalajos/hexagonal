package io.github.scalajos.business.spi

import io.github.scalajos.business.core.Currency
import zio.IO

enum CustomerRepositoryErrors:
  case AlreadyExisting(customerName: String) extends CustomerRepositoryErrors
  case NotFound(customerName: String) extends CustomerRepositoryErrors
  case Overdraft(customerName: String, currentAmount: BigDecimal, debitAmount: BigDecimal) extends CustomerRepositoryErrors

trait CustomerRepositorySPI :
  def createCustomer(customerName: String, initialAmount: BigDecimal, currency: Currency): IO[CustomerRepositoryErrors.AlreadyExisting, Unit]
  def getAccountCurrency(customerName: String): IO[CustomerRepositoryErrors.NotFound, Currency]
  def creditCustomerBalance(customerName: String, amount: BigDecimal): IO[CustomerRepositoryErrors.NotFound, BigDecimal]
  def debitCustomerBalance(customerName: String, amount: BigDecimal): IO[CustomerRepositoryErrors.NotFound|CustomerRepositoryErrors.Overdraft, BigDecimal]

