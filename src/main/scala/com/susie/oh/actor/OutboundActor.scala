package com.susie.oh.actor

import akka.actor.ActorLogging
import akka.actor.Actor
import com.susie.oh.outbound.DataFileWriter
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayBuffer
import akka.actor.Cancellable
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import com.susie.oh.outbound.OutboundDataMessage

class OutboundActor extends Actor with ActorLogging {
  
  val fileCounter = new AtomicInteger(1)
  
  // TODO refactor how we're doing this
  var fileWriter = new DataFileWriter(fileCounter.getAndIncrement())
  
  val scheduledTasks = new ArrayBuffer[Cancellable]()
  
  override def preStart() {
    import context.dispatcher
    scheduledTasks.+=(context.system.scheduler.schedule(Duration(10, TimeUnit.MINUTES), Duration(10, TimeUnit.MINUTES), self, ClearFileMessage))
  }
  
  override def postStop() {
    scheduledTasks.foreach(_.cancel())
  }
  
  override def receive = {
    
    case ClearFileMessage => {
      
      val fileName = fileWriter.close()
      
      System.err.println(s"=== Cleared file with name: $fileName ===")
      
      fileWriter = new DataFileWriter(fileCounter.getAndIncrement())
      
    }
    
    case m: OutboundDataMessage => fileWriter.write(m)
    
  }
  
  case object ClearFileMessage
  
}