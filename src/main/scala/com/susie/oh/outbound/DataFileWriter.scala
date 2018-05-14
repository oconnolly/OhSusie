package com.susie.oh.outbound

import com.susie.oh.model.Triangle
import com.susie.oh.model.Leg
import java.io.BufferedWriter
import java.io.FileWriter

class DataFileWriter(num: Int) {
  
  private val filename = s"trades-$num.csv"
  
  lazy val bufferedWriter = {
    val writer = new BufferedWriter(new FileWriter(filename))
    writer.write("Ratio,Exchange 1,Trade 1,Exchange 2,Trade 2,Exchange 3,Trade 3\n")
    writer
  }
  
  def write(data: OutboundDataMessage) {
    bufferedWriter.write(data.ratio.toString())
    bufferedWriter.write(',')
    bufferedWriter.write(data.trade1._2)
    bufferedWriter.write(',')
    bufferedWriter.write(data.trade1._1.sold)
    bufferedWriter.write('/')
    bufferedWriter.write(data.trade1._1.bought)
    bufferedWriter.write(',')
    bufferedWriter.write(data.trade2._2)
    bufferedWriter.write(',')
    bufferedWriter.write(data.trade2._1.sold)
    bufferedWriter.write('/')
    bufferedWriter.write(data.trade2._1.bought)
    bufferedWriter.write(',')
    bufferedWriter.write(data.trade3._2)
    bufferedWriter.write(',')
    bufferedWriter.write(data.trade3._1.sold)
    bufferedWriter.write('/')
    bufferedWriter.write(data.trade3._1.bought)
    bufferedWriter.write('\n')
  }
  
  def close(): String = {
    bufferedWriter.close()
    return filename
  }
  
}

case class OutboundDataMessage(val ratio: Double, val trade1: (Leg, String), val trade2: (Leg, String), val trade3: (Leg, String))