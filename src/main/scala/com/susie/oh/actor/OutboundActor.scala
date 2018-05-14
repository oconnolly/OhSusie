package com.susie.oh.actor

import com.susie.oh.outbound.OrderBookDataWriter
import com.susie.oh.outbound.TradeDataWriter
import com.susie.oh.outbound.message.OutboundDataMessage
import com.susie.oh.outbound.message.OutboundTradeMessage

import akka.actor.Actor
import akka.actor.ActorLogging

class OutboundActor extends Actor with ActorLogging {
  
  val orderBookFileWriter = new OrderBookDataWriter("orderbook")
  
  val tradesFileWriter = new TradeDataWriter("trades")
  
  val statisticsFileWriter = new OrderBookDataWriter("stats")
  
  override def receive = {
    
    case m: OutboundDataMessage => orderBookFileWriter.write(m)
    
    case m: OutboundTradeMessage => tradesFileWriter.write(m)
    
  }
  
}
