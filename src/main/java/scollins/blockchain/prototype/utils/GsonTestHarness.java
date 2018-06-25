package scollins.blockchain.prototype.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import scollins.blockchain.prototype.risk.riskengine.data.SettlementMessage;
import scollins.blockchain.prototype.risk.riskengine.data.Token;
import scollins.blockchain.prototype.risk.riskengine.data.WithdrawBalanceRequest;


public class GsonTestHarness {

    public static void main(String[] args) throws Exception {
      loadUsers();
      requestToJson();
    }
    
    private static void loadUsers() throws Exception {
      try (InputStream in = Resources.getResource("useraccounts.json").openStream()) {
        Reader reader = new InputStreamReader(in, "UTF-8");
        
        Gson gson = new Gson();
        @SuppressWarnings({"rawtypes", "unchecked"})
        LinkedTreeMap<String, LinkedTreeMap> json = gson.fromJson(reader, LinkedTreeMap.class);
        json.entrySet().stream().forEach(e -> {
          System.out.println(e.getValue().get("BTC")); 
        });
        
        
        System.out.println(json.getClass());
        System.out.println(json.toString());
      } finally {
        
      }
    }
    
    private static void requestToJson() throws Exception {
      WithdrawBalanceRequest request = new WithdrawBalanceRequest(
          "1", 100, Token.EUR, BigDecimal.valueOf(100));

      System.out.println(new Gson().toJson(request));
      
      SettlementMessage settlement = new SettlementMessage(100, "ADBSD", 
          Token.USD, BigDecimal.valueOf(15), 
          Token.EUR, BigDecimal.valueOf(90));
      System.out.println(new Gson().toJson(settlement));
    }
}