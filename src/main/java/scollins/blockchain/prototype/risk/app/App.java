package scollins.blockchain.prototype.risk.app;

import static spark.Spark.*;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.google.gson.Gson;

import scollins.blockchain.prototype.risk.broker.SettlementConsumer;
import scollins.blockchain.prototype.risk.broker.SettlementPublisher;
import scollins.blockchain.prototype.risk.broker.TradeBroker;
import scollins.blockchain.prototype.risk.riskengine.PrototypeRiskEngine;
import scollins.blockchain.prototype.risk.riskengine.RiskEngine;
import scollins.blockchain.prototype.risk.riskengine.data.Order;
import scollins.blockchain.prototype.risk.riskengine.data.OrderRequest;
import scollins.blockchain.prototype.risk.riskengine.data.SettlementMessage;
import scollins.blockchain.prototype.risk.riskengine.data.WithdrawBalanceRequest;
import scollins.blockchain.prototype.risk.riskengine.data.WithdrawalStatus;
import spark.Request;

/**
 * Prototype risk engine application.
 */
public class App {
  
  private static RiskEngine riskEngine;
  private static SettlementPublisher publisher;
  private static TradeBroker tradeBroker;
  private static Executor threadExecutor = Executors.newSingleThreadExecutor();  

  public static void main(String[] args) {
    riskEngine = PrototypeRiskEngine.getInstance();
    publisher = SettlementPublisher.getInstance();
    tradeBroker = new TradeBroker(riskEngine, publisher);
    
    threadExecutor.execute(() -> {
      new SettlementConsumer(riskEngine).consume();
    });

    serviceMappings();
  }

  private static void serviceMappings() {
    path("/api", () -> {
      before("/*", (q, a) -> System.out.println("api request receieved..."));

      path("/riskengine", () -> {
        post("/order", (req, res) -> {
          Order order = riskEngine.createOrder(fromJson(req, OrderRequest.class));
          return new Gson().toJson(order.getId());
        });

        post("/withdrawbalance", (req, res) -> {
          WithdrawalStatus status = riskEngine.withdrawBalance(fromJson(req, WithdrawBalanceRequest.class));
          return new Gson().toJson(status.toString());
        });

      });
      
      path("/tradebroker", () -> {
        post("/settle", (req, res) -> {
          tradeBroker.settlement(fromJson(req, SettlementMessage.class));
          return "OK";
        });        

        post("/simulate", (req, res) -> {
          tradeBroker.simulateTrade();
          return "OK";
        });        
      });
      
    });
    System.out.println("Service awaiting requests...");
  }

  private static <T> T fromJson(Request request, Class<T> t) {
    return new Gson().fromJson(request.body(), t);
  }
}
