package scollins.blockchain.prototype.risk.riskengine;

import java.math.BigDecimal;

import scollins.blockchain.prototype.risk.riskengine.data.Order;
import scollins.blockchain.prototype.risk.riskengine.data.OrderRequest;
import scollins.blockchain.prototype.risk.riskengine.data.SettlementMessage;
import scollins.blockchain.prototype.risk.riskengine.data.Token;
import scollins.blockchain.prototype.risk.riskengine.data.WithdrawBalanceRequest;
import scollins.blockchain.prototype.risk.riskengine.data.WithdrawalStatus;

public interface RiskEngine {

  public Order createOrder(OrderRequest request);
  
  public WithdrawalStatus withdrawBalance(WithdrawBalanceRequest request);
  
  public void reconcileSettlement(SettlementMessage settlement);
  
  public BigDecimal queryBalance(Integer customerId, Token token);

}
