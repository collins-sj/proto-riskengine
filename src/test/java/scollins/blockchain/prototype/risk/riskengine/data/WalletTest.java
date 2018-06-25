package scollins.blockchain.prototype.risk.riskengine.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.EnumSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class WalletTest {

  private Wallet wallet;
  private EnumSet<Token> allTokens = EnumSet.allOf(Token.class);
  
  @Before
  public void init() {
    wallet = new Wallet(1);
    for (Token token : allTokens) {
      wallet.addQuantity(token, BigDecimal.valueOf(100));
    }
  }
  
  @Test
  public void testGetAvailableBalance() {
    for (Token token : allTokens) {
      assertEquals(BigDecimal.valueOf(100), wallet.getAvailableBalance(token));
    }
  }
  
  @Test
  public void testAddQuantity() {
    wallet.addQuantity(Token.BCH, BigDecimal.valueOf(50.5));
    assertEquals(BigDecimal.valueOf(150.5), wallet.getAvailableBalance(Token.BCH));

    wallet.addQuantity(Token.BTC, BigDecimal.valueOf(100.5));
    assertEquals(BigDecimal.valueOf(200.5), wallet.getAvailableBalance(Token.BTC));

    wallet.addQuantity(Token.ETH, BigDecimal.valueOf(150.5));
    assertEquals(BigDecimal.valueOf(250.5), wallet.getAvailableBalance(Token.ETH));

    wallet.addQuantity(Token.EUR, BigDecimal.valueOf(200.5));
    assertEquals(BigDecimal.valueOf(300.5), wallet.getAvailableBalance(Token.EUR));

    wallet.addQuantity(Token.USD, BigDecimal.valueOf(250.5));
    assertEquals(BigDecimal.valueOf(350.5), wallet.getAvailableBalance(Token.USD));    
  }
  
  @Test
  public void testRemoveQuantity() {
    wallet.removeQuantity(Token.BCH, BigDecimal.valueOf(50.5));
    assertEquals(BigDecimal.valueOf(49.5), wallet.getAvailableBalance(Token.BCH));

    wallet.removeQuantity(Token.BTC, BigDecimal.valueOf(60.5));
    assertEquals(BigDecimal.valueOf(39.5), wallet.getAvailableBalance(Token.BTC));

    wallet.removeQuantity(Token.ETH, BigDecimal.valueOf(70.5));
    assertEquals(BigDecimal.valueOf(29.5), wallet.getAvailableBalance(Token.ETH));

    wallet.removeQuantity(Token.EUR, BigDecimal.valueOf(80.5));
    assertEquals(BigDecimal.valueOf(19.5), wallet.getAvailableBalance(Token.EUR));

    wallet.removeQuantity(Token.USD, BigDecimal.valueOf(90.5));
    assertEquals(BigDecimal.valueOf(9.5), wallet.getAvailableBalance(Token.USD));    
  }
  
  @Test
  public void testRemovedQuantityExceedsAvailable() {
    for (Token token : allTokens) {
      try {
        wallet.removeQuantity(token, BigDecimal.valueOf(100.5));
      } catch (Exception e) {
        assertTrue(e instanceof RuntimeException);
      }
      assertEquals(BigDecimal.valueOf(100), wallet.getAvailableBalance(token));
    }
  }
}
