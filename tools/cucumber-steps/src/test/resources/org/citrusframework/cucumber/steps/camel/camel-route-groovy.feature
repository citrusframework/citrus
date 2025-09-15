Feature: Camel groovy route

  Background:
    Given variable logLevel is "INFO"
    Given Camel route hello.groovy
    """
    from("direct:hello")
     .to("log:org.citrusframework.cucumber.steps.camel?level=${logLevel}")
     .split(body().tokenize(" "))
       .to("seda:tokens")
     .end()
    """

  Scenario: Send body
    When send Camel exchange to("direct:hello") with body: Hello Camel from Groovy!
    And receive Camel exchange from("seda:tokens") with body: Hello
    And receive Camel exchange from("seda:tokens") with body: Camel
    And receive Camel exchange from("seda:tokens") with body: from
    And receive Camel exchange from("seda:tokens") with body: Groovy!

  Scenario: Expect body received
    Given Camel exchange body: Hi Camel!
    When send Camel exchange to("direct:hello")
    Then expect Camel exchange body: Hi
    And receive Camel exchange from("seda:tokens")
    Then expect Camel exchange body: Camel!
    And receive Camel exchange from("seda:tokens")

  Scenario: Body multiline
    Given Camel exchange body
    """
    Howdy Camel!
    """
    When send Camel exchange to("direct:hello")
    Then expect Camel exchange body
    """
    Howdy
    """
    And receive Camel exchange from("seda:tokens")
    Then expect Camel exchange body
    """
    Camel!
    """
    And receive Camel exchange from("seda:tokens")

  Scenario: Load body
    Given load Camel exchange body body.txt
    When send Camel exchange to("direct:hello")
    And receive Camel exchange from("seda:tokens") with body: Hello
    And receive Camel exchange from("seda:tokens") with body: from
    And receive Camel exchange from("seda:tokens") with body: Citrus!
