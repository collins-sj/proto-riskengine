package scollins.blockchain.prototype.risk.broker;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import scollins.blockchain.prototype.risk.riskengine.data.SettlementMessage;

public class SettlementPublisher {

  private static final SettlementPublisher INSTANCE = new SettlementPublisher();
  
  private static Properties kafkaProps = new Properties();
  
  static {
    try {
      kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
      kafkaProps.put("value.serializer", "scollins.blockchain.prototype.utils.SettlementSerializer");
      kafkaProps.put("client.id", InetAddress.getLocalHost().getHostName());
      kafkaProps.put("group.id", "tradebroker");
      kafkaProps.put("bootstrap.servers", "localhost:29092"); //192.168.1.91
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }
  
  public static SettlementPublisher getInstance() {
    return INSTANCE; 
  }
  
  private SettlementPublisher() {
  }

  public void publishSettlement(SettlementMessage settlement) {
    // set up the producer
    String topicName = "settlements";
    try (KafkaProducer<String, SettlementMessage> producer = new KafkaProducer<>(kafkaProps))  {
      System.out.println("Publishing settlement to topic " + topicName);
      ProducerRecord<String, SettlementMessage> record = new ProducerRecord<>(topicName, settlement.getOrderId(), settlement);
      producer.send(record, new Callback() {
        @Override
        public void onCompletion(RecordMetadata metadata, Exception exception) {
          if (exception != null) {
            System.out.println("Exception: " + exception);
          }
        }
      });
      producer.flush();
    }
  }
 }
