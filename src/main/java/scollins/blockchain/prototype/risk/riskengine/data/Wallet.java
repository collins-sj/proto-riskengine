package scollins.blockchain.prototype.risk.riskengine.data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class Wallet {

  private Integer userId;
  
  private Map<Token, BigDecimal> availableBalances = new HashMap<>();
  
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
    updateBalance(token, current -> current.subtract(quantity));
  }

  public void updateBalance(Token token, Function<BigDecimal, BigDecimal> f) {
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((availableBalances == null) ? 0 : availableBalances.hashCode());
    result = prime * result + ((userId == null) ? 0 : userId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Wallet other = (Wallet) obj;
    if (availableBalances == null) {
      if (other.availableBalances != null)
        return false;
    } else if (!availableBalances.equals(other.availableBalances))
      return false;
    if (userId == null) {
      if (other.userId != null)
        return false;
    } else if (!userId.equals(other.userId))
      return false;
    return true;
  }
  
}
