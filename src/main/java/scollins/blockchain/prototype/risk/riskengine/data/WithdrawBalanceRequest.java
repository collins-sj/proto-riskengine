package scollins.blockchain.prototype.risk.riskengine.data;

import java.math.BigDecimal;

public final class WithdrawBalanceRequest {

  private Integer userId;
  private String orderId;
  private Token token;
  private BigDecimal quantity;
  
  public WithdrawBalanceRequest(String orderId, Integer userId, Token token, BigDecimal quantity) {
    this.orderId = orderId;
    this.userId = userId;
    this.token = token;
    this.quantity = quantity;
  }

  public String getOrderId() {
    return orderId;
  }

  public Integer getUserId() {
    return userId;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public Token getToken() {
    return token;
  }
  
}
