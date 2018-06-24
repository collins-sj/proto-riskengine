package scollins.blockchain.prototype.risk.broker;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import scollins.blockchain.prototype.risk.riskengine.PrototypeRiskEngine;
import scollins.blockchain.prototype.risk.riskengine.RiskEngine;
import scollins.blockchain.prototype.risk.riskengine.data.SettlementMessage;

public class SettlementConsumer {

  private static final String TOPIC_NAME = "settlements"; 
  
  private RiskEngine riskEngine;
  private KafkaConsumer<String, String> consumer;
  private static Properties kafkaProps = new Properties();
  
  static {
    try {
      kafkaProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
      kafkaProps.put("value.deserializer", "scollins.blockchain.prototype.utils.SettlementDeserializer");
      kafkaProps.put("client.id", InetAddress.getLocalHost().getHostName());
      kafkaProps.put("group.id", "tradebroker");
      kafkaProps.put("bootstrap.servers", "192.168.1.91:29092");
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
  }
  
  public SettlementConsumer(RiskEngine riskEngine) {
    this.riskEngine = riskEngine;
  }
  
  public void consume() {
    // and the consumer
    System.out.println("Polling topic " + TOPIC_NAME);
    try (KafkaConsumer<String, SettlementMessage> consumer = new KafkaConsumer<>(kafkaProps))  {
        consumer.subscribe(Arrays.asList(TOPIC_NAME), createListener());
        
        while (true) {
          ConsumerRecords<String, SettlementMessage> records = consumer.poll(5000L);
          records.forEach(record -> {
            System.out.println("Consuming settlement record: " + record.value());
            try {
              riskEngine.reconcileSettlement(record.value());
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          });
          consumer.commitSync();
        }
        
    } catch(Exception e) {
      throw new RuntimeException();
    } finally {
      if (consumer != null) {
        consumer.close();
      }
    }
  }
  
  ConsumerRebalanceListener createListener() {
    return new ConsumerRebalanceListener() {
      @Override
      public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        partitions.forEach(partition -> System.out.println("Partition revoked: " + partition));
      }

      @Override
      public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        partitions.forEach(partition -> System.out.println("Partition assigned: " + partition));
      }
    };
  }
  
  public static void main(String[] args) throws Exception {
    new SettlementConsumer(PrototypeRiskEngine.getInstance()).consume();
  }
}
