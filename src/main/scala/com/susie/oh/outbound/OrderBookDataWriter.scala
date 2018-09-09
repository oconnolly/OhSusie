package com.susie.oh.outbound

import com.susie.oh.model.Leg
import com.susie.oh.outbound.message.OutboundDataMessage
import com.susie.oh.outbound.message.OutboundMessage

class OrderBookDataWriter() extends DataFileWriter("orderbook") {
  
  override protected def doWrite(data: OutboundMessage) {
    
    val message = data.asInstanceOf[OutboundDataMessage]

    def getTradeStr(trade: (Leg, String)): String = {
      new StringBuilder()
        .append(trade._2).append(',').append(trade._1.sold).append('/').append(trade._1.bought)
        .toString()
    }

    bufferedWriter.write(message.ratio.toString())
    bufferedWriter.write(',')

    val tradeListStr = message.trades.map(getTradeStr).mkString(",")

    bufferedWriter.write(tradeListStr)

    bufferedWriter.write('\n')
    
  }

  override protected def getHeaders(): Seq[String] = {
    Seq("Ratio", "Exchange 1", "Trade 1", "Exchange 2", "Trade 2",
      "Exchange 3", "Trade 3", "Exchange 4", "Trade 4", "Exchange 5", "Trade 5", "Exchange 6", "Trade 6")
  }
  
}
