package scollins.blockchain.prototype.risk.riskengine.data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class Wallet {

  private Integer userId;
  
  private Map<Token, BigDecimal> availableBalances = new HashMap<>();
  
  public Wallet(Integer userId) {
    this.userId = userId;
  }
  
  public Wallet(Integer userId, Map<String, Double> tokenBalances) {
    this.userId = userId;
    tokenBalances.entrySet().stream().forEach(e -> {
      BigDecimal amount = BigDecimal.valueOf(e.getValue());
      availableBalances.put(Token.valueOf(e.getKey()), amount);
    });
  }

  public void addQuantity(Token token, BigDecimal quantity) {
    updateBalance(token, current -> current.add(quantity));
  }

  public void removeQuantity(Token token, BigDecimal quantity) {
    updateBalance(token, current -> { 
      BigDecimal newBalance = current.subtract(quantity);
      if (newBalance.compareTo(BigDecimal.ZERO) <= 0) {
        throw new RuntimeException();
      }
      return newBalance;
    });
  }

  private void updateBalance(Token token, Function<BigDecimal, BigDecimal> f) {
    this.availableBalances.putIfAbsent(token, BigDecimal.ZERO);
    this.availableBalances.compute(token, (k,v) -> {
      BigDecimal currentBalance = this.availableBalances.get(token);
      return f.apply(currentBalance);
    });
  }
  
  public BigDecimal getAvailableBalance(Token token) {
    return availableBalances.get(token);
  }

  public Integer getCustomerId() {
    return userId;
  }

  @Override
  public String toString() {
    return "Wallet [userId=" + userId + ", availableBalances=" + availableBalances + "]";
  }  
}
