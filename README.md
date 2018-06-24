### Prototype Risk Engine

This project is an implementation of the Mercury Code Challenge, that provides a RESTful interface to a prototype Risk Engine. The Risk Engine contains functionality to perform a simulated trade of currency/token assets. 

#### Requirements
* `maven` version 3
* `java` version 8
* a running `kafka` instance

#### Up and Running
The application can be launched from the main application class

    scollins.blockchain.prototype.risk.app.App

This main App class initialises both consumers and producer for the Kafka topics, the core application components, as well as initialising the service APIs backed by `sparkjava`. 


#### Key Concepts

##### Initialisation
The application loads a file useraccounts.json from the /resources folder that contains the
data for the available wallet balances for all users across a limited set of tokens. The data is parsed into an in-memory `DataPersistence` data store, which serves as a prototype substitution for a database in this case.

When the `RiskEngine` initialises it populates a `guava` cache bounded to 300 records and uses the default LRU eviction strategy. The cache is backed/populated by the `DataPersistence` data store. 

##### Lifecycle
The trade lifecycle is as follows:

1. Create an order for a user, for a specific token asset
2. Initiate a withdrawal of funds (a reserve) for a specified amount of that asset
3. Initiate an token buy order, and a token sell order - where the sell order is a known order with reserved funds
4. Settlement of buy and sell, using the reserved amount for the sell order and refunding the difference to the user wallet  

##### Components
The core `RiskEngine` is fronted externally only by the `/order` and `/withdrawbalance` APIs. The settlement function of the `RiskEngine` is serviced by a decoupled kafka queue, with the `TradeBroker` having published settlement messages to the topic.

With the `DataPersistence` component serving as the core data store, all updates to a UserAccount are first made through this component, with the local cache record being refreshed accordingly.

Note: This is a prototype project, and as such has not been implemented with load, transaction management, or thread conflicts in mind. This is particularly the case with regard to the persistence of `UserAccount` data, and any atomic updates between the `DataPersistence` component and the local cache.


#### Service APIs

The REST service APIs are implemented using `sparkjava` running by default on `localhost:4567`.

##### Create Order
Initiates an order, generating and returning an orderId to be used in sequence.

    POST /api/riskengine/order HTTP/1.1
    Host: localhost:4567
    Content-Type: application/json
    {"userId":100,"token":"EUR"}

##### Withdraw Balance
Reserves balance for an order, for a specified token quantity.

    POST /api/riskengine/withdrawbalance HTTP/1.1
    Host: localhost:4567
    Content-Type: application/json
    {"userId":100,"orderId":"5d2c1e81","token":"EUR","quantity":100}

##### Settle Trade
Settles the trade, using the reserved funds from the specified order for sell options.

    POST /api/tradebroker/settle HTTP/1.1
    Host: localhost:4567
    Content-Type: application/json
    {"userId":100,"orderId":"5d2c1e81","tokenPurchased":"USD","quantityPurchased":15,"tokenSold":"EUR","quantitySold":90}

##### Simulate Trade
Simulates the full trade lifecycle, generating simulated order and settlement trades.

    POST /api/tradebroker/simulate HTTP/1.1
    Host: localhost:4567
    Content-Type: application/json

#### Kafka

##### Connectivity
By default the producer and consumer for the kafka topics connect to a broker listening on port `localhost:29092`.

##### Topics
By default the topic *settlements* is used here. This topic can be created as such:

    $ $KAFKA_HOME/bin/kafka-topics --zookeeper localhost:32181 --create --topic settlements --partitions 1 --replication-factor 1


