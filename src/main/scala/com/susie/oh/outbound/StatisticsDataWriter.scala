package com.susie.oh.outbound

import com.susie.oh.outbound.message.OutboundMessage
import com.susie.oh.outbound.message.OutboundExchangeLatencyMessage

class StatisticsDataWriter() extends DataFileWriter("stats") {
  
  override protected def doWrite(data: OutboundMessage): Unit = {
    
    val message = data.asInstanceOf[OutboundExchangeLatencyMessage]
    
    val trade = message.request.leg.sold + "/" + message.request.leg.bought
    bufferedWriter.write(trade)
    bufferedWriter.write(',')
    bufferedWriter.write(message.request.requestFactory.exchangeProfile.id)
    bufferedWriter.write(',')
    bufferedWriter.write(message.duration.toString())
    bufferedWriter.write('\n')
    
  }
  
  override protected def getHeaders(): Seq[String] = {
    Seq("Trade", "Exchange", "Duration (milliseconds)")
  }
  
}