package com.susie.oh.model

case class Triangle(val first: Leg, val second: Leg, val third: Leg)

object Triangle {
  
  def load(): Seq[Triangle] = {
    
    Seq(
        Triangle(Leg("BTC", "USDT"), Leg("USDT", "ETH"), Leg("ETH", "BTC")),
        Triangle(Leg("BTC", "ETH"), Leg("ETH", "USDT"), Leg("USDT", "BTC")),
        Triangle(Leg("USDT", "ETH"), Leg("ETH", "LTC"), Leg("LTC", "USDT")),
        Triangle(Leg("USDT", "BTC"), Leg("BTC", "ETH"), Leg("ETH", "USDT")),
        Triangle(Leg("USDT", "ETH"), Leg("ETH", "BTC"), Leg("BTC", "USDT")),
        Triangle(Leg("USDT", "BTC"), Leg("BTC", "LTC"), Leg("LTC", "USDT")),
        Triangle(Leg("USDT", "LTC"), Leg("LTC", "BTC"), Leg("BTC", "USDT")),
        Triangle(Leg("ETH", "BTC"), Leg("BTC", "LTC"), Leg("LTC", "ETH")),
        Triangle(Leg("ETH", "LTC"), Leg("LTC", "BTC"), Leg("BTC", "ETH")),
        Triangle(Leg("USDT", "BNB"), Leg("BNB", "ETH"), Leg("ETH", "USDT")),
        Triangle(Leg("USDT", "LTC"), Leg("LTC", "BNB"), Leg("BNB", "USDT")),
        Triangle(Leg("USDT", "BTC"), Leg("BTC", "BNB"), Leg("BNB", "USDT")),
        Triangle(Leg("USDT", "BNB"), Leg("BNB", "BTC"), Leg("BTC", "USDT")),
        Triangle(Leg("USDT", "BNB"), Leg("BNB", "LTC"), Leg("LTC", "USDT")),
        Triangle(Leg("USDT", "ETH"), Leg("ETH", "BNB"), Leg("BNB", "USDT")),
        Triangle(Leg("BTC", "BNB"), Leg("BNB", "LTC"), Leg("LTC", "BTC")),
        Triangle(Leg("LTC", "ETH"), Leg("ETH", "BNB"), Leg("BNB", "LTC")),
        Triangle(Leg("BTC", "LTC"), Leg("LTC", "BNB"), Leg("BNB", "BTC")),
        Triangle(Leg("LTC", "BNB"), Leg("BNB", "ETH"), Leg("ETH", "LTC")),
        Triangle(Leg("BTC", "ETH"), Leg("ETH", "BNB"), Leg("BNB", "BTC")),
        Triangle(Leg("BTC", "BNB"), Leg("BNB", "ETH"), Leg("ETH", "BTC")),
        
        Triangle(Leg("BTC", "BCC"), Leg("BCC", "ETH"), Leg("ETH", "BTC")),
        Triangle(Leg("BTC", "BCC"), Leg("BCC", "USDT"), Leg("USDT", "BTC")),
        Triangle(Leg("BTC", "BCC"), Leg("BCC", "BNB"), Leg("BNB", "BTC")),
        
        Triangle(Leg("ETH", "BCC"), Leg("BCC", "BNB"), Leg("BNB", "ETH")),
        Triangle(Leg("USDT", "BCC"), Leg("BCC", "BNB"), Leg("BNB", "USDT")),
        
        Triangle(Leg("BTC", "ETH"), Leg("ETH", "BCC"), Leg("BCC", "BTC")),
        Triangle(Leg("BTC", "ETH"), Leg("ETH", "XRP"), Leg("XRP", "BTC")),
        
        Triangle(Leg("BTC", "USDT"), Leg("USDT", "BCC"), Leg("BCC", "BTC")),
        Triangle(Leg("BTC", "USDT"), Leg("USDT", "XRP"), Leg("XRP", "BTC")),
        
        Triangle(Leg("BTC", "XRP"), Leg("XRP", "ETH"), Leg("ETH", "BTC")),
        Triangle(Leg("BTC", "XRP"), Leg("XRP", "USDT"), Leg("USDT", "BTC")))
    
  }
  
}