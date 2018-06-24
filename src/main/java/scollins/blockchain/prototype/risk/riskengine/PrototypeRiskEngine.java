package scollins.blockchain.prototype.risk.riskengine;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import scollins.blockchain.prototype.risk.persistence.DataPersistence;
import scollins.blockchain.prototype.risk.persistence.PrototypeDataPersistence;
import scollins.blockchain.prototype.risk.riskengine.data.Order;
import scollins.blockchain.prototype.risk.riskengine.data.OrderRequest;
import scollins.blockchain.prototype.risk.riskengine.data.SettlementMessage;
import scollins.blockchain.prototype.risk.riskengine.data.Token;
import scollins.blockchain.prototype.risk.riskengine.data.UserAccount;
import scollins.blockchain.prototype.risk.riskengine.data.WithdrawBalanceRequest;
import scollins.blockchain.prototype.risk.riskengine.data.WithdrawalStatus;

public class PrototypeRiskEngine implements RiskEngine {

  private static final PrototypeRiskEngine INSTANCE = new PrototypeRiskEngine();
  // Bounded to 300 records
  private LoadingCache<Integer, UserAccount> userAccounts;
  private DataPersistence dataPersistence = PrototypeDataPersistence.getInstance();

  private PrototypeRiskEngine() {
    userAccounts = CacheBuilder.newBuilder()
        .maximumSize(300)
        .build(new CacheLoader<Integer, UserAccount>() {
      public UserAccount load(Integer customerId) throws Exception {
        return dataPersistence.retrieveAccount(customerId);
      }
    });
  }

  public static PrototypeRiskEngine getInstance() {
    return INSTANCE;
  }

  private UserAccount getUserAccount(Integer userId) {
    try {
      return userAccounts.get(userId);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  public BigDecimal queryBalance(Integer userId, Token token) {
    return getUserAccount(userId).getAvailableBalance(token);
  }

  @Override
  public Order createOrder(OrderRequest request) {
    Order order = new Order(request.getUserId(), request.getToken());
    UserAccount userAccount = getUserAccount(request.getUserId());
    userAccount.addOrder(order);
    
    logDetails("Order created", userAccount, order.getId());
    return order;
  }

  @Override
  public WithdrawalStatus withdrawBalance(WithdrawBalanceRequest request) {
    UserAccount userAccount = getUserAccount(request.getUserId());
    logDetails("Withdraw initiated", userAccount, request.getOrderId());
    
    try {
      userAccount.reserveFunds(request.getOrderId(), request.getToken(), request.getQuantity());
    } catch (RuntimeException e) {
      logDetails("Withdraw failed:" + WithdrawalStatus.INSUFFICIENT_BALANCE, userAccount, request.getOrderId());
      return WithdrawalStatus.INSUFFICIENT_BALANCE;
    }

    // Update the value to the data persistence layer.
    dataPersistence.addOrUpdate(userAccount);

    // Refresh the local cache record from the data persistence layer.
    userAccounts.refresh(request.getUserId());

    logDetails("Withdraw complete", userAccount, request.getOrderId());
    return WithdrawalStatus.SUFFICIENT_BALANCE;
  }

  public void reconcileSettlement(SettlementMessage settlement) {
    UserAccount userAccount = getUserAccount(settlement.getUserId());
    logDetails("Settlement initiated", userAccount, settlement.getOrderId());

    userAccount.completeOrder(settlement.getOrderId(), settlement.getTokenSold(), settlement.getQuantitySold());

    userAccount.updateForTokenPurchase(settlement.getTokenPurchased(), settlement.getQuantityPurchased());

    // Update the value to the data persistence layer.
    dataPersistence.addOrUpdate(userAccount);

    // Refresh the local cache record from the data persistence layer.
    userAccounts.refresh(settlement.getUserId());

    logDetails("Settlement complete", getUserAccount(settlement.getUserId()), settlement.getOrderId());
  }

  private void logDetails(String message, UserAccount userAccount, String orderId) {
    System.out.println( message + ":\n\t" 
        + userAccount.getOrder(orderId) + ",\n\t" 
        + userAccount.getWallet() + "");
  }
  
  public static void main(String[] args) throws Exception {
    new PrototypeRiskEngine().dataPersistence.displayRecords();
  }
}
