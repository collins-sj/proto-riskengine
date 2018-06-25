package scollins.blockchain.prototype.risk.riskengine.data;

import java.math.BigDecimal;
import java.util.UUID;

public class Order {

  private final String orderId;
  private final Integer userId;
  private final Token token;
  private final BigDecimal reservedAmount;
  private final BigDecimal refundedAmount;
  private final OrderStatus status;
  
  public Order(Integer userId, Token token) {
    this(UUID.randomUUID().toString().substring(0, 8), 
        userId, token, BigDecimal.ZERO, BigDecimal.ZERO, OrderStatus.OPEN);
  }

  private Order(String orderId, Integer userId, Token token, BigDecimal reservedAmount, BigDecimal refundedAmount, OrderStatus status) {
    this.orderId = orderId;
    this.userId = userId;
    this.token = token;
    this.reservedAmount = reservedAmount;
    this.refundedAmount = refundedAmount;
    this.status = status;
  }

  public Order reserveFunds(BigDecimal reserveAmount) {
    return new Order(this.orderId, this.userId, this.token, 
        reserveAmount, this.refundedAmount, OrderStatus.FUNDS_RESERVED);
  }

  public Order revoke() {
    return new Order(this.orderId, this.userId, this.token, 
        this.reservedAmount, this.refundedAmount, OrderStatus.REVOKED);
  }
  
  public Order complete(BigDecimal used) {
    return new Order(this.orderId, this.userId, this.token, 
        BigDecimal.ZERO, this.reservedAmount.subtract(used), OrderStatus.COMPLETE);
  }
  
  public String getId() {
    return orderId;
  }

  public Token getToken() {
    return token;
  }

  public BigDecimal getReservedAmount() {
    return reservedAmount;
  }

  public BigDecimal getRefundedAmount() {
    return refundedAmount;
  }

  public Integer getUserId() {
    return userId;
  }

  public OrderStatus getStatus() {
    return status;
  }

  @Override
  public String toString() {
    return "Order [orderId=" + orderId + ", userId=" + userId + ", token=" + token + ", reservedAmount="
        + reservedAmount + ", refundedAmount=" + refundedAmount + ", status=" + status + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((orderId == null) ? 0 : orderId.hashCode());
    result = prime * result + ((refundedAmount == null) ? 0 : refundedAmount.hashCode());
    result = prime * result + ((reservedAmount == null) ? 0 : reservedAmount.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
    result = prime * result + ((token == null) ? 0 : token.hashCode());
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
    Order other = (Order) obj;
    if (orderId == null) {
      if (other.orderId != null)
        return false;
    } else if (!orderId.equals(other.orderId))
      return false;
    if (refundedAmount == null) {
      if (other.refundedAmount != null)
        return false;
    } else if (!refundedAmount.equals(other.refundedAmount))
      return false;
    if (reservedAmount == null) {
      if (other.reservedAmount != null)
        return false;
    } else if (!reservedAmount.equals(other.reservedAmount))
      return false;
    if (status != other.status)
      return false;
    if (token != other.token)
      return false;
    if (userId == null) {
      if (other.userId != null)
        return false;
    } else if (!userId.equals(other.userId))
      return false;
    return true;
  }
  
}
