Feature: Kafka

  Background:
    Given Disable auto removal of Testcontainers resources
    Given Kafka consumer timeout is 5000 milliseconds
    Given Kafka topic: hello

  Scenario: Start Kafka container
    Given start Kafka container

  Scenario: Send and receive Kafka message
    Given variables
      | key     | citrus:randomNumber(4) |
      | message | Hello Kafka |
    Given new Kafka connection
      | url | ${CITRUS_TESTCONTAINERS_KAFKA_LOCAL_BOOTSTRAP_SERVERS} |
    And Kafka message key: ${key}
    When send Kafka message with body and headers: ${message}
      | messageId | ${key} |
    Then expect Kafka message with body and headers: ${message}
      | messageId | ${key} |

  Scenario: Stop Kafka container
    Given stop Kafka container
