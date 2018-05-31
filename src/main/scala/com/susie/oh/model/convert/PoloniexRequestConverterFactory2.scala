package com.susie.oh.model.convert

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import com.susie.oh.main.BidAskResponsePoloniex
import com.susie.oh.model.ExchangeProfile
import com.susie.oh.model.OrderBookRequest
import com.susie.oh.model.Price

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer

class PoloniexRequestConverterFactory2(override val exchangeProfile: ExchangeProfile) extends RequestConverterFactory(exchangeProfile) {
  
  override def getRequest(orderBookRequest: OrderBookRequest): HttpRequest = {
    
    val args = Map("command" -> "returnOrderBook", "currencyPair" -> (orderBookRequest.leg.sold + "_" + orderBookRequest.leg.bought), "depth" -> "1")
    
    val uriWithParams = exchangeProfile.address + "?" + args.map { case (name, value) => s"$name=$value" }.mkString("&")
    
    HttpRequest(uri = uriWithParams)
    
  }
  
  override def getResponse(orderBookRequest: OrderBookRequest, httpResponse: HttpResponse)(implicit ec: ExecutionContext, mat: ActorMaterializer): Future[(Price, Price)] = {
    
    httpResponse.status match {
      
      case StatusCodes.OK => {
        
        val resRaw = Await.result(Unmarshal(httpResponse.entity).to[BidAskResponsePoloniex], orderBookRequest.requestFactory.exchangeProfile.timeout)
        
        val lowestAsk = resRaw.asks(0).price
        val lowestAskVolume = resRaw.asks(0).amount
        
        val highestBid = resRaw.bids(0).price
        val highestBidVolume = resRaw.bids(0).amount
        
        val firstPrice = Price(orderBookRequest.leg, orderBookRequest.requestFactory.exchangeProfile.id, highestBid.toDouble * (1 + orderBookRequest.requestFactory.exchangeProfile.limitFee), highestBidVolume)
        
        val secondPrice = Price(orderBookRequest.leg.swap(), orderBookRequest.requestFactory.exchangeProfile.id, (1 / lowestAsk.toDouble) * (1 + orderBookRequest.requestFactory.exchangeProfile.limitFee), lowestAskVolume, true)
        
        System.err.println("GOT POLONIEX PRICE: " + firstPrice + " " + secondPrice)
        
        Future.successful((firstPrice, secondPrice))
        
      }
      
      case _ => Future.failed(new Exception(s"Received error for request: ${httpResponse.status}"))
      
    }
    
  }
  
}