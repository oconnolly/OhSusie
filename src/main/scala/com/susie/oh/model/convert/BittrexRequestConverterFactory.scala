package com.susie.oh.model.convert

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import com.google.common.util.concurrent.AtomicDouble
import com.susie.oh.main.BidAskResponseBittrex
import com.susie.oh.model.ExchangeProfile
import com.susie.oh.model.OrderBookRequest
import com.susie.oh.model.Price

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer

class BittrexRequestConverterFactory() extends RequestConverterFactory() {
  
  override def getRequest(exchangeProfile: ExchangeProfile, orderBookRequest: OrderBookRequest): HttpRequest = {
      
    val args = Map("market" -> (orderBookRequest.leg.sold.toUpperCase() + "-" + orderBookRequest.leg.bought.toUpperCase()), "type" -> "both")
    
    val uriWithParams = exchangeProfile.address + "/getorderbook?" + args.map { case (name, value) => s"$name=$value" }.mkString("&")
    
    HttpRequest(uri = uriWithParams)
    
  }
  
  override def getResponse(orderBookRequest: OrderBookRequest, httpResponse: HttpResponse)(implicit ec: ExecutionContext, mat: ActorMaterializer): Future[(Price, Price)] = {
    
    httpResponse.status match {
      
      case StatusCodes.OK => {
        
        val resRaw = Await.result(Unmarshal(httpResponse.entity).to[BidAskResponseBittrex], orderBookRequest.exchangeProfile.timeout)
        
        val accumVolume = new AtomicDouble(0)
        
        val currencyMinimumVolume = ExchangeProfile.minimumVolume(orderBookRequest.leg.bought)
        
        val (lowestAsk, lowestAskVolume) = resRaw.result.sell.find { sell =>
          val volume = sell.Quantity
          accumVolume.addAndGet(volume)
          volume > currencyMinimumVolume
        }.map { sell => (sell.Rate, accumVolume.getAndSet(0)) }.getOrElse(throw new Exception("Not enough eligible trade volumes"))
        
        val (highestBid, highestBidVolume) = resRaw.result.buy.find { buy =>
          val volume = buy.Quantity
          accumVolume.addAndGet(volume)
          volume > currencyMinimumVolume
        }.map { buy =>
          (buy.Rate, accumVolume.get())
        }.getOrElse(throw new Exception("Not enough eligible trade volumes"))
        
        val firstPrice = Price(orderBookRequest.leg, orderBookRequest.exchangeProfile.id, lowestAsk.toDouble * (1 + orderBookRequest.exchangeProfile.fee), lowestAskVolume)
        
        val secondVolumeConverted = highestBidVolume.toDouble * highestBid.toDouble
        
        val secondPrice = Price(orderBookRequest.leg.swap(), orderBookRequest.exchangeProfile.id, (1 / highestBid.toDouble) * (1 + orderBookRequest.exchangeProfile.fee), secondVolumeConverted)
        
        Future.successful((firstPrice, secondPrice))
        
      }
      
      case _ => Future.failed(new Exception(s"Received error for request: ${httpResponse.status}"))
      
    }
    
  }
  
}