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

class BittrexRequestConverterFactory(override val exchangeProfile: ExchangeProfile) extends RequestConverterFactory(exchangeProfile) {
  
  override def getRequest(orderBookRequest: OrderBookRequest): HttpRequest = {
      
    val args = Map("market" -> (orderBookRequest.leg.sold.toUpperCase() + "-" + orderBookRequest.leg.bought.toUpperCase()), "type" -> "both")
    
    val uriWithParams = exchangeProfile.address + "/getorderbook?" + args.map { case (name, value) => s"$name=$value" }.mkString("&")
    
    HttpRequest(uri = uriWithParams)
    
  }
  
  override def getResponse(orderBookRequest: OrderBookRequest, httpResponse: HttpResponse)(implicit ec: ExecutionContext, mat: ActorMaterializer): Future[(Price, Price)] = {
    
    httpResponse.status match {
      
      case StatusCodes.OK => {
        
        val resRaw = Await.result(Unmarshal(httpResponse.entity).to[BidAskResponseBittrex], orderBookRequest.requestFactory.exchangeProfile.timeout)
        
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
        
        val firstPrice = Price(orderBookRequest.leg, orderBookRequest.requestFactory.exchangeProfile.id, lowestAsk.toDouble * (1 + orderBookRequest.requestFactory.exchangeProfile.marketFee), lowestAskVolume)
        
        val secondVolumeConverted = highestBidVolume.toDouble * highestBid.toDouble
        
        val secondPrice = Price(orderBookRequest.leg.swap(), orderBookRequest.requestFactory.exchangeProfile.id, (1 / highestBid.toDouble) * (1 + orderBookRequest.requestFactory.exchangeProfile.marketFee), secondVolumeConverted, true)
        
        Future.successful((firstPrice, secondPrice))
        
      }
      
      case _ => Future.failed(new Exception(s"Received error for request: ${httpResponse.status}"))
      
    }
    
  }
  
}