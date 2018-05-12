package com.susie.oh.model.convert

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import com.susie.oh.main.JsonSupport
import com.susie.oh.model.ExchangeProfile
import com.susie.oh.model.OrderBookRequest
import com.susie.oh.model.Price

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.stream.ActorMaterializer

abstract class RequestConverterFactory() extends JsonSupport {
  
  def getRequest(exchangeProfile: ExchangeProfile, orderBookRequest: OrderBookRequest): HttpRequest
  
  def getResponse(exchangeProfile: ExchangeProfile, httpResponse: HttpResponse)(implicit ec: ExecutionContext, mat: ActorMaterializer): Future[(Price, Price)]
  
}