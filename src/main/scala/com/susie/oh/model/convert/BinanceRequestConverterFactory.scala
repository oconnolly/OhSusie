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

class BinanceRequestConverterFactory() extends RequestConverterFactory() {
  
  override def getRequest(exchangeProfile: ExchangeProfile, orderBookRequest: OrderBookRequest): HttpRequest = {
    
    val args = Map("symbol" -> (orderBookRequest.bought + orderBookRequest.sold))
      
    val uriWithParams = exchangeProfile.address + "?" + args.map { case (name, value) => s"$name=$value" }.mkString("&")
    
    HttpRequest(uri = uriWithParams)
    
  }
  
  override def getResponse(exchangeProfile: ExchangeProfile, httpResponse: HttpResponse)(implicit ec: ExecutionContext, mat: ActorMaterializer): Future[(Price, Price)] = {
    
    httpResponse.status match {
      
      case StatusCodes.OK => {
        
        val resRaw = Await.result(Unmarshal(httpResponse.entity).to[BidAskResponseBinance], exchangeProfile.timeout)
        
        val lowestAsk = resRaw.asks(0)(0)
        
        val highestBid = resRaw.bids(0)(0)
        
        val firstPrice = Price(null, null, exchangeProfile.id, lowestAsk.toDouble)
        
        val secondPrice = Price(null, null, exchangeProfile.id, 1 / highestBid.toDouble)
        
        Future.successful((firstPrice, secondPrice))
        
      }
      
      case _ => Future.failed(new Exception(s"Received error for request: ${httpResponse.status}"))
      
    }
    
  }
  
}