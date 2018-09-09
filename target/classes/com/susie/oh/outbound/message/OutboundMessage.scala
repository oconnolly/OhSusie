package com.susie.oh.outbound.message

import com.susie.oh.model.Leg
import com.susie.oh.model.OrderBookRequest

abstract class OutboundMessage

case class OutboundDataMessage(val ratio: Double, val trade1: (Leg, String), val trade2: (Leg, String), val trade3: (Leg, String)) extends OutboundMessage

case class OutboundTradeMessage() extends OutboundMessage

case class OutboundExchangeLatencyMessage(val request: OrderBookRequest, val duration: Long) extends OutboundMessage
