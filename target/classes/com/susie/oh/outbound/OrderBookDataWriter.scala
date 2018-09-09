package com.susie.oh.outbound

import com.susie.oh.outbound.message.OutboundDataMessage
import com.susie.oh.outbound.message.OutboundMessage

class OrderBookDataWriter() extends DataFileWriter("orderbook") {
  
  override protected def doWrite(data: OutboundMessage) {
    
    val message = data.asInstanceOf[OutboundDataMessage]
    
    bufferedWriter.write(message.ratio.toString())
    bufferedWriter.write(',')
    bufferedWriter.write(message.trade1._2)
    bufferedWriter.write(',')
    bufferedWriter.write(message.trade1._1.sold)
    bufferedWriter.write('/')
    bufferedWriter.write(message.trade1._1.bought)
    bufferedWriter.write(',')
    bufferedWriter.write(message.trade2._2)
    bufferedWriter.write(',')
    bufferedWriter.write(message.trade2._1.sold)
    bufferedWriter.write('/')
    bufferedWriter.write(message.trade2._1.bought)
    bufferedWriter.write(',')
    bufferedWriter.write(message.trade3._2)
    bufferedWriter.write(',')
    bufferedWriter.write(message.trade3._1.sold)
    bufferedWriter.write('/')
    bufferedWriter.write(message.trade3._1.bought)
    bufferedWriter.write('\n')
    
  }
  
  override protected def getHeaders(): Seq[String] = {
    Seq("Ratio", "Exchange 1", "Trade 1", "Exchange 2", "Trade 2", "Exchange 3", "Trade 3")
  }
  
}
