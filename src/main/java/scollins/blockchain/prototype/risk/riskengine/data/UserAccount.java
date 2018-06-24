package scollins.blockchain.prototype.risk.riskengine.data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


public class UserAccount {

  private Integer userId;

  private Wallet wallet;

  private Map<String, Order> orders;

  public UserAccount(Integer userId, Wallet wallet) {
    this.userId = userId;
    this.wallet = wallet;
    orders = new HashMap<>();
  }
  
  public void addOrder(Order order) {
    orders.put(order.getId(), order);
  }

  public void updateForTokenPurchase(Token token, BigDecimal quantityPurchased) {
    wallet.addQuantity(token, quantityPurchased);
  }

  public BigDecimal getAvailableBalance(Token token) {
    return wallet.getAvailableBalance(token);
  }
  
  public boolean hasSufficientBalance(Token token, BigDecimal quantity) {
    return getAvailableBalance(token).compareTo(quantity) >= 0;
  }
  
  public void reserveFunds(String orderId, Token token, BigDecimal quantity) {
    if (!hasSufficientBalance(token, quantity)) {
      throw new RuntimeException();
    }
    
    orders.compute(orderId, (k,v) -> {
      return v.reserveFunds(quantity);
    });

    // Adjust the available balance
    wallet.removeQuantity(token, quantity);
  }

  public void completeOrder(String orderId, Token token, BigDecimal quantitySold) {
    // Complete the order
    Order completed = orders.compute(orderId, (k,v) -> {
      return v.complete(quantitySold);
    });
    
    // Refund the difference
    wallet.addQuantity(token, completed.getRefundedAmount());
  }

  public Order getOrder(String orderId) {
    return orders.get(orderId);
  }
  
  public Integer getUserId() {
    return userId;
  }

  public Wallet getWallet() {
    return wallet;
  }

  @Override
  public String toString() {
    return "UserAccount [userId=" + userId + ", wallet=" + wallet + ", orders=" + orders + "]";
  }
  
  
}
