package com.susie.oh.model

case class Price(val leg: Leg = null, val exchangeId: String, val price: Double, val volume: Double)