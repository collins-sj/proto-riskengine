package scollins.blockchain.prototype.risk.riskengine.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.EnumSet;

import org.junit.Before;
import org.junit.Test;

public class UserAccountTest {

  private UserAccount userAccount;
  private Wallet wallet = new Wallet(1);
  private EnumSet<Token> allTokens = EnumSet.allOf(Token.class);

  @Before
  public void init() {
    wallet = new Wallet(1);
    for (Token token : allTokens) {
      wallet.addQuantity(token, new BigDecimal(100));
    }
    userAccount = new UserAccount(1, wallet);
  }
  
  @Test
  public void testAddOrder() {
    Order order = new Order(1, Token.ETH);
    userAccount.addOrder(order);

    Order updatedOrder = userAccount.getOrder(order.getId());
    assertEquals(order, updatedOrder);
    assertEquals(order.getId(), updatedOrder.getId());
    assertEquals(BigDecimal.ZERO, updatedOrder.getReservedAmount());
    assertEquals(BigDecimal.ZERO, updatedOrder.getRefundedAmount());
  }

  @Test
  public void testGetAvailableBalance() {
    for (Token token : allTokens) {
      assertEquals(new BigDecimal(100), userAccount.getAvailableBalance(token));
    }
  }

  @Test
  public void testUpdateForTokenPurchase() {
    userAccount.updateForTokenPurchase(Token.BCH, new BigDecimal(50));
    assertEquals(new BigDecimal(150), userAccount.getAvailableBalance(Token.BCH));

    userAccount.updateForTokenPurchase(Token.BTC, new BigDecimal(100));
    assertEquals(new BigDecimal(200), userAccount.getAvailableBalance(Token.BTC));

    userAccount.updateForTokenPurchase(Token.ETH, new BigDecimal(150));
    assertEquals(new BigDecimal(250), userAccount.getAvailableBalance(Token.ETH));

    userAccount.updateForTokenPurchase(Token.EUR, new BigDecimal(200));
    assertEquals(new BigDecimal(300), userAccount.getAvailableBalance(Token.EUR));

    userAccount.updateForTokenPurchase(Token.USD, new BigDecimal(250));
    assertEquals(new BigDecimal(350), userAccount.getAvailableBalance(Token.USD));

  }
  
  @Test
  public void testHasSufficientBalance() {
    for (Token token : allTokens) {
      assertTrue(userAccount.hasSufficientBalance(token, BigDecimal.ZERO));
      assertTrue(userAccount.hasSufficientBalance(token, BigDecimal.ONE));
      assertTrue(userAccount.hasSufficientBalance(token, BigDecimal.TEN));
      assertTrue(userAccount.hasSufficientBalance(token, new BigDecimal(99)));
      assertTrue(userAccount.hasSufficientBalance(token, new BigDecimal(100)));
      assertFalse(userAccount.hasSufficientBalance(token, new BigDecimal(100.01)));
      assertFalse(userAccount.hasSufficientBalance(token, new BigDecimal(101)));
      assertFalse(userAccount.hasSufficientBalance(token, new BigDecimal(500.1)));
    }
  }
  
  @Test
  public void testReserveFunds() {
    Order order = new Order(1, Token.ETH);
    userAccount.addOrder(order);
    userAccount.reserveFunds(order.getId(), order.getToken(), new BigDecimal(40));
    
    Order updatedOrder = userAccount.getOrder(order.getId());
    assertEquals(order.getId(), updatedOrder.getId());
    assertEquals(new Integer(1), updatedOrder.getUserId());
    assertEquals(new BigDecimal(40), updatedOrder.getReservedAmount());
    assertEquals(BigDecimal.ZERO, updatedOrder.getRefundedAmount());
    assertEquals(OrderStatus.FUNDS_RESERVED, updatedOrder.getStatus());
    
    assertEquals(new BigDecimal(60), wallet.getAvailableBalance(Token.ETH));
  }
  
  @Test
  public void testCompleteOrder() {
    Order order = new Order(1, Token.ETH);
    userAccount.addOrder(order);
    userAccount.reserveFunds(order.getId(), order.getToken(), new BigDecimal(40));
    userAccount.completeOrder(order.getId(), order.getToken(), new BigDecimal(38));
    
    Order updatedOrder = userAccount.getOrder(order.getId());
    assertEquals(order.getId(), updatedOrder.getId());
    assertEquals(new Integer(1), updatedOrder.getUserId());
    assertEquals(BigDecimal.ZERO, updatedOrder.getReservedAmount());
    assertEquals(new BigDecimal(2), updatedOrder.getRefundedAmount());
    assertEquals(OrderStatus.COMPLETE, updatedOrder.getStatus());
    
    assertEquals(new BigDecimal(62), wallet.getAvailableBalance(Token.ETH));
  }
  
}
