Feature: Camel xml route

  Background:
    Given Camel route hello.xml
    """
    <route>
      <from uri="direct:hello"/>
      <to uri="log:org.citrusframework.cucumber.steps.camel?level=INFO"/>
      <split>
        <tokenize token=" "/>
        <to uri="seda:tokens"/>
      </split>
    </route>
    """

  Scenario: Hello route
    When send Camel exchange to("direct:hello") with body: Hello Camel!
    And receive Camel exchange from("seda:tokens") with body: Hello
    And receive Camel exchange from("seda:tokens") with body: Camel!
