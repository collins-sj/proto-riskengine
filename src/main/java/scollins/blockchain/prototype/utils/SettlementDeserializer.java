package scollins.blockchain.prototype.utils;

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import scollins.blockchain.prototype.risk.riskengine.data.SettlementMessage;

public class SettlementDeserializer implements Deserializer<SettlementMessage> {
  
  @Override public void configure(Map<String, ?> arg0, boolean arg1) {
  }
  
  @Override
  public SettlementMessage deserialize(String arg0, byte[] arg1) {
    ObjectMapper mapper = new ObjectMapper();
    SettlementMessage message = null;
    try {
      message = mapper.readValue(arg1, SettlementMessage.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return message;
  }

  @Override public void close() {
  }
  
}