package scollins.blockchain.prototype.risk.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import scollins.blockchain.prototype.risk.riskengine.data.UserAccount;
import scollins.blockchain.prototype.risk.riskengine.data.Wallet;

// TODO Turn this into a singleton
public class PrototypeDataPersistence implements DataPersistence {

  private static PrototypeDataPersistence instance = new PrototypeDataPersistence();
  
  private Map<Integer, UserAccount> dataStore = new HashMap<>();
  
  public static PrototypeDataPersistence getInstance() {
    return instance;
  }
  
  @SuppressWarnings({"unchecked", "rawtypes"})
  private PrototypeDataPersistence() {
    try (InputStream in = Resources.getResource("useraccounts.json").openStream()) {
      Reader reader = new InputStreamReader(in, "UTF-8");
      
      Gson gson = new Gson();
      LinkedTreeMap<String, LinkedTreeMap> json = gson.fromJson(reader, LinkedTreeMap.class);
      json.entrySet().stream().forEach(e -> {
        Integer userId = Integer.valueOf(e.getKey());
        dataStore.putIfAbsent(userId, 
            new UserAccount(userId, new Wallet(userId, e.getValue())));
      });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public void addOrUpdate(UserAccount userAccount) {
    dataStore.put(userAccount.getUserId(), userAccount);
  }

  @Override
  public UserAccount retrieveAccount(Integer userId) {
    return dataStore.get(userId);
  }
  
  @Override
  public void displayRecords() {
    dataStore.values().stream().forEach(System.out::println);
  }

}
