package com.susie.oh.outbound

import com.susie.oh.outbound.message.OutboundDataMessage
import com.susie.oh.outbound.message.OutboundMessage

class TradeDataWriter(override val fileType: String) extends DataFileWriter(fileType) {
  
  override protected def doWrite(data: OutboundMessage) {
    
    val message = data.asInstanceOf[OutboundDataMessage]
    
    
    bufferedWriter.write('\n')
    
  }
  
  override protected def getHeaders(): Seq[String] = {
    Seq() // "Ratio", "Exchange 1", "Trade 1", "Exchange 2", "Trade 2", "Exchange 3", "Trade 3")
  }
  
}