---
layout: post
title: Apache Camel Integration Testing - Part 3
short-title: Apache Camel III
author: Christoph Deppisch
github: christophd
categories: [blog]
---

In this post I will continue with the Apache Camel integration testing scenario that we have worked on in [part one](/news/2014/11/21/camel-testing-part-1) and
[part two](/news/2015/05/27/camel-testing-part-2) of this series.
This time we focus on exception handling in Camel routes. First of all let's add exception handling to our Camel route.

{% highlight xml %}
<camelContext id="camelContext" xmlns="http://camel.apache.org/schema/spring">
  <route id="helloRoute">
    <from uri="direct:hello"/>
    <to uri="seda:sayHello" pattern="InOut"/>
    <onException>
      <exception>com.consol.citrus.exceptions.CitrusRuntimeException</exception>
      <to uri="seda:errors"/>
    </onException>
  </route>
</camelContext>
{% endhighlight %}

Camel supports exception handling on specific exception types. The _onException_ route logic is executed when the defined exception type was raised
during message processing. In the sample route we call a separate Seda endpoint _seda:errors_ for further exception handling. The challenge for our
test scenario is obvious. We need to force this error handling and we need to make sure that the Seda endpoint _seda:errors_ has been called accordingly.

Let's add the error handling endpoint to the Citrus Spring bean configuration.

{% highlight xml %}
<citrus-camel:sync-endpoint id="sedaErrorHandlingEndpoint"
                       camel-context="camelContext"
                       endpoint-uri="seda:errors"/>
{% endhighlight %}

The static endpoint definition is not mandatory as Citrus is also able to work with dynamic endpoints. In our case the dynamic endpoint for consuming messages on
the _seda:errors_ endpoint would simply be _camel:sync:seda:errors_. The decision which kind of endpoint someone should use depends
on how much the endpoint could be reused throughout multiple test scenarios. Of course static endpoints are highly reusable in different test cases. On the downside
we have to manage the additional configuration burden. In this post I will use the static endpoint that is injected to the test case via Spring's autowiring mechanism.
Let's write the integration test.

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

    @Autowired
    @Qualifier("sedaErrorHandlingEndpoint")
    private Endpoint sedaErrorHandlingEndpoint;

    @CitrusTest(name = "SayHello_Error_Test")
    public void sayHello_Error_Test() {
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
            .payload("Something went wrong!")
            .header("citrus_camel_exchange_exception",
                    "com.consol.citrus.exceptions.CitrusRuntimeException")
            .header("citrus_camel_exchange_exception_message",
                    "Something went wrong!");

        receive(sedaErrorHandlingEndpoint)
            .messageType(MessageType.PLAINTEXT)
            .payload("Something went wrong!")
            .header("CamelExceptionCaught",
                "com.consol.citrus.exceptions.CitrusRuntimeException: Something went wrong!");

        send(sedaErrorHandlingEndpoint)
            .messageType(MessageType.PLAINTEXT)
            .payload("Hello after error!");

        receive(directHelloEndpoint)
            .messageType(MessageType.PLAINTEXT)
            .payload("Hello after error!");
    }
}
{% endhighlight %}

The magic happens when Citrus sends back a synchronous response on the _seda:sayHello_ endpoint which is done right after the sleep action in our sample test.
Instead of responding with a usual plain text message we add special header values __citrus_camel_exchange_exception__ and __citrus_camel_exchange_exception_message__.

{% highlight java %}
send(sedaHelloEndpoint)
    .messageType(MessageType.PLAINTEXT)
    .payload("Something went wrong!")
    .header("citrus_camel_exchange_exception",
            "com.consol.citrus.exceptions.CitrusRuntimeException")
    .header("citrus_camel_exchange_exception_message",
            "Something went wrong!");
{% endhighlight %}

These special Citrus headers instruct the Citrus Camel endpoint to raise an exception. We are able to specify the exception type as well as the exception message. As a result
the Citrus endpoint raises the exception on the Camel exchange which should force the exception handling in our Camel route.

The route _onException_ block in our example should send the error to the _seda:errors_ endpoint. So let's consume this message in a next test step.

{% highlight java %}
receive(sedaErrorHandlingEndpoint)
    .messageType(MessageType.PLAINTEXT)
    .payload("Something went wrong!")
    .header("CamelExceptionCaught",
        "com.consol.citrus.exceptions.CitrusRuntimeException: Something went wrong!");
{% endhighlight %}

With this receive action on the error endpoint we intend to validate that the exception handling was done as expected. We are able to check the error message payload and in addition
to that we have access to the Camel internal message headers that indicate the exception handling. Both message payload and message headers are compared to expected values in Citrus.

As a next test step we should provide a proper response message that is used as fallback response. The response is sent back as synchronous response on the _seda:errors_ endpoint saying _Hello after error!_.
The Camel _onException_ block and in detail the default Camel error handler will use this message as final result of the route logic. So finally in our test we can receive the fallback response message as result
of our initial direct _direct:hello_ request.

This completes this simple test scenario. We raised an exception and forced the Camel route to perform proper exception handling. With the Citrus endpoints in duty we received the error message and provided a fallback
response message which is used as final result of the Camel route.

Error handling in message based enterprise integration scenarios is complex. We need to deal with delivery timeouts, retry strategies and proper transaction handling. This post only showed the top of the iceberg but I hope
you got the idea of how to set up automated integration tests with Apache Camel routes. The <a href="http://www.citrusframework.org" title="Citrus framework" target="_blank">Citrus framework</a> is focused on providing real message endpoints no matter of what kind or nature (Http, JMS, REST, SOAP, Ftp, XML, JSON,
Seda and so on). What we get is automated integration testing with real messages being exchanged on different transports and endpoints.
