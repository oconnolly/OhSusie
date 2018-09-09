package com.susie.oh.model.convert

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import org.slf4j.LoggerFactory

import com.google.common.util.concurrent.AtomicDouble
import com.susie.oh.main.BidAskResponseBinance
import com.susie.oh.model.ExchangeProfile
import com.susie.oh.model.OrderBookRequest
import com.susie.oh.model.Price

import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer

class BinanceRequestConverterFactory2(override val exchangeProfile: ExchangeProfile) extends RequestConverterFactory(exchangeProfile) {
  
  private val log = LoggerFactory.getLogger(this.getClass())
  
  override def getTradeRequest(price: Price) = {
    
    val secretKey = "IsjRLPqRsL9jZhA64hAka9YpR2VTP5qPSQlMvklIbN6zlkdBFUvrV3IstxV39djg"
    
    val symbol = if(price.isSwapped) price.leg.sold + price.leg.bought else price.leg.bought + price.leg.sold
    
    val side = if(price.isSwapped) "SELL" else "BUY"
      
    val args = Map("symbol" -> symbol, "side" -> side, "type" -> "LIMIT",
        "timeInForce" -> "GTC", "recvWindow" -> 10000, "timestamp" -> System.currentTimeMillis())
    
    val argsString = args.map { case (name, value) => s"$name=$value" }.mkString("&")
    
    val hash = ExchangeProfile.getHash(argsString, secretKey)
    
    val uriWithParams = s"$argsString&signature=$hash"
    
    val httpRequest = HttpRequest(uri = uriWithParams, method = HttpMethods.POST)
    
    
    
  }
  
  override def getRequest(orderBookRequest: OrderBookRequest): HttpRequest = {
    
    val args = Map("symbol" -> (orderBookRequest.leg.bought + orderBookRequest.leg.sold))
      
    val uriWithParams = exchangeProfile.address + "?" + args.map { case (name, value) => s"$name=$value" }.mkString("&")
    
    HttpRequest(uri = uriWithParams)
    
  }
  
  override def getResponse(orderBookRequest: OrderBookRequest, httpResponse: HttpResponse)(implicit ec: ExecutionContext, mat: ActorMaterializer): Future[(Price, Price)] = {
    
    httpResponse.status match {
      
      case StatusCodes.OK => {
        
        val resRaw = Await.result(Unmarshal(httpResponse.entity).to[BidAskResponseBinance], orderBookRequest.requestFactory.exchangeProfile.timeout)
        
        val accumVolume = new AtomicDouble(0)
        
        val currencyMinimumVolume = ExchangeProfile.minimumVolume(orderBookRequest.leg.bought)

        val (lowestAsk, lowestAskVolume) = (resRaw.asks(0)(0).toDouble, resRaw.asks(0)(1).toDouble)

        val (highestBid, highestBidVolume) = (resRaw.bids(0)(0).toDouble, resRaw.bids(0)(1).toDouble)

        val firstPrice = Price(orderBookRequest.leg, orderBookRequest.requestFactory.exchangeProfile.id,
          lowestAsk * (1 + orderBookRequest.requestFactory.exchangeProfile.limitFee), lowestAskVolume)
        
        val secondVolumeConverted = highestBid * highestBidVolume
        
        val secondPrice = Price(orderBookRequest.leg.swap(), orderBookRequest.requestFactory.exchangeProfile.id,
          (1 / highestBid) * (1 + orderBookRequest.requestFactory.exchangeProfile.limitFee), secondVolumeConverted, true)
        
        Future.successful((firstPrice, secondPrice))
        
      }
      
      case _ => Future.failed(new Exception(s"Received error: ${httpResponse.status} for request: $orderBookRequest"))
      
    }
    
  }
  
}