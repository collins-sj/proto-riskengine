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
      wallet.addQuantity(token, BigDecimal.valueOf(100));
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
      assertEquals(BigDecimal.valueOf(100), userAccount.getAvailableBalance(token));
    }
  }

  @Test
  public void testUpdateForTokenPurchase() {
    userAccount.updateForTokenPurchase(Token.BCH, BigDecimal.valueOf(50));
    assertEquals(BigDecimal.valueOf(150), userAccount.getAvailableBalance(Token.BCH));

    userAccount.updateForTokenPurchase(Token.BTC, BigDecimal.valueOf(100));
    assertEquals(BigDecimal.valueOf(200), userAccount.getAvailableBalance(Token.BTC));

    userAccount.updateForTokenPurchase(Token.ETH, BigDecimal.valueOf(150));
    assertEquals(BigDecimal.valueOf(250), userAccount.getAvailableBalance(Token.ETH));

    userAccount.updateForTokenPurchase(Token.EUR, BigDecimal.valueOf(200));
    assertEquals(BigDecimal.valueOf(300), userAccount.getAvailableBalance(Token.EUR));

    userAccount.updateForTokenPurchase(Token.USD, BigDecimal.valueOf(250));
    assertEquals(BigDecimal.valueOf(350), userAccount.getAvailableBalance(Token.USD));

  }
  
  @Test
  public void testHasSufficientBalance() {
    for (Token token : allTokens) {
      assertTrue(userAccount.hasSufficientBalance(token, BigDecimal.ZERO));
      assertTrue(userAccount.hasSufficientBalance(token, BigDecimal.ONE));
      assertTrue(userAccount.hasSufficientBalance(token, BigDecimal.TEN));
      assertTrue(userAccount.hasSufficientBalance(token, BigDecimal.valueOf(99)));
      assertTrue(userAccount.hasSufficientBalance(token, BigDecimal.valueOf(100)));
      assertFalse(userAccount.hasSufficientBalance(token, BigDecimal.valueOf(100.01)));
      assertFalse(userAccount.hasSufficientBalance(token, BigDecimal.valueOf(101)));
      assertFalse(userAccount.hasSufficientBalance(token, BigDecimal.valueOf(500.1)));
    }
  }
  
  @Test
  public void testReserveFunds() {
    Order order = new Order(1, Token.ETH);
    userAccount.addOrder(order);
    userAccount.reserveFunds(order.getId(), order.getToken(), BigDecimal.valueOf(40));
    
    Order updatedOrder = userAccount.getOrder(order.getId());
    assertEquals(order.getId(), updatedOrder.getId());
    assertEquals(new Integer(1), updatedOrder.getUserId());
    assertEquals(BigDecimal.valueOf(40), updatedOrder.getReservedAmount());
    assertEquals(BigDecimal.ZERO, updatedOrder.getRefundedAmount());
    assertEquals(OrderStatus.FUNDS_RESERVED, updatedOrder.getStatus());
    
    // Check the updated wallet balance
    assertEquals(BigDecimal.valueOf(60), wallet.getAvailableBalance(Token.ETH));
  }
  
  @Test(expected=RuntimeException.class)
  public void testReserveFundsWithInsufficientBalance() {
    Order order = new Order(1, Token.ETH);
    userAccount.addOrder(order);
    userAccount.reserveFunds(order.getId(), order.getToken(), BigDecimal.valueOf(140));
  }
  
  @Test
  public void testCompleteOrder() {
    Order order = new Order(1, Token.ETH);
    userAccount.addOrder(order);
    userAccount.reserveFunds(order.getId(), order.getToken(), BigDecimal.valueOf(40));
    userAccount.completeOrder(order.getId(), order.getToken(), BigDecimal.valueOf(38));
    
    Order updatedOrder = userAccount.getOrder(order.getId());
    assertEquals(order.getId(), updatedOrder.getId());
    assertEquals(new Integer(1), updatedOrder.getUserId());
    assertEquals(BigDecimal.ZERO, updatedOrder.getReservedAmount());
    assertEquals(BigDecimal.valueOf(2), updatedOrder.getRefundedAmount());
    assertEquals(OrderStatus.COMPLETE, updatedOrder.getStatus());
    
    // Check the updated wallet balance
    assertEquals(BigDecimal.valueOf(62), wallet.getAvailableBalance(Token.ETH));
  }
  
}
