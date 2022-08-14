package io.github.scalajos.business.core

import io.github.scalajos.business.api.CustomerServiceAPI
import io.github.scalajos.business.spi.{CustomerRepositorySPI, CustomerRepositoryErrors}
import zio.{IO, URIO, ZIO}

// Define a service implementation module for CustomerServiceAPI
// This module is not autonomous : it has a dependency on another service (CustomerRepositorySPI)
// A case class is required to model such non autonomous module, dependencies are expressed through class constructor
// Reminder: constructor are just apply(...) functions
// To make a layer (injectable module through ZIO environment), use
// ZLayer.fromFunction(CustomerService(_)) : ZLayer[CustomerRepositorySPI, Nothing, CustomerService]

final case class CustomerService(customerRepository: CustomerRepositorySPI) extends CustomerServiceAPI :
  def creditCustomer(customerName: String, moneyAmount: MoneyAmount): IO[CustomerRepositoryErrors.NotFound, MoneyAmount] =
    for {
      _               <- ZIO.logDebug("\n--------------------\nENTERING BUSINESS CORE THROUGH API PORT CustomerServiceAPI\n--------------------\nCalling CustomerService:creditCustomer\nGetting customer account currency")
      accountCurrency <- customerRepository.getAccountCurrency(customerName)
      _               <- ZIO.logDebug(s"\nCustomer account currency is retrieved: $accountCurrency\nCrediting customer account")
      newBalance      <- customerRepository.creditCustomerBalance(customerName, 
                                                                  CurrencyConverter.convert(moneyAmount, accountCurrency).amount)
      _               <- ZIO.logDebug("\nCustomerService:creditCustomer is successfully completed")
    }
    yield (moneyAmount.copy(amount = newBalance))

  def createCustomer(customerName: String, initialMoneyAmount: MoneyAmount): IO[CustomerRepositoryErrors.AlreadyExisting, Unit] =
    ZIO.logDebug("\n--------------------\nENTERING BUSINESS CORE THROUGH API PORT CustomerServiceAPI\n--------------------\nCalling CustomerService:createCustomer") *>
      customerRepository.createCustomer(customerName, initialMoneyAmount.amount, initialMoneyAmount.currency) *>
      ZIO.logDebug("\n--------------------\nLEAVING TECHNICAL OUTBOUND ADAPTER\n--------------------\nCustomerService:createCustomer is successfully completed")