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
import com.susie.oh.main.BidAskResponseBittrex

class BittrexRequestConverterFactory() extends RequestConverterFactory() {
  
  override def getRequest(exchangeProfile: ExchangeProfile, orderBookRequest: OrderBookRequest): HttpRequest = {
      
    val args = Map("market" -> (orderBookRequest.sold.toUpperCase() + "-" + orderBookRequest.bought.toUpperCase()), "type" -> "both")
    
    val uriWithParams = exchangeProfile.address + "/getorderbook?" + args.map { case (name, value) => s"$name=$value" }.mkString("&")
    
    HttpRequest(uri = uriWithParams)
    
  }
  
  override def getResponse(exchangeProfile: ExchangeProfile, httpResponse: HttpResponse)(implicit ec: ExecutionContext, mat: ActorMaterializer): Future[(Price, Price)] = {
    
    httpResponse.status match {
      
      case StatusCodes.OK => {
        
        val resRaw = Await.result(Unmarshal(httpResponse.entity).to[BidAskResponseBittrex], exchangeProfile.timeout)
        
        val lowestAsk = resRaw.result.sell(0).Rate
        
        val highestBid = resRaw.result.buy(0).Rate
        
        val firstPrice = Price(exchangeId = exchangeProfile.id, price = lowestAsk.toDouble * (1 + exchangeProfile.fee))
        
        val secondPrice = Price(exchangeId = exchangeProfile.id, price = (1 / highestBid.toDouble) * (1 + exchangeProfile.fee))
        
        Future.successful((firstPrice, secondPrice))
        
      }
      
      case _ => Future.failed(new Exception(s"Received error for request: ${httpResponse.status}"))
      
    }
    
  }
  
}