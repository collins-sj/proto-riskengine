package scollins.blockchain.prototype.risk.riskengine.data;

public class OrderRequest {

  private final Integer userId;
  private final Token token;
  
  public OrderRequest(Integer userId, Token token) {
    super();
    this.userId = userId;
    this.token = token;
  }

  public Integer getUserId() {
    return userId;
  }
  public Token getToken() {
    return token;
  }

}
