package com.susie.oh.main

import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  
  implicit val bidAskFormat = jsonFormat2(BidAskResponse)
  
}

case class BidAskResponse(val bids: Array[Array[Double]], val asks: Array[Array[Double]])