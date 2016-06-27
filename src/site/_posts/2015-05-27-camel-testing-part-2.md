---
layout: post
title: Apache Camel Integration Testing - Part 2
short-title: Apache Camel II
author: Christoph Deppisch
github: christophd
categories: [blog]
---

In [part one](/news/2014/11/21/camel-testing-part-1) of this blog series we have used Citrus in combination with Apache Camel for setting up
a complete integration test scenario. Remember we have interacted with our Camel route via JMS as client and via SOAP Http WebService as a server.

Now in the second part we want to interact with a Camel route using direct and Seda in memory message transports. First of all we need a Camel route to test.

{% highlight xml %}
<camelContext id="camelContext" xmlns="http://camel.apache.org/schema/spring">
  <route id="helloRoute">
    <from uri="direct:hello"/>
    <to uri="seda:sayHello" pattern="InOut"/>
  </route>
</camelContext>
{% endhighlight %}

The Camel route is obviously very simple. The route reads messages from a direct inbound endpoint and forwards all messages to a Seda in memory channel. The Seda communication is synchronous
so the route waits for a synchronous response to arrive. Now when we would like to test this simple route we would have to provide the inbound messages to trigger the route and we would have to
provide proper synchronous response messages on the Seda endpoint.

Lets set up a Citrus test case for this test scenario. We need a direct Camel route message endpoint that is able to call the Camel route. The Camel endpoints are available with a separate Citrus module.
We need to add this library to our project as test scoped dependency if not already done so.

{% highlight xml %}
<dependency>
  <groupId>com.consol.citrus</groupId>
  <artifactId>citrus-camel</artifactId>
  <version>${citrus.version}</version>
  <scope>test</scope>
</dependency>
{% endhighlight %}

Now we can use the Citrus Camel endpoint components in the Spring bean application context that is part of the Citrus project.

{% highlight xml %}
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:citrus-camel="http://www.citrusframework.org/schema/camel/config"
     xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
                     http://www.citrusframework.org/schema/camel/config http://www.citrusframework.org/schema/camel/config/citrus-camel-config.xsd">

  <camelContext id="camelContext" xmlns="http://camel.apache.org/schema/spring">
    <route id="helloRoute">
      <from uri="direct:hello"/>
      <to uri="seda:sayHello" pattern="InOut"/>
    </route>
  </camelContext>

  <citrus-camel:sync-endpoint id="directHelloEndpoint"
                           camel-context="camelContext"
                           endpoint-uri="direct:hello"/>

  <citrus-camel:sync-endpoint id="sedaHelloEndpoint"
                           camel-context="camelContext"
                           endpoint-uri="seda:sayHello"/>
</beans>
{% endhighlight %}

As you can see we have added two Citrus endpoint components both coming from the Citrus Camel module. The first component is interacting with the direct endpoint _direct:hello_ and the second component is interacting with the
Seda _seda:sayHello_ endpoint. Both Citrus components use synchronous message communication so we are able to send and receive messages synchronously. Let's move on with writing a test case.

{% highlight java %}
package com.consol.citrus.hello;

import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import com.consol.citrus.dsl.annotations.CitrusTest;
import com.consol.citrus.endpoint.Endpoint;
import org.testng.annotations.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * @author Christoph Deppisch
 */
@Test
public class SayHelloTest extends TestNGCitrusTestBuilder {

    @Autowired
    @Qualifier("directHelloEndpoint")
    private Endpoint directHelloEndpoint;

    @Autowired
    @Qualifier("sedaHelloEndpoint")
    private Endpoint sedaHelloEndpoint;

    @CitrusTest(name = "SayHello_Ok_Test")
    public void sayHello_Ok_Test() {
        send(directHelloEndpoint)
            .fork(true)
            .messageType(MessageType.PLAINTEXT)
            .payload("Hello Citrus!");

        receive(sedaHelloEndpoint)
            .messageType(MessageType.PLAINTEXT)
            .payload("Hello Citrus!");

        sleep(500L);

        send(sedaHelloEndpoint)
            .messageType(MessageType.PLAINTEXT)
            .payload("Hello Camel!");

        receive(directHelloEndpoint)
            .messageType(MessageType.PLAINTEXT)
            .payload("Hello Camel!");
    }
}
{% endhighlight %}

The test is using Spring's autowiring mechanism in order to inject the Citrus message endpoint components. We have four message interactions in our test. First of all we send a plain text message to the direct Camel route endpoint.
The Camel route is triggered and according to the route logic the message is forwarded to the Seda endpoint. The second test action is a message receive action on this same Seda endpoint. So if the Camel route logic is working as
expected we should be able to receive a message here. The receive test action also performs a validation on the message content received. As a tester we specify the expected message payload. Citrus as test framework compares this
expectation to the message content actually arriving. When everything is matching as expected we continue with the test.

As the Seda endpoint is synchronous we can send back a response to the calling client. In our test this is done with a respective send message action that references the same Seda endpoint. Before we send back a plain text response message
we add a sleep test action in order to simulate some hard work on the backend. As a next step the Camel route receives our simulated response message and immediately responds to the calling direct endpoint client to complete the route.
This is our last step in the test case where we receive the very same response message on the direct endpoint as final message response.

Please do not get confused with this test setup. This scenario is purely constructed for demonstrating how Citrus interacts with Camel routes in terms of synchronous communication on both ends (consuming and producing). As the test performs
all actions four messages are exchanged in between Citrus and our Camel route to test. If everything is working as expected all messages are completed and the test case is successful.

Someone might feel uncomfortable in defining the Citrus endpoint components for each Camel route endpoint in the Spring application context just to reference them inside the test case. This is where dynamic endpoints come in handy. Citrus
is also able to create the endpoint at runtime. See how this looks like.

{% highlight java %}
package com.consol.citrus.hello;

import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import com.consol.citrus.dsl.annotations.CitrusTest;
import com.consol.citrus.endpoint.Endpoint;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class SayHelloTest extends TestNGCitrusTestBuilder {

    @CitrusTest(name = "SayHello_Ok_Test")
    public void sayHello_Ok_Test() {
        send("camel:sync:direct:hello")
            .fork(true)
            .messageType(MessageType.PLAINTEXT)
            .payload("Hello Camel!");

        receive("camel:sync:seda:sayHello")
            .messageType(MessageType.PLAINTEXT)
            .payload("Hello Camel!");

        sleep(500L);

        send("camel:sync:seda:sayHello")
            .messageType(MessageType.PLAINTEXT)
            .payload("Hello Citrus!");

        receive(camel:sync:direct:hello)
            .messageType(MessageType.PLAINTEXT)
            .payload("Hello Citrus!");
    }
}
{% endhighlight %}

With the dynamic endpoints we do not have to use any of the predefined endpoint components in the Citrus Spring application context. The test just creates the endpoints automatically at runtime. The result is exactly the same as before.

We have seen that Citrus is able to both send and receive message from an to route message endpoints in Apache Camel. This is a good way of testing Camel routes with simulation of route interface partners. In the next part I will
add some error scenarios where the Seda endpoint component is forcing an exception that should be handled within our Camel route.



