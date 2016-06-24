---
layout: post
title: Arquillian & Citrus in combination - Part 1
short-title: Arquillian & Citrus I
author: Christoph Deppisch
github: christophd
categories: [blog]
---

<a href="http://www.citrusframework.org" title="Citrus framework" target="_blank">Citrus</a> and <a href="http://arquillian.org/" title="Arquillian" target="_blank">Arquillian</a>
both refer to themselves as integration test frameworks. Following from that you might think these frameworks somehow ship the same package but this is not the case. In fact the frameworks work
brilliant together when it comes to automate the integration testing of JEE applications.

Arquillian has a focus on simplifying the archive deployment with the ability to run integration test cases inside the boundaries of an application container.
The tests are literally part of a server deployment. This approach brings great advantages such as accessing deployment and container resources within the test case.
The application is deployed to a real application server (e.g. Tomcat, Wildfly) which is a good idea as we then also test our application server configuration and we load
the application with all container managed resources such as database connections, JNDI resources and so on. Secondly the test case itself is able to directly use the
container managed resources with injection in the Arquillian test case which is a great thing to do! It becomes very easy to invoke your services in a close to production
nature then.

Citrus primarily has the objective to simplify the usage of different messaging transports in a test case. Citrus offers ready to use components for sending and receiving
messages as a client or server and helps to design a message flow across multiple service calls.

In combination the two frameworks manage complex integration test scenarios that perform fully automated. And this is what we want to have at the end of a day - fully automated integration tests.
The magic is possible since Arquillian offers a great extension mechanism and Citrus provides an Arquillian extension module. Once the extension is enabled in your Arquillian project
you can use Citrus features and components within a your Arquillian test case.

Lets have a look at this with a small example:

{% highlight xml %}
<extension qualifier="citrus">
  <property name="citrusVersion">2.2</property>
  <property name="autoPackage">true</property>
  <property name="suiteName">citrus-arquillian-suite</property>
</extension>
{% endhighlight %}

The extension configuration is placed in the basic Arquillian descriptor called __arquillian.xml__. We use the _citrus_ qualifier so the settings are automatically loaded with the Citrus extension.

For now the possible extension settings are:

* __citrusVersion:__ The explicit version of Citrus that should be used. Be sure to have the same library version available in your project (e.g. as Maven dependency). This property is optional. By default the extension just uses the latest stable version.
* __autoPackage:__ When true (default setting) the extension will automatically add Citrus libraries and all transitive dependencies to the test deployment. This automatically enables you to use the Citrus API inside the Arquillian test even when the test is executed inside the application container.
* __suiteName:__ This optional setting defines the name of the test suite that is used for the Citrus test run. When using before/after suite functionality in Citrus this setting might be of interest.
* __configurationClass:__ Full qualified Java class name of customized Citrus Spring bean configuration to use when loading the Citrus Spring application context. As a user you can define a custom Citrus configuration with this optional setting.

So we also need to add the Citrus Maven dependency to our project:

{% highlight xml %}
<dependency>
  <groupId>com.consol.citrus</groupId>
  <artifactId>citrus-arquillian</artifactId>
  <version>2.2</version>
</dependency>
{% endhighlight %}

The dependency automatically adds the required Citrus core dependencies so you are ready to work with Citrus framework components. In case you want to use special Citrus components like JMS, FTP, Mail or something like that you need to
add these modules each separately to your Maven POM, too. For now we want to use Http clients and the Citrus Java DSL so lets add the dependencies for that.

{% highlight xml %}
<dependency>
  <groupId>com.consol.citrus</groupId>
  <artifactId>citrus-http</artifactId>
  <version>2.2</version>
</dependency>
<dependency>
  <groupId>com.consol.citrus</groupId>
  <artifactId>citrus-java-dsl</artifactId>
  <version>2.2</version>
</dependency>
{% endhighlight %}

Now we are ready to integrate Citrus in a first Arquillian test case:

{% highlight java %}
@RunWith(Arquillian.class)
@RunAsClient
public class EmployeeResourceTest {

    @CitrusFramework
    private Citrus citrusFramework;

    @ArquillianResource
    private URL baseUri;

    private String serviceUri;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
            .addClasses(RegistryApplication.class, EmployeeResource.class,
                Employees.class, Employee.class, EmployeeRepository.class);
    }

    @Before
    public void setUp() throws MalformedURLException {
        serviceUri = new URL(baseUri, "registry/employee").toExternalForm();
    }

    /**
    * Test adding new employees and getting list of all employees.
    */
    @Test
    public void testCreateEmployeeAndGet(@CitrusTest CitrusTestBuilder citrus) {
        citrus.send(serviceUri)
            .message(new HttpMessage("name=Penny&age=20")
            .method(HttpMethod.POST)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED));

        citrus.receive(serviceUri)
            .message(new HttpMessage()
            .statusCode(HttpStatus.NO_CONTENT));

        citrus.send(serviceUri)
            .message(new HttpMessage()
            .method(HttpMethod.GET)
            .accept(MediaType.APPLICATION_XML));

        citrus.receive(serviceUri)
        .message(new HttpMessage("<employees>" +
            "<employee>" +
              "<age>20</age>" +
              "<name>Penny</name>" +
              "</employee>" +
            "</employees>")
        .statusCode(HttpStatus.OK));

        citrusFramework.run(citrus.build());
    }
}
{% endhighlight %}

As you can see this is a normal Arquillian JUnit test case. We are running the test in client mode with the __@RunAsClient__ annotation. As the
Citrus extension is active in background we are able to inject the framework instance with the __@CitrusFramework__ annotation. The Citrus
framework instance is automatically loaded and configured with all necessary settings in background when the Arquillian test starts. As usual
we can build the deployment archive with our server resources that we need to test and we have access to __@ArquillianResource__ annotated
resources such as the REST service endpoint URI of our application.

The test method itself is provided with a __@CitrusTest__ annotated method parameter that represents the Citrus execution test builder. This is a Java DSL representation
of what Citrus has to offer when it comes to sending and receiving messages over various message transports. The Citrus Arquillian extension will automatically inject
this method parameter so we can use it inside the test method block in order to define the Citrus test logic.

Basically we invoke the deployed REST service with Citrus using the Java DSL _send_ and _receive_ methods. Each receive operation in Citrus also triggers the message validation mechanism. This includes
a syntax check in case of SOAP and XML messages with a WSDL or XSD given and a semantic check on received message body and header values. The tester is able to give an expected message template that is used
as comparison template. Citrus is able to deeply walk through XML or JSON message payloads comparing elements, attributes, namespaces and values. In case the received message response does not match the given message template
the Arquillian test case will fail with respective validation errors. For now lets keep it simple so we just expect some Http response status codes in the receive operations. The last receive operation expects a very simple XML
message payload with the newly added employee data.

With the ability to send and receive messages via Http REST we simply invoke the deployed service endpoint and validate its outcome. With a sequence of send and receive operations in Citrus we can build complex message flows with
multiple service endpoints involved.

That's it we have successfully combined both frameworks. Arquillian helps us to deploy and run our application inside an application container and we have easy access to managed resources. Citrus is able to invoke the services via Http with
powerful validation of the received response messages. This is just a very simple sample for now as I just wanted to show the basic setup. Based on this knowledge we can continue with a more complex test scenario. We will start
to use Arquillian in container testing mode and we will start to use more complex Citrus server components in one of my next posts. Stay tuned!