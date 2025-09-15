Feature: Kafka steps

  Background:
    Given variable body is "citrus:randomString(10)"
    Given variable key is "citrus:randomString(10)"
    Given variable value is "citrus:randomString(10)"
    Given Kafka consumer timeout is 5000 milliseconds
    Given Kafka topic: hello
    Given Kafka connection
      | url         | localhost:9092 |

  Scenario: Send and receive with predefined body and headers
    Given Kafka message body: ${body}
    And Kafka message header ${key}="${value}"
    And Kafka message key: 1
    When send Kafka message
    Then verify Kafka message body: ${body}
    And verify Kafka message header ${key} is "${value}"
    And receive Kafka message

  Scenario: Send and receive body from file
    Given load Kafka message body body.json
    When send Kafka message to topic hello
    Then verify Kafka message body loaded from body.json
    And receive Kafka message on topic hello

  Scenario: Send and receive body and headers
    And Kafka message key: 2
    When send Kafka message with body and headers: ${body}
      | ${key} | ${value} |
    Then expect Kafka message with body and headers: ${body}
      | ${key} | ${value} |
