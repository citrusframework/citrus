Feature: Camel context

  Background:
    Given New Spring Camel context
    """
    <beans xmlns="http://www.springframework.org/schema/beans"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
                              http://camel.apache.org/schema/spring http://camel.apache.org/schema/spring/camel-spring.xsd">
      <camelContext id="helloContext" xmlns="http://camel.apache.org/schema/spring">
        <route id="helloRoute">
          <from uri="direct:hello"/>
          <to uri="log:org.citrusframework.cucumber.steps.camel?level=INFO"/>
          <split>
            <tokenize token=" "/>
            <to uri="seda:tokens"/>
          </split>
        </route>
      </camelContext>
    </beans>
    """

  Scenario: Hello Context
    Given Camel exchange body: Hello Camel!
    When send Camel exchange to("direct:hello")
    Then expect Camel exchange body: Hello
    And receive Camel exchange from("seda:tokens")
    Then expect Camel exchange body: Camel!
    And receive Camel exchange from("seda:tokens")
