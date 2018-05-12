package com.susie.oh.main

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.json.JsValue
import spray.json.RootJsonFormat

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  
  implicit val bidAskFormat = jsonFormat2(BidAskResponse)
  
  import spray.json._
  
  implicit object bidAskBinanceFormat extends RootJsonFormat[BidAskResponseBinance] {
    
    override def read(value: JsValue): BidAskResponseBinance = {
      
      val obj = value.toString().replace(",[]", "").parseJson.asJsObject
      
      val bids = listFormat[Array[String]].read(obj.fields("bids")).toArray
      
      val asks = listFormat[Array[String]].read(obj.fields("asks")).toArray
      
      BidAskResponseBinance(bids, asks)
      
    }
    
    override def write(obj: BidAskResponseBinance) = throw new Exception("Unsupported operation")
    
  }
  
}

case class BidAskResponse(val bids: Array[Array[Double]], val asks: Array[Array[Double]])

case class BidAskResponseBinance(val bids: Array[Array[String]], val asks: Array[Array[String]])