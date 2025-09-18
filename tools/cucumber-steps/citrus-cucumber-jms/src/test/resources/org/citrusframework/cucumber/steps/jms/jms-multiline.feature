Feature: JMS multiline steps

  Background:
    Given JMS connection factory jmsConnectionFactory

  Scenario: Predefined multiline body
    Given JMS message body
      """
      {
        "message": "Hello from Citrus!"
      }
      """
    When send JMS message to destination hello
    Then verify JMS message body
      """
      { "message": "Hello from Citrus!" }
      """
    And receive JMS message on destination hello

  Scenario: Multiline body
    When send JMS message with body
      """
      {
        "message": "Hello from Citrus!"
      }
      """
    Then expect JMS message with body
      """
      { "message": "Hello from Citrus!" }
      """
