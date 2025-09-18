Feature: Knative event consumer

  Background:
    Given variable knativeServicePort is "8181"
    And Knative service port 8181
    And Knative service "hello-service"
    And Knative event consumer timeout is 5000 ms
    And create Knative event consumer service hello-service
    And variable id is "citrus:randomNumber(4)"
    And create test event
    """
    {
      "specversion" : "1.0",
      "type" : "greeting",
      "source" : "https://github.com/citrusframework",
      "subject" : "hello",
      "id" : "say-hello-${id}",
      "datacontenttype" : "application/json",
      "time": "citrus:currentDate('yyyy-MM-dd'T'HH:mm:ss')",
      "data" : "{\"msg\": \"Hello Knative!\"}"
    }
    """

  Scenario: Receive event
    When receive Knative event
      | specversion     | 1.0 |
      | type            | greeting |
      | source          | https://github.com/citrusframework |
      | subject         | hello |
      | id              | say-hello-${id} |
      | time            | @matchesDatePattern('yyyy-MM-dd'T'HH:mm:ss')@ |
      | datacontenttype | application/json;charset=UTF-8 |
      | data            | {"msg": "Hello Knative!"} |
    Then verify test event accepted

  Scenario: Receive event selected attributes
    Given expect Knative event data: {"msg": "Hello Knative!"}
    When receive Knative event
      | type            | greeting |
      | subject         | hello |
    Then verify test event accepted

  Scenario: Receive event data
    Given expect Knative event data: {"msg": "Hello Knative!"}
    When receive Knative event
      | type            | greeting |
      | source          | https://github.com/citrusframework |
      | subject         | hello |
      | id              | say-hello-${id} |
    Then verify test event accepted

  Scenario: Receive event http
    Given expect Knative event data: {"msg": "Hello Knative!"}
    When receive Knative event
      | ce-specversion     | 1.0 |
      | ce-type            | greeting |
      | ce-source          | https://github.com/citrusframework |
      | ce-subject         | hello |
      | ce-id              | say-hello-${id} |
      | ce-time            | @matchesDatePattern('yyyy-MM-dd'T'HH:mm:ss')@ |
      | Content-Type       | application/json;charset=UTF-8 |
    Then verify test event accepted

  Scenario: Receive multiline event data
    Given expect Knative event data
    """
    {"msg": "Hello Knative!"}
    """
    When receive Knative event
      | type            | greeting |
      | source          | https://github.com/citrusframework |
      | subject         | hello |
      | id              | say-hello-${id} |
    Then verify test event accepted

  Scenario: Receive event json
    When receive Knative event as json
    """
    {
      "specversion" : "1.0",
      "type" : "greeting",
      "source" : "https://github.com/citrusframework",
      "subject" : "hello",
      "id" : "say-hello-${id}",
      "time": "@matchesDatePattern('yyyy-MM-dd'T'HH:mm:ss')@",
      "datacontenttype" : "application/json;charset=UTF-8",
      "data" : "{\"msg\": \"Hello Knative!\"}"
    }
    """
    Then verify test event accepted
