Feature: MongoDB

  Background:
    Given Disable auto removal of Testcontainers resources

  Scenario: Start container
    Given start MongoDB container
    And log 'Started MongoDB container: ${CITRUS_TESTCONTAINERS_MONGODB_CONTAINER_NAME}'

  Scenario: Create MongoDB client
    Given New global Camel context
    Given bind to Camel registry mongoClient.groovy
    """
    com.mongodb.client.MongoClients.create("${CITRUS_TESTCONTAINERS_MONGODB_LOCAL_URL}")
    """

  Scenario: Insert collection
    Given send Camel exchange to("mongodb:mongoClient?database=example&collection=transaction&operation=save") with body
    """
    {
      "transactionid": "123456789",
      "transactiontype": "ADD",
      "sender": {
        "username": "Christina",
        "userid": "chrissy"
      },
      "currency": "USD",
      "amt": 100.0,
      "receiverid": "Franz"
    }
    """

  Scenario: Verify collection
    Given Camel exchange pattern InOut
    And Camel exchange message header CamelMongoDbOperation="count"
    When send Camel exchange to("mongodb:mongoClient?database=example&collection=transaction") with body: {"transactionid": "123456789"}
    Then receive Camel exchange from("mongodb:mongoClient?database=example&collection=transaction") with body: 1

  Scenario: Stop container
    Given stop MongoDB container
