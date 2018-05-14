package com.susie.oh.outbound

import java.io.BufferedWriter
import java.io.FileWriter
import java.util.concurrent.atomic.AtomicInteger

import com.susie.oh.outbound.message.OutboundMessage

abstract class DataFileWriter(val fileType: String) {
  
  private val fileCounter = new AtomicInteger(0)
  private val lineLimit = 100000
  protected var lineCounter: Int = _
  private var filename: String = _
  protected var bufferedWriter: BufferedWriter = _
  private val comma = ","
  
  protected def doWrite(data: OutboundMessage): Unit
  
  protected def getHeaders(): Seq[String]
  
  def write(data: OutboundMessage): Unit = {
    checkBufferedWriter()
    doWrite(data)
    lineCounter += 1
    checkBufferedWriter()
  }
  
  def close(): String = {
    bufferedWriter.close()
    return filename
  }
  
  private def getNewBufferedWriter(): BufferedWriter = {
    filename = s"$fileType-${fileCounter.incrementAndGet()}.csv"
    val writer = new BufferedWriter(new FileWriter(filename))
    writer.write(getHeaders().mkString(comma))
    writer.write('\n')
    writer
  }
  
  private def checkBufferedWriter(): Unit = {
    if(bufferedWriter == null) {
      bufferedWriter = getNewBufferedWriter()
    } else if(lineCounter >= lineLimit) {
      val filename = close()
      System.err.println(s"=== Closed file with name: $filename ===") // TODO replace
      bufferedWriter = getNewBufferedWriter()
    }
  }
  
}
