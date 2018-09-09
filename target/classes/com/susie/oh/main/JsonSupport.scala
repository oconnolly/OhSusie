package com.susie.oh.main

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.json.JsValue
import spray.json.RootJsonFormat

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  
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
  
  implicit object bidAskPoloniexValueFormat extends RootJsonFormat[BidAskResponsePoloniexValue] {
    
    override def write(obj: BidAskResponsePoloniexValue) = ???
    
    override def read(value: JsValue): BidAskResponsePoloniexValue = {
      value match {
        case JsArray(elements) => {
          BidAskResponsePoloniexValue(elements(0).convertTo[String].toDouble, elements(1).convertTo[Double])
        }
        case _ => throw new Exception("Unexpected element")
      }
    }
    
  }
  
  implicit val bidAskFormat = jsonFormat2(BidAskResponse)
  implicit val bidAskFormatPoloniex = jsonFormat2(BidAskResponsePoloniex)
  
  implicit val bidAskFormatBittrexResultObj = jsonFormat2(BidAskResponseBittrexResultObj)
  implicit val bidAskFormatBittrexResult = jsonFormat2(BidAskResponseBittrexResult)
  implicit val bidAskFormatBittrex = jsonFormat1(BidAskResponseBittrex)
  
  implicit val tradeResponseFormatBinance = jsonFormat11(BinanceTradeResponse)

  implicit val bidAskFormatKuCoinResult = jsonFormat2(BidAskResponseKuCoinValue)
  implicit val bidAskFormatKuCoin = jsonFormat1(BidAskResponseKuCoin)
  
}

case class BidAskResponse(val bids: Array[Array[Double]], val asks: Array[Array[Double]])

case class BidAskResponseBinance(val bids: Array[Array[String]], val asks: Array[Array[String]])

case class BidAskResponsePoloniex(val asks: Array[BidAskResponsePoloniexValue], val bids: Array[BidAskResponsePoloniexValue])

case class BidAskResponsePoloniexValue(val price: Double, val amount: Double)

case class BidAskResponseBittrex(val result: BidAskResponseBittrexResult)

case class BidAskResponseBittrexResult(val buy: Seq[BidAskResponseBittrexResultObj], val sell: Seq[BidAskResponseBittrexResultObj])

case class BidAskResponseBittrexResultObj(val Quantity: Double, val Rate: Double)

case class BinanceTradeResponse(val symbol: String, val orderId: Long,
      val clientOrderId: String, val transactTime: Long, val price: String, val origQty: String,
          val executedQty: String, val status: String, val timeInForce: String, val `type`: String, val side: String)

case class BidAskResponseKuCoinValue(val SELL: Array[Array[Double]], val BUY: Array[Array[Double]]) {
  override def toString(): String = {
    s"${this.getClass.getSimpleName}[sell=${SELL.map(_.mkString(", ")).mkString("[", "; ", "]")}, buy=${BUY.map(_.mkString(", ")).mkString("[", "; ", "]")}]"
  }
}

case class BidAskResponseKuCoin(val data: BidAskResponseKuCoinValue)