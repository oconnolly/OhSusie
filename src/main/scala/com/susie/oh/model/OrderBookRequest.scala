package com.susie.oh.model

case class OrderBookRequest(val sold: String, val bought: String, val exchangeProfile: ExchangeProfile)