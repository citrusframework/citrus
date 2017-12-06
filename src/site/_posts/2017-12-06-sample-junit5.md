---
layout: sample
title: JUnit5 sample
sample: sample-junit5
description: Use JUnit5 test framework
categories: [samples]
permalink: /samples/junit5/
---

This sample demonstrates the JUnit5 support in Citrus. We write some JUnit5 Citrus test cases that test the REST API of the todo sample application. The JUnit5 support is
also described in detail in [reference guide](http://www.citrusframework.org/reference/html/index.html#run-with-junit5)

Objectives
---------

The [todo-list](/samples/todo-app/) sample application provides a REST API for managing todo entries.
Citrus is able to call the API methods as a client in order to validate the Http response messages.

We need a Http client component in the configuration:

{% highlight java %}
@Bean
public HttpClient todoClient() {
    return CitrusEndpoints.http()
                        .client()
                        .requestUrl("http://localhost:8080")
                        .build();
}
{% endhighlight %}
    
In test cases we can reference this client component in order to send REST calls to the server. In JUnit5 we can use the `@ExtendsWith` annotation that loads the
`CitrusExtension` in JUnit5.
    
{% highlight java %}
@ExtendWith(CitrusExtension.class)
public class TodoListIT {

    @CitrusEndpoint
    private HttpClient todoClient;

    @Test
    @CitrusTest
    void testPost(@CitrusResource TestRunner runner) {
        http(action -> action.client(todoClient)
            .send()
            .post("/todolist")
            .contentType("application/x-www-form-urlencoded")
            .payload("title=${todoName}&description=${todoDescription}"));
            
        http(action -> action.client(todoClient)
            .receive()
            .response(HttpStatus.OK));  
    }
}  
{% endhighlight %}
        
The `CitrusExtension` makes sure that Citrus framework is loaded at startup and all configuration is done properly. Then we can inject method parameters such as `@CitrusResource` annotated `TestRunner` that is
our entrance to the Citrus Java fluent API. The runner is then able to use the `httpClient` which is automatically injected via `@CitrusEndpoint` annotation as a class field member.

We can use the Citrus Java DSL fluent API in the JUnit5 test in order to exchange messages with the todo application system under test. The test is a normal JUnit5 test that is executable via Java IDE or command line using Maven or Gradle.

In order to setup Maven for JUnit5 we need to configure the `maven-failsafe-plugin` with the JUnit platform.

{% highlight xml %}
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-failsafe-plugin</artifactId>
    <version>2.19.1</version>
    <configuration>
      <forkCount>1</forkCount>
    </configuration>
    <executions>
      <execution>
        <id>integration-tests</id>
        <goals>
          <goal>integration-test</goal>
          <goal>verify</goal>
        </goals>
      </execution>
    </executions>
    <dependencies>
      <dependency>
        <groupId>org.junit.platform</groupId>
        <artifactId>junit-platform-surefire-provider</artifactId>
        <version>${junit.platform.version}</version>
      </dependency>
    </dependencies>
</plugin>
{% endhighlight %}
    
In addition to that we need the JUnit dependency in test scope in our project:

{% highlight xml %}
<!-- Test scoped dependencies -->
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter-engine</artifactId>
  <version>${junit.version}</version>
  <scope>test</scope>
</dependency>    
{% endhighlight %}
       
That completes the project setup. We are now ready to execute the tests.

Run
---------

You can execute some sample Citrus test cases in this sample in order to write the reports.
Open a separate command line terminal and navigate to the sample folder.

Execute all Citrus tests by calling

     mvn integration-test

You should see Citrus performing several tests with lots of debugging output. 
And of course green tests at the very end of the build and some new reporting files in `target/citrus-reports` folder.

Of course you can also start the Citrus tests from your favorite IDE.
Just start the Citrus test using the TestNG IDE integration in IntelliJ, Eclipse or Netbeans.