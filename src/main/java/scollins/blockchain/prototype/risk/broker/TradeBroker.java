package scollins.blockchain.prototype.risk.broker;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import scollins.blockchain.prototype.risk.riskengine.RiskEngine;
import scollins.blockchain.prototype.risk.riskengine.data.Order;
import scollins.blockchain.prototype.risk.riskengine.data.OrderRequest;
import scollins.blockchain.prototype.risk.riskengine.data.SettlementMessage;
import scollins.blockchain.prototype.risk.riskengine.data.Token;
import scollins.blockchain.prototype.risk.riskengine.data.WithdrawBalanceRequest;

public class TradeBroker {

  private static Properties kafkaProps = new Properties();

  private RiskEngine riskEngine;
  private SettlementPublisher settlementPublisher;
  
  static {
    try {
      kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
      kafkaProps.put("value.serializer", "scollins.blockchain.prototype.utils.SettlementSerializer");
      kafkaProps.put("client.id", InetAddress.getLocalHost().getHostName());
      kafkaProps.put("group.id", "tradebroker");
      kafkaProps.put("bootstrap.servers", "192.168.1.91:29092");
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }
  
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
        sellOrder.getId(), 100, sellOrder.getToken(), new BigDecimal(100)));
    
    settlement(createSettlement(sellOrder));
  }

  private SettlementMessage createSettlement(Order sellOrder) {
    return new SettlementMessage(100, sellOrder.getId(), 
        Token.USD, new BigDecimal(15), 
        Token.EUR, new BigDecimal(90));
  }
}