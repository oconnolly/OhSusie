package com.susie.oh.model.convert

import scala.concurrent.Await
import com.susie.oh.model.Price
import com.susie.oh.main.JsonSupport
import com.susie.oh.model.ExchangeProfile
import akka.stream.ActorMaterializer
import com.susie.oh.model.OrderBookRequest
import com.susie.oh.main.BidAskResponseBinance
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCodes
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger
import com.google.common.util.concurrent.AtomicDouble

class BinanceRequestConverterFactory() extends RequestConverterFactory() {
  
  override def getRequest(exchangeProfile: ExchangeProfile, orderBookRequest: OrderBookRequest): HttpRequest = {
    
    val args = Map("symbol" -> (orderBookRequest.leg.bought + orderBookRequest.leg.sold))
      
    val uriWithParams = exchangeProfile.address + "?" + args.map { case (name, value) => s"$name=$value" }.mkString("&")
    
    HttpRequest(uri = uriWithParams)
    
  }
  
  override def getResponse(orderBookRequest: OrderBookRequest, httpResponse: HttpResponse)(implicit ec: ExecutionContext, mat: ActorMaterializer): Future[(Price, Price)] = {
    
    httpResponse.status match {
      
      case StatusCodes.OK => {
        
        val resRaw = Await.result(Unmarshal(httpResponse.entity).to[BidAskResponseBinance], orderBookRequest.exchangeProfile.timeout)
        
        val accumVolume = new AtomicDouble(0)
        
        val (lowestAsk, lowestAskVolume) = resRaw.asks.find { ask =>
          val volume = ask(1).toDouble
          accumVolume.addAndGet(volume)
          volume > 1
        }.map { ask =>
          (ask(0).toDouble, accumVolume.getAndSet(0))
        }.getOrElse(throw new Exception("Not enough eligible trade volumes"))
        
        val (highestBid, highestBidVolume) = resRaw.bids.find { bid =>
          val volume = bid(1).toDouble
          accumVolume.addAndGet(volume)
          volume > 1
        }.map { bid =>
          (bid(0).toDouble, accumVolume.get())
        }.getOrElse(throw new Exception("Not enough eligible trade volumes"))
        
        val firstPrice = Price(orderBookRequest.leg, orderBookRequest.exchangeProfile.id, lowestAsk * (1 + orderBookRequest.exchangeProfile.fee), lowestAskVolume)
        
        val secondVolumeConverted = highestBidVolume * highestBid
        
        val secondPrice = Price(orderBookRequest.leg.swap(), orderBookRequest.exchangeProfile.id, (1 / highestBid) * (1 + orderBookRequest.exchangeProfile.fee), secondVolumeConverted)
        
        Future.successful((firstPrice, secondPrice))
        
      }
      
      case _ => Future.failed(new Exception(s"Received error for request: ${httpResponse.status}"))
      
    }
    
  }
  
}