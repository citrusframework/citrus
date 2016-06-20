---
layout: post
title: Apache Camel Integration Testing - Part 1
short-title: Apache Camel I
author: Christoph Deppisch
github: christophd
categories: [blog]
---

Apache Camel is a great mediation and routing framework that integrates with almost every enterprise messaging transport. In the past I have experienced Camel projects struggling with integration testing where the actual message interfaces to boundary applications are not tested properly in an automated way.

So in a series of posts I would like to talk about integration testing strategies for Apache Camel projects using the Citrus integration test framework.

* <a href="http://christophd.github.io/camel-testing-part-1/" title="Part 1" target="_blank">Part 1</a>: Setup the Citrus test project and interact with a sample Apache Camel project with JMS and SOAP WebService components
* <a href="http://christophd.github.io/camel-testing-part-2/" title="Part 2" target="_blank">Part 2</a>: Invoke Camel routes from Citrus test cases and validate outbound messages
* <a href="http://christophd.github.io/camel-testing-part-3/" title="Part 3" target="_blank">Part 3</a>: Test error situations with Camel exception handling

So lets have a sample Camel project that we would like to test. We need a simple Camel route like this:

{% highlight xml %}
<camelContext id="camelContext" xmlns="http://camel.apache.org/schema/spring">
  <route id="newsRoute">
    <from uri="jms:queue:JMS.Queue.News"/>
    <to uri="log:com.consol.citrus.camel?level=INFO"/>
    <to uri="spring-ws:http://localhost:8080?soapAction=newsFeed"/>
  </route>
</camelContext>
{% endhighlight %}

The route consumes messages from a JMS destination called _JMS.Queue.News_, logs the message content and forwards the message content to a Http SOAP WebService using the Spring WS library. So we have two different messaging interfaces (JMS and SOAP WebService) to boundary systems in our sample. 

Camel does provide very good test strategies for mocking these message transports. In a unit test you can mock the boundary JMS and SOAP interfaces with special mock components. This is great, but sometimes error prone for the following reasons. The JMS protocol provides several settings that are essential for the whole application behavior. For instance the JMS consumer may operate with concurrent consumers, connection pooling and transactional behavior. These settings are done on the Camel JMS component and on the JMS connection factory. In a mocked unit test these settings are not included as the test mock just does not process the real JMS message transports. No doubt these settings make a significant difference in production and have to be tested. In addition to that the SOAP WebService interface uses a WSDL and other SOAP specific settings like the soap action header. We could also add WS-Security and WS-Addressing headers here. In a mocked unit test these quite important interface characteristics are not tested over the wire. The actual SOAP message is not created and you are not able to verify the complete message contents as they are sent over the wire.

So the crucial weapon to avoid bugs related to untested transport settings is integration testing where the actual JMS message broker and a real SOAP WebService endpoint are involved in the test. And this is where Citrus comes in. In a Citrus test case the actual JMS consumer and producer settings do apply as well as a fully qualified WebService endpoint that receives the messages via Http message protocol. We use a real JMS message broker and Http server as it is done in production.

Lets setup a Citrus project that is able to interact with the sample Camel application route.

Citrus as integration test framework works best with Maven. You can setup a Citrus Maven project in minutes with this <a href="http://www.citrusframework.org/tutorials-maven.html" title="Maven Quickstart" target="_blank">quick start tutorial</a>. Once you have done this we have a Maven project with some sample Citrus test cases already in it. No we need to add the JMS and SOAP WebService configuration to the Citrus project. First of all let us add ActiveMQ as JMS message broker.

{% highlight xml %}
<dependency>
  <groupId>org.apache.activemq</groupId>
  <artifactId>activemq-broker</artifactId>
  <version>${activemq.version}</version>
</dependency>
<dependency>
  <groupId>org.apache.activemq</groupId>
  <artifactId>activemq-spring</artifactId>
  <version>${activemq.version}</version>
</dependency>
<dependency>
  <groupId>org.apache.xbean</groupId>
  <artifactId>xbean-spring</artifactId>
  <version>${xbean.version}</version>
</dependency>
{% endhighlight %}

We need to add the ActiveMQ Maven dependencies to our Citrus project. This is done in the Maven POM _pom.xml_ file. Once we have this we can add the message broker to the Citrus configuration. We add a new Spring application context file _citrus-activemq-context.xml_ in _src/citrus/resources_ folder.

{% highlight xml %}
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:amq="http://activemq.apache.org/schema/core"
     xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
                     http://activemq.apache.org/schema/core http://activemq.apache.org/schema/core/activemq-core.xsd">

  <!-- Embedded ActiveMQ JMS broker -->
  <amq:broker useJmx="false" persistent="false">
    <amq:transportConnectors>
      <amq:transportConnector uri="tcp://localhost:61616" />
    </amq:transportConnectors>
  </amq:broker>

  <bean id="connectionFactory" class="org.apache.activemq.ActiveMQConnectionFactory">
    <property name="brokerURL" value="tcp://localhost:61616" />
  </bean>
</beans>
{% endhighlight %}

As next step we include this new Spring context file in the _citrus-context.xml_ so both the ActiveMQ message broker and the JMS connection factory get loaded during startup. Just add a import statement to the _citrus-context.xml_ file like this:

{% highlight xml %}
<import resource="classpath:citrus-activemq-context.xml"/>
{% endhighlight %}

Good! Now we are ready to connect with the JMS message transport. Let us add the Citrus JMS endpoints. You can do this also in the _citrus-context.xml_ file:

{% highlight xml %}
<citrus-jms:endpoint id="newsJmsEndpoint"
                   destination-name="JMS.Queue.News"
                   timeout="5000"/>
{% endhighlight %}

That's it! Now we can send and receive JMS messages on the JMS destination _JMS.Queue.News_. Ok so let's write a new integration test! We create a new Java class in _src/citrus/java_

{% highlight java %}
package com.consol.citrus.news;

import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import com.consol.citrus.dsl.annotations.CitrusTest;
import com.consol.citrus.ws.message.SoapMessageHeaders;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class NewsFeedTest extends TestNGCitrusTestBuilder {
    
    @CitrusTest(name = "NewsFeed_Ok_Test")
    public void newsFeed_Ok_Test() {
        send("newsJmsEndpoint")
            .payload("<News>" +
                         "<Message>Citrus rocks!</Message>" +
                     "</News>");
        
        receive("newsSoapServer")
            .payload("<News>" +
                         "<Message>Citrus rocks!</Message>" +
                     "</News>")
            .header(SoapMessageHeaders.SOAP_ACTION, "newsFeed");
    }
}
{% endhighlight %}

This represents a Citrus Java test case. Notice that this is nothing but a normal Java unit test. I use TestNG as unit test framework. Others might prefer JUnit which is also possible. I think TestNG is much more powerful but this is another story. Also notice that we referenced the _newsJmsEndpoint_ Citrus component in the first send operation. We could have used Spring autowire injection of the JmsEndpoint here, but we want to keep it simple for now. What we have right now is an integration test which actually sends the JMS message with some news content to the JMS queue destination and on the other side we receive the real SOAP message as a web server. But wait! We have not yet added the SOAP server configuration yet! Let's do this in the _citrus-context.xml_ configuration file.

{% highlight xml %}
<citrus-ws:server id="newsSoapServer"
               port="8080"
               auto-start="true"
               timeout="10000"/>
{% endhighlight %} 

The server starts a fully qualified Http web server with the SOAP endpoint for receiving the news request. In the test case we can reference the server in a receive operation. That's it! We are able to run the test. Of course we also need to start our Camel application. You can do this in another process with Maven or you deploy your Camel application to some application server. Citrus is interacting with the Camel route as a normal interface client just using the JMS endpoint. Once you run the integration test you will see how the messages are actualy sent over the wire and you will see Citrus receiving the actual SOAP request message:

{% highlight xml %}
Received SOAP request:
<?xml version="1.0" encoding="UTF-8"?><SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
<SOAP-ENV:Header jmsmessageid="ID:localhost-50188-1416562129343-3:4:1:1:1"/>
<SOAP-ENV:Body>
<News>
<Message>Citrus rocks!</Message>
</News>
</SOAP-ENV:Body>
</SOAP-ENV:Envelope>
{% endhighlight %} 

Also Citrus will put this received message content to validation with comparing the message body and header to the expected content given in the test case. Let's recap. What we have done is a complete integration test where the Camel route interfaces are called with real message transport interaction. The ActiveMQ JMS message broker is real the SOAP WebService server is real. The message transport configuration is tested properly with message conversion and expected message content validation. The Camel application is loaded and deployed with the complete configuration and settings.

Citrus invokes the Camel route by sending a proper JMS message to the route endpoint. The Camel route processes the message and sends out the SOAP request message. In case the message content is not as expected in the Citrus receive operation or in case the SOAP message is not arriving at all the integration test fails. In this integration test we can simulate both client and server side interaction with our Camel application. 

As the Citrus test case is nothing but a normal TestNG/JUnit test we can integrate the test in our Maven build lifecycle and continuous integration process. This completes our first part of how to do extended integration testing with Apache Camel projects. In my next part I will concentrate on how to interact with Apache Camel routes using direct and Seda in memory message transports.