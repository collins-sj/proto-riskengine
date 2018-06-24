package scollins.blockchain.prototype.risk.riskengine.data;

import java.math.BigDecimal;

public class SettlementMessage {

  private Integer userId;
  private String orderId;
  private Token tokenPurchased;
  private BigDecimal quantityPurchased;
  private Token tokenSold;
  private BigDecimal quantitySold;
  
  public SettlementMessage() {
  }
  
  public SettlementMessage(Integer userId, String orderId, Token tokenPurchased, BigDecimal quantityPurchased, Token tokenSold,
      BigDecimal quantitySold) {
    super();
    this.userId = userId;
    this.orderId = orderId;
    this.tokenPurchased = tokenPurchased;
    this.quantityPurchased = quantityPurchased;
    this.tokenSold = tokenSold;
    this.quantitySold = quantitySold;
  }

  public Integer getUserId() {
    return userId;
  }

  public String getOrderId() {
    return orderId;
  }

  public Token getTokenPurchased() {
    return tokenPurchased;
  }

  public BigDecimal getQuantityPurchased() {
    return quantityPurchased;
  }

  public Token getTokenSold() {
    return tokenSold;
  }

  public BigDecimal getQuantitySold() {
    return quantitySold;
  }

  @Override
  public String toString() {
    return "SettlementMessage [userId=" + userId + ", orderId=" + orderId + ", tokenPurchased=" + tokenPurchased
        + ", quantityPurchased=" + quantityPurchased + ", tokenSold=" + tokenSold + ", quantitySold=" + quantitySold
        + "]";
  }
}
