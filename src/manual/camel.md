## Apache Camel support

Apache Camel project implements the enterprise integration patterns for building mediation and routing rules in your enterprise application. With the Citrus Camel support you are able to directly interact with the Apache Camel components and route definitions. You can call Camel routes and receive synchronous response messages. You can also simulate the Camel route endpoint with receiving messages and providing simulated response messages.

**Note**
The camel components in Citrus are kept in a separate Maven module. So you should add the module as Maven dependency to your project accordingly.

```xml
<dependency>
  <groupId>com.consol.citrus</groupId>
  <artifactId>citrus-camel</artifactId>
  <version>2.6.2-SNAPSHOT</version>
</dependency>
```

Citrus provides a special Apache Camel configuration schema that is used in our Spring configuration files. You have to include the citrus-camel namespace in your Spring configuration XML files as follows.

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xmlns:citrus="http://www.citrusframework.org/schema/config"
      xmlns:citrus-camel="http://www.citrusframework.org/schema/camel/config"
      xsi:schemaLocation="
      http://www.springframework.org/schema/beans
      http://www.springframework.org/schema/beans/spring-beans.xsd
      http://www.citrusframework.org/schema/config
      http://www.citrusframework.org/schema/config/citrus-config.xsd
      http://www.citrusframework.org/schema/camel/config
      http://www.citrusframework.org/schema/camel/config/citrus-camel-config.xsd">

      [...]

      </beans>
```

Now you are ready to use the Citrus Apache Camel configuration elements using the citrus-camel namespace prefix.

The next sections explain the Citrus capabilities while working with Apache Camel.

### Camel endpoint

Camel and Citrus both use the endpoint pattern in order to define message destinations. Users can interact with these endpoints when creating the mediation and routing logic. The Citrus endpoint component for Camel interaction is defined as follows in your Citrus Spring configuration.

```xml
<citrus-camel:endpoint id="directCamelEndpoint"
      endpoint-uri="direct:news"/>
```

Right next to that Citrus endpoint we need the Apache Camel route that is located inside a camel context component.

```xml

<camelContext id="camelContext" xmlns="http://camel.apache.org/schema/spring">
  <route id="newsRoute">
    <from uri="direct:news"/>
    <to uri="log:com.consol.citrus.camel?level=INFO"/>
    <to uri="seda:news-feed"/>
  </route>
</camelContext>
```

As you can see the Citrus camel endpoint is able to interact with the Camel route. In the example above the Camel context is placed as Spring bean Camel context. This would be the easiest setup to use Camel with Citrus as you can add the Camel context straight to the Spring bean application context. Of course you can also import your Camel context and routes from other Spring bean context files or you can start the Camel context routes with Java code.

In the example the Apache Camel route is listening on the route endpoint uri **direct:news** . Incoming messages will be logged to the console using a **log** Camel component. After that the message is forwarded to a **seda** Camel component which is a simple queue in memory. So we have a small Camel routing logic with two different message transports.

The Citrus endpoint can interact with this sample route definition. The endpoint configuration holds the endpoint uri information that tells Citrus how to access the Apache Camel route destination. This endpoint uri can be any Camel endpoint uri that is used in a Camel route. Here we just use the direct endpoint uri **direct:news** so the sample Camel route gets called directly. In your test case you can use this endpoint component referenced by its id or name in order to send and receive messages on the route address **direct:news** . The Camel route listening on this direct address will be invoked accordingly.

The Apache Camel routes support asynchronous and synchronous message communication patterns. By default Citrus uses asynchronous communication with Camel routes. This means that the Citrus producer sends the exchange message to the route endpoint uri and is finished immediately. There is no synchronous response to await. In contrary to that the synchronous endpoint will send and receive a synchronous message on the Camel destination route. We will discuss this later on in this chapter. For now we have a look on how to use the Citrus camel endpoint in a test case in order to send a message to the Camel route:

```xml
<send endpoint="directCamelEndpoint">
  <message type="plaintext">
    <payload>Hello from Citrus!</payload>
  </message>
</send>
```

The Citrus camel endpoint component can also be used in a receive message action in your test case. In this situation you would receive a message from the route endpoint. This is especially designed for queueing endpoint routes such as the Camel seda component. In our example Camel route above the seda Camel component is called with the endpoint uri **seda:news-feed** . This means that the Camel route is sending a message to the seda component. Citrus is able to receive this route message with a endpoint component like this:

```xml
<citrus-camel:endpoint id="sedaCamelEndpoint"
    endpoint-uri="seda:news-feed"/>
```

You can use the Citrus camel endpoint in your test case receive action in order to consume the message on the seda component.

```xml
<receive endpoint="sedaCamelEndpoint">
  <message type="plaintext">
    <payload>Hello from Citrus!</payload>
  </message>
</receive>
```

**Tip**
Instead of defining a static Citrus camel component you could also use the dynamic endpoint components in Citrus. This would enable you to send your message directly using the endpoint uri **direct:news** in your test case. Read more about this in[endpoint-components](endpoint-components).

Citrus is able to send and receive messages with Camel route endpoint uri. This enables you to invoke a Camel route. The Camel components used is defined by the endpoint uri as usual. When interacting with Camel routes you might need to send back some response messages in order to simulate boundary applications. We will discuss the synchronous communication in the next section.

### Synchronous Camel endpoint

The synchronous Apache Camel producer sends a message to some route and waits synchronously for the response to arrive. In Camel this communication is represented with the exchange pattern **InOut** . The basic configuration for a synchronous Apache Camel endpoint component looks like follows:

```xml
<citrus-camel:sync-endpoint id="camelSyncEndpoint"
      endpoint-uri="direct:hello"
      timeout="1000"
      polling-interval="300"/>
```

Synchronous endpoints poll for synchronous reply messages to arrive. The poll interval is an optional setting in order to manage the amount of reply message handshake attempts. Once the endpoint was able to receive the reply message synchronously the test case can receive the reply. In case the reply message is not available in time we raise some timeout error and the test will fail.

In a first test scenario we write a test case the sends a message to the synchronous endpoint and waits for the synchronous reply message to arrive. So we have two actions on the same Citrus endpoint, first send then receive.

```xml
<send endpoint="camelSyncEndpoint">
  <message type="plaintext">
    <payload>Hello from Citrus!</payload>
  </message>
</send>

<receive endpoint="camelSyncEndpoint">
  <message type="plaintext">
    <payload>This is the reply from Apache Camel!</payload>
  </message>
</receive>
```

The next variation deals with the same synchronous communication, but send and receive roles are switched. Now Citrus receives a message from a Camel route and has to provide a reply message. We handle this synchronous communication with the same synchronous Apache Camel endpoint component. Only difference is that we initially start the communication by receiving a message from the endpoint. Knowing this Citrus is able to send a synchronous response back. Again just use the same endpoint reference in your test case. So we have again two actions in our test case, but this time first receive then send.

```xml
<receive endpoint="camelSyncEndpoint">
  <message type="plaintext">
    <payload>Hello from Apache Camel!</payload>
  </message>
</receive>

<send endpoint="camelSyncEndpoint">
  <message type="plaintext">
    <payload>This is the reply from Citrus!</payload>
  </message>
</send>
```

This is pretty simple. Citrus takes care on setting the Apache Camel exchange pattern **InOut** while using synchronous communications. The Camel routes do respond and Citrus is able to receive the synchronous messages accordingly. With this pattern you can interact with Apache Camel routes where Citrus simulates synchronous clients and consumers.

### Camel exchange headers

Apache Camel uses exchanges when sending and receiving messages to and from routes. These exchanges hold specific information on the communication outcome. Citrus automatically converts these exchange information to special message header entries. You can validate those exchange headers then easily in your test case:

```xml
<receive endpoint="sedaCamelEndpoint">
  <message type="plaintext">
    <payload>Hello from Camel!</payload>
  </message>
  <header>
    <element name="citrus_camel_route_id" value="newsRoute"/>
    <element name="citrus_camel_exchange_id" value="ID-local-50532-1402653725341-0-3"/>
    <element name="citrus_camel_exchange_failed" value="false"/>
    <element name="citrus_camel_exchange_pattern" value="InOnly"/>
    <element name="CamelCorrelationId" value="ID-local-50532-1402653725341-0-1"/>
    <element name="CamelToEndpoint" value="seda://news-feed"/>
  </header>
</receive>
```

Besides the Camel specific exchange information the Camel exchange does also hold some custom properties. These properties such as **CamelToEndpoint** or **CamelCorrelationId** are also added automatically to the Citrus message header so can expect them in a receive message action.

### Camel exception handling

Let us suppose following route definition:

```xml
<camelContext id="camelContext" xmlns="http://camel.apache.org/schema/spring">
  <route id="newsRoute">
    <from uri="direct:news"/>
    <to uri="log:com.consol.citrus.camel?level=INFO"/>
    <to uri="seda:news-feed"/>
    <onException>
      <exception>com.consol.citrus.exceptions.CitrusRuntimeException</exception>
      <to uri="seda:exceptions"/>
    </onException>
  </route>
</camelContext>
```

The route has an exception handling block defined that is called as soon as the exchange processing ends up in some error or exception. With Citrus you can also simulate a exchange exception when sending back a synchronous response to a calling route.

```xml
<send endpoint="sedaCamelEndpoint">
  <message type="plaintext">
    <payload>Something went wrong!</payload>
  </message>
  <header>
    <element name="citrus_camel_exchange_exception"
                value="com.consol.citrus.exceptions.CitrusRuntimeException"/>
    <element name="citrus_camel_exchange_exception_message" value="Something went wrong!"/>
    <element name="citrus_camel_exchange_failed" value="true"/>
  </header>
</send>
```

This message as response to the **seda:news-feed** route would cause Camel to enter the exception handling in the route definition. The exception handling is activated and calls the error handling route endpoint **seda:exceptions** . Of course Citrus would be able to receive such an exception exchange validating the exception handling outcome.

In such failure scenarios the Apache Camel exchange holds the exception information (**CamelExceptionCaught**) such as causing exception class and error message. These headers are present in an error scenario and can be validated in Citrus when receiving error messages as follows:

```xml
<receive endpoint="errorCamelEndpoint">
  <message type="plaintext">
    <payload>Something went wrong!</payload>
  </message>
  <header>
    <element name="citrus_camel_route_id" value="newsRoute"/>
    <element name="citrus_camel_exchange_failed" value="true"/>
    <element name="CamelExceptionCaught"
        value="com.consol.citrus.exceptions.CitrusRuntimeException: Something went wrong!"/>
  </header>
</receive>
```

This completes the basic exception handling in Citrus when using the Apache Camel endpoints.

### Camel context handling

In the previous samples we have used the Apache Camel context as Spring bean context that is automatically loaded when Citrus starts up. Now when using a single Camel context instance Citrus is able to automatically pick this Camel context for route interaction. If you use more that one Camel context you have to tell the Citrus endpoint component which context to use. The endpoint offers an optional attribute called **camel-context** .

```xml
<citrus-camel:endpoint id="directCamelEndpoint"
    camel-context="newsContext"
    endpoint-uri="direct:news"/>

<camelContext id="newsContext" xmlns="http://camel.apache.org/schema/spring">
    <route id="newsRoute">
      <from uri="direct:news"/>
      <to uri="log:com.consol.citrus.camel?level=INFO"/>
      <to uri="seda:news-feed"/>
    </route>
</camelContext>

<camelContext id="helloContext" xmlns="http://camel.apache.org/schema/spring">
  <route id="helloRoute">
    <from uri="direct:hello"/>
    <to uri="log:com.consol.citrus.camel?level=INFO"/>
    <to uri="seda:hello"/>
  </route>
</camelContext>
```

In the example abpove we have two Camel context instances loaded. The endpoint has to pick the context to use with the attribute **camel-context** which resides to the Spring bean id of the Camel context.

### Camel route actions

Since Citrus 2.4 we introduced some Camel specific test actions that enable easy interaction with Camel routes and the Camel context. The test actions do follow a specific XML namespace so we have to add this namespace to the test case when using the actions.

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xmlns:camel="http://www.citrusframework.org/schema/camel/testcase"
      xsi:schemaLocation="
      http://www.springframework.org/schema/beans
      http://www.springframework.org/schema/beans/spring-beans.xsd
      http://www.citrusframework.org/schema/camel/testcase
      http://www.citrusframework.org/schema/camel/testcase/citrus-camel-testcase.xsd">

  [...]

</beans>
```

We added a special camel namespace with prefix **camel:** so now we can start to add Camel test actions to the test case:

**XML DSL** 

```xml
<testcase name="CamelRouteIT">
  <actions>
      <camel:create-routes>
        <routeContext xmlns="http://camel.apache.org/schema/spring">
          <route id="route_1">
            <from uri="direct:test1"/>
            <to uri="mock:test1"/>
          </route>

          <route id="route_2">
              <from uri="direct:test2"/>
              <to uri="mock:test2"/>
          </route>
        </routeContext>
      </camel:create-routes>

      <camel:create-routes camel-context="camelContext">
        <routeContext xmlns="http://camel.apache.org/schema/spring">
          <route>
            <from uri="direct:test3"/>
            <to uri="mock:test3"/>
          </route>
        </routeContext>
      </camel:create-routes>
  </actions>
</testcase>
```

In the example above we have used the **camel:create-route** test action that will create new Camel routes at runtime in the Camel context. The target Camel context is specified with the optional **camel-context** attribute. By default Citrus will search for a Camel context available in the Spring bean application context. Removing routes at runtime is also supported.

**XML DSL** 

```xml
<testcase name="CamelRouteIT">
  <actions>
      <camel:remove-routes camel-context="camelContext">
        <route id="route_1"/>
        <route id="route_2"/>
        <route id="route_3"/>
      </camel:remove-routes>
  </actions>
</testcase>
```

Next operation we will discuss is the start and stop of existing Camel routes:

**XML DSL** 

```xml
<testcase name="CamelRouteIT">
  <actions>
      <camel:start-routes camel-context="camelContext">
        <route id="route_1"/>
      </camel:start-routes>

      <camel:stop-routes camel-context="camelContext">
      <route id="route_2"/>
      <route id="route_3"/>
      </camel:stop-routes>
  </actions>
</testcase>
```

Starting and stopping Camel routes at runtime is important when temporarily Citrus need to receive a message on a Camel endpoint URI. We can stop a route, use a Citrus camel endpoint instead for validation and start the route after the test is done. This way wen can also simulate errors and failure scenarios in a Camel route interaction.

Of course all Camel route actions are also available in Java DSL.

**Java DSL** 

```java
@Autowired
private CamelContext camelContext;

@CitrusTest
public void camelRouteTest() {
    camel().context(camelContext).create(new RouteBuilder(camelContext) {
          @Override
          public void configure() throws Exception {
              from("direct:news")
                  .routeId("route_1")
                  .autoStartup(false)
                  .setHeader("headline", simple("This is BIG news!"))
                  .to("mock:news");

              from("direct:rumors")
                  .routeId("route_2")
                  .autoStartup(false)
                  .setHeader("headline", simple("This is just a rumor!"))
                  .to("mock:rumors");
          }
      });

    camel().context(camelContext).start("route_1", "route_2");

    camel().context(camelContext).stop("route_2");

    camel().context(camelContext).remove("route_2");
}
```

As you can see we have access to the Camel route builder that adds 1-n new Camel routes to the context. After that we can start, stop and remove the routes within the test case.

### Camel controlbus actions

The Camel controlbus component is a good way to access route statistics and route status information within a Camel context. Citrus provides controlbus test actions to easily access the controlbus operations at runtime.

**XML DSL** 

```xml
<testcase name="CamelControlBusIT">
  <actions>
    <camel:control-bus>
      <camel:route id="route_1" action="start"/>
    </camel:control-bus>

    <camel:control-bus camel-context="camelContext">
      <camel:route id="route_2" action="status"/>
      <camel:result>Stopped</camel:result>
    </camel:control-bus>

    <camel:control-bus>
      <camel:language type="simple">${camelContext.stop()}</camel:language>
    </camel:control-bus>

    <camel:control-bus camel-context="camelContext">
      <camel:language type="simple">${camelContext.getRouteStatus('route_3')}</camel:language>
      <camel:result>Started</camel:result>
    </camel:control-bus>
  </actions>
</testcase>
```

The example test case shows the controlbus access. Camel provides two different ways to specify operations and parameters. The first option is the use of an **action** attribute. The Camel route id has to be specified as mandatory attribute. As a result the controlbus action will be executed on the target route during test runtime. This way we can also start and stop Camel routes in a Camel context.

In case an controlbus operation has a result such as the **status** action we can specify a control result that is compared. Citrus will raise validation exceptions when the results differ. The second option for executing a controlbus action is the language expression. We can use Camel language expressions on the Camel context for accessing a controlbus operation. Also here we can define an optional outcome as expected result.

The Java DSL also supports these controlbus operations as the next example shows:

**Java DSL** 

```java
@Autowired
private CamelContext camelContext;

@CitrusTest
public void camelRouteTest() {
      camel().controlBus()
              .route("my_route", "start");

      camel().controlBus()
              .language(SimpleBuilder.simple("${camelContext.getRouteStatus('my_route')}"))
              .result(ServiceStatus.Started);
}
```

The Java DSL works with Camel language expression builders as well as **ServiceStatus** enum values as expected result.

