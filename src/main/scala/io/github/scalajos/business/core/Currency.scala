package io.github.scalajos.business.core


enum Currency:
  case CHF, EUR

object CurrencyConverter:
  def convert(moneyAmount: MoneyAmount, targetCurrency: Currency): MoneyAmount =
    println(s"Convert currency $moneyAmount into $targetCurrency")
    moneyAmount match {
      case MoneyAmount(amount, currency) =>
        currency match {
          case Currency.CHF =>
            if (targetCurrency == Currency.CHF) moneyAmount
            else MoneyAmount(amount * 0.98651575, Currency.EUR)
          case Currency.EUR =>
            if (targetCurrency == Currency.EUR) moneyAmount
            else MoneyAmount(amount * 1.013684, Currency.EUR)
        }
    }