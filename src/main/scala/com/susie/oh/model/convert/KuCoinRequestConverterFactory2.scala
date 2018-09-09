package com.susie.oh.model.convert

import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.susie.oh.main.BidAskResponseKuCoin
import com.susie.oh.model.{ExchangeProfile, OrderBookRequest, Price}

import scala.concurrent.{Await, ExecutionContext, Future}

class KuCoinRequestConverterFactory2(override val exchangeProfile: ExchangeProfile) extends RequestConverterFactory(exchangeProfile) {

  override def getRequest(orderBookRequest: OrderBookRequest): HttpRequest = {

    val args = Map("symbol" -> s"${orderBookRequest.leg.bought}-${orderBookRequest.leg.sold}")

    val uriWithParams = exchangeProfile.address + "?" + args.map { case (name, value) => s"$name=$value" }.mkString("&")

    HttpRequest(uri = uriWithParams)

  }

  override def getResponse(request: OrderBookRequest, httpResponse: HttpResponse)(implicit ec: ExecutionContext, mat: ActorMaterializer): Future[(Price, Price)] = {

    httpResponse.status match {

      case StatusCodes.OK => {

        val resRaw = Await.result(Unmarshal(httpResponse.entity).to[BidAskResponseKuCoin], request.requestFactory.exchangeProfile.timeout)

        // System.err.println("For leg: " + request.leg + ": " + resRaw)

        val lowestAsk = resRaw.data.SELL(0)(0)
        val lowestAskVolume = resRaw.data.SELL(0)(1)

        val highestBid = resRaw.data.BUY(0)(0)
        val highestBidVolume = resRaw.data.BUY(0)(2)

        val buyPrice = Price(request.leg, request.requestFactory.exchangeProfile.id,
          lowestAsk * (1 + request.requestFactory.exchangeProfile.limitFee), lowestAskVolume)

        val sellPrice = Price(request.leg.swap(), request.requestFactory.exchangeProfile.id,
          (1 / highestBid) * (1 + request.requestFactory.exchangeProfile.limitFee), highestBidVolume, true)

        Future.successful((buyPrice, sellPrice))

      }

      case _ => Future.failed(new Exception(s"Received error: ${httpResponse.status} for request: $request"))

    }

  }

}
