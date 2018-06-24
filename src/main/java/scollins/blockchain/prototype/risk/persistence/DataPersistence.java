package scollins.blockchain.prototype.risk.persistence;

import scollins.blockchain.prototype.risk.riskengine.data.UserAccount;

public interface DataPersistence {

  public UserAccount retrieveAccount(Integer userId);

  public void addOrUpdate(UserAccount userAccount);

  public void displayRecords();
  
}
