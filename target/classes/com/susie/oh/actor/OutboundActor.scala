package com.susie.oh.actor

import com.susie.oh.outbound.OrderBookDataWriter
import com.susie.oh.outbound.StatisticsDataWriter
import com.susie.oh.outbound.TradeDataWriter
import com.susie.oh.outbound.message.OutboundDataMessage
import com.susie.oh.outbound.message.OutboundExchangeLatencyMessage
import com.susie.oh.outbound.message.OutboundTradeMessage

import akka.actor.Actor
import akka.actor.ActorLogging

class OutboundActor extends Actor with ActorLogging {
  
  val orderBookFileWriter = new OrderBookDataWriter()
  
  val tradesFileWriter = new TradeDataWriter()
  
  val statisticsFileWriter = new StatisticsDataWriter()
  
  override def receive = {
    
    case m: OutboundDataMessage => orderBookFileWriter.write(m)
    
    case m: OutboundTradeMessage => tradesFileWriter.write(m)
    
    case m: OutboundExchangeLatencyMessage => statisticsFileWriter.write(m)
    
  }
  
}
