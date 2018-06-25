package scollins.blockchain.prototype.risk.riskengine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.cache.LoadingCache;

import scollins.blockchain.prototype.risk.persistence.DataPersistence;
import scollins.blockchain.prototype.risk.riskengine.data.Order;
import scollins.blockchain.prototype.risk.riskengine.data.OrderRequest;
import scollins.blockchain.prototype.risk.riskengine.data.OrderStatus;
import scollins.blockchain.prototype.risk.riskengine.data.SettlementMessage;
import scollins.blockchain.prototype.risk.riskengine.data.Token;
import scollins.blockchain.prototype.risk.riskengine.data.UserAccount;
import scollins.blockchain.prototype.risk.riskengine.data.WithdrawBalanceRequest;
import scollins.blockchain.prototype.risk.riskengine.data.WithdrawalStatus;

public class RiskEngineTest {

  private RiskEngine riskEngine;
  private LoadingCache<Integer, UserAccount> userAccounts;
  private DataPersistence dataPersistence;
  private UserAccount userAccount;
  
  public RiskEngineTest() {
  }

  @SuppressWarnings("unchecked")
  @Before
  public void init() throws Exception {
    userAccount = mock(UserAccount.class);
    userAccounts = mock(LoadingCache.class);
    dataPersistence = mock(DataPersistence.class);
    riskEngine = new PrototypeRiskEngine(userAccounts, dataPersistence);
    
    when(userAccounts.get(Mockito.anyInt())).thenReturn(userAccount);
  }
  
  @Test
  public void testCreateOrder() throws Exception {
    OrderRequest request = new OrderRequest(1, Token.BCH);
    Order order = riskEngine.createOrder(request);
    assertNotNull(order);
    assertEquals(request.getUserId(), order.getUserId());
    assertEquals(request.getToken(), order.getToken());
    assertEquals(OrderStatus.OPEN, order.getStatus());
    assertEquals(BigDecimal.ZERO, order.getReservedAmount());
    assertEquals(BigDecimal.ZERO, order.getRefundedAmount());
    
    verify(userAccounts).get(request.getUserId());
    verify(userAccount).addOrder(order);
  }

  @Test
  public void testWithdrawBalanceWithSufficientFunds() {
   WithdrawBalanceRequest request = new WithdrawBalanceRequest("1", 1, Token.ETH, new BigDecimal(10.25));

   WithdrawalStatus status = riskEngine.withdrawBalance(request);
   
   assertEquals(WithdrawalStatus.SUFFICIENT_BALANCE, status);
   verify(userAccount).reserveFunds(request.getOrderId(), request.getToken(), request.getQuantity());
   verify(dataPersistence).addOrUpdate(userAccount);
   verify(userAccounts).refresh(request.getUserId());
  }
  
  @Test
  public void testWithdrawBalanceWithInsufficientFunds() {
   WithdrawBalanceRequest request = new WithdrawBalanceRequest("1", 1, Token.ETH, new BigDecimal(10.25));
   Mockito.doThrow(RuntimeException.class).when(userAccount).reserveFunds(request.getOrderId(), request.getToken(), request.getQuantity());

   WithdrawalStatus status = riskEngine.withdrawBalance(request);
   
   assertEquals(WithdrawalStatus.INSUFFICIENT_BALANCE, status);
   verify(userAccount).reserveFunds(request.getOrderId(), request.getToken(), request.getQuantity());
   verify(dataPersistence, never()).addOrUpdate(userAccount);
   verify(userAccounts, never()).refresh(request.getUserId());
  }

  public void testReconcileSettlement() {
    SettlementMessage settlement = new SettlementMessage(1, "100", Token.ETH, new BigDecimal(100), Token.BTC, BigDecimal.ONE);
    riskEngine.reconcileSettlement(settlement);
    
    verify(userAccount).completeOrder(settlement.getOrderId(), settlement.getTokenSold(), settlement.getQuantitySold());
    verify(userAccount).updateForTokenPurchase(settlement.getTokenPurchased(), settlement.getQuantityPurchased());
    verify(dataPersistence).addOrUpdate(userAccount);
    verify(userAccounts).refresh(settlement.getUserId());
  }
  
  public void testQueryBalance() {
    when(userAccount.getAvailableBalance(Token.USD)).thenReturn(BigDecimal.TEN);
    
    BigDecimal balance = riskEngine.queryBalance(1, Token.USD);
    assertEquals(BigDecimal.TEN, balance);
    
    verify(userAccount).getAvailableBalance(Token.USD);
  }
  
}
