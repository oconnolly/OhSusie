package com.susie.oh.outbound.message

import com.susie.oh.model.{Leg, OrderBookRequest}

abstract class OutboundMessage

case class OutboundDataMessage(val ratio: Double, val trades: Seq[(Leg, String)]) extends OutboundMessage

case class OutboundTradeMessage() extends OutboundMessage

case class OutboundExchangeLatencyMessage(val request: OrderBookRequest, val duration: Long) extends OutboundMessage
