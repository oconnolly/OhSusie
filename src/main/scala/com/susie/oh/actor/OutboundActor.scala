package com.susie.oh.actor

import akka.actor.{Actor, ActorLogging}
import com.susie.oh.outbound.message.{OutboundDataMessage, OutboundExchangeLatencyMessage, OutboundTradeMessage}
import com.susie.oh.outbound.{OrderBookDataWriter, StatisticsDataWriter, TradeDataWriter}

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
