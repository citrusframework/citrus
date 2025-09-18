Feature: Endpoint script config

  Background:
    Given variable basePort is "1"

  Scenario: Load endpoint
    Given variable randomPort is "citrus:randomNumber(4)"
    Given URL: http://localhost:${basePort}${randomPort}
    Given load endpoint fooServer.groovy
    When verify endpoint fooServer
    Then send GET /hello
    And receive HTTP 200 OK

  Scenario: Create Http endpoint
    Given variable randomPort is "citrus:randomNumber(4)"
    Given URL: http://localhost:${basePort}${randomPort}
    Given create endpoint helloServer.groovy
    """
    http()
      .server()
      .port(${basePort}${randomPort})
      .autoStart(true)
    """
    When verify endpoint helloServer
    Then send GET /hello
    And receive HTTP 200 OK

  Scenario: Create direct endpoint
    Given create message queue hello-queue
    Given create endpoint helloEndpoint.groovy
    """
    direct()
      .asynchronous()
      .queue("hello-queue")
    """
    When verify endpoint helloEndpoint
    When endpoint helloEndpoint sends body Hello from new direct endpoint!
    Then endpoint helloEndpoint should receive body Hello from new direct endpoint!
