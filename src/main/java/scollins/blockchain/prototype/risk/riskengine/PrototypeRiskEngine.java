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

  // Bounded to 300 records
private static final PrototypeRiskEngine INSTANCE = new PrototypeRiskEngine(300);

  private LoadingCache<Integer, UserAccount> userAccounts;
  private DataPersistence dataPersistence;

  /* Package protected constructor used for test. */ 
  PrototypeRiskEngine(LoadingCache<Integer, UserAccount> userAccounts, DataPersistence dataPersistence) {
    this.userAccounts = userAccounts;
    this.dataPersistence = dataPersistence;
  }
  
  private PrototypeRiskEngine(long cacheSize) {
    dataPersistence = PrototypeDataPersistence.getInstance();
    userAccounts = CacheBuilder.newBuilder()
        .maximumSize(cacheSize)
        .build(new CacheLoader<Integer, UserAccount>() {
      public UserAccount load(Integer userId) throws Exception {
        return dataPersistence.retrieveAccount(userId);
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
    logDetails("Withdrawal initiated", userAccount, request.getOrderId());
    
    try {
      userAccount.reserveFunds(request.getOrderId(), request.getToken(), request.getQuantity());
    } catch (RuntimeException e) {
      logDetails("Withdrawal failed:" + WithdrawalStatus.INSUFFICIENT_BALANCE, userAccount, request.getOrderId());
      return WithdrawalStatus.INSUFFICIENT_BALANCE;
    }

    // Update the value to the data persistence layer.
    dataPersistence.addOrUpdate(userAccount);

    // Refresh the local cache record from the data persistence layer.
    userAccounts.refresh(request.getUserId());

    logDetails("Withdrawal complete", userAccount, request.getOrderId());
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
    getInstance().dataPersistence.displayRecords();
  }
}
