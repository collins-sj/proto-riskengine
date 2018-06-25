package scollins.blockchain.prototype.risk.broker;

import java.math.BigDecimal;

import scollins.blockchain.prototype.risk.riskengine.RiskEngine;
import scollins.blockchain.prototype.risk.riskengine.data.Order;
import scollins.blockchain.prototype.risk.riskengine.data.OrderRequest;
import scollins.blockchain.prototype.risk.riskengine.data.SettlementMessage;
import scollins.blockchain.prototype.risk.riskengine.data.Token;
import scollins.blockchain.prototype.risk.riskengine.data.WithdrawBalanceRequest;

public class TradeBroker {

  private RiskEngine riskEngine;
  private SettlementPublisher settlementPublisher;
  
  public TradeBroker(RiskEngine riskEngine, SettlementPublisher settlementPublisher) {
    this.riskEngine = riskEngine;
    this.settlementPublisher = settlementPublisher;
  }
  
  public void settlement(SettlementMessage message) {
    settlementPublisher.publishSettlement(message);
  }
  
  public void simulateTrade() {
    Order sellOrder = riskEngine.createOrder(new OrderRequest(100, Token.EUR));
    
    riskEngine.withdrawBalance(new WithdrawBalanceRequest(
        sellOrder.getId(), 100, sellOrder.getToken(), BigDecimal.valueOf(100)));
    
    settlement(createSettlement(sellOrder));
  }

  private SettlementMessage createSettlement(Order sellOrder) {
    return new SettlementMessage(100, sellOrder.getId(), 
        Token.USD, BigDecimal.valueOf(15), 
        Token.EUR, BigDecimal.valueOf(90));
  }
}