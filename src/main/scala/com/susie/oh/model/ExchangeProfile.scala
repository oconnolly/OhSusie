package com.susie.oh.model

import com.susie.oh.model.convert.RequestConverterFactory
import scala.concurrent.duration.FiniteDuration

case class ExchangeProfile(val id: String, val address: String, val timeout: FiniteDuration, val requestConverterFactory: RequestConverterFactory)

object ExchangeProfile {
  
  def createNull(): ExchangeProfile = ExchangeProfile(null, null, null, null)
  
}