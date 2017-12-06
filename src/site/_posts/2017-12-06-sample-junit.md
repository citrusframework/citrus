---
layout: sample
title: JUnit sample
sample: sample-junit
description: Use JUnit test framework
categories: [samples]
permalink: /samples/junit/
---

This sample demonstrates the JUnit support in Citrus. We write some JUnit Citrus test cases that test the REST API of the todo sample application. The JUnit support is
also described in detail in [reference guide](http://www.citrusframework.org/reference/html/index.html#run-with-junit)

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
    
In test cases we can reference this client component in order to send REST calls to the server. Citrus is able to integrate with JUnit as test execution framework. You can use
the `JUnit4CitrusTestRunner` implementation as base for your test.
    
{% highlight java %}
public class TodoListIT extends JUnit4CitrusTestRunner {

    @Autowired
    private HttpClient todoClient;

    @Test
    @CitrusTest
    public void testPost() {
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        http(action -> action.client(todoClient)
            .send()
            .post("/todolist")
            .contentType("application/x-www-form-urlencoded")
            .payload("title=${todoName}&description=${todoDescription}"));

        http(action -> action.client(todoClient)
            .receive()
            .response(HttpStatus.FOUND));
    }
}      
{% endhighlight %}
        
The `JUnit4CitrusTestRunner` makes sure that Citrus framework is loaded at startup and all configuration is done properly. Also we need to set the annotation `@CitrusTest` on our test methods in
addition to the normal JUnit `@Test` annotation. This way we can inject Citrus endpoints such as the `todoClient` and we can use the runner Java fluent API in Citrus to send and receive messages using that client component. 

As an alternative to that you can also use the test designer fluent API. You need to extend from `JUnit4CitrusTestDesigner` base class then. The other concepts and configuration stays the same.

Last not least we can also use resource injection to the test methods using `@CitrusResource` method parameter annotations.

{% highlight java %}
public class TodoListInjectIT extends JUnit4CitrusTest {

    @Autowired
    private HttpClient todoClient;

    @Test
    @CitrusTest
    public void testPost(@CitrusResource TestRunner runner) {
        runner.variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        runner.variable("todoDescription", "Description: ${todoName}");

        runner.http(action -> action.client(todoClient)
            .send()
            .post("/todolist")
            .contentType("application/x-www-form-urlencoded")
            .payload("title=${todoName}&description=${todoDescription}"));

        runner.http(action -> action.client(todoClient)
            .receive()
            .response(HttpStatus.FOUND));
    }

}  
{% endhighlight %}
  
We can inject method parameters such as `@CitrusResource` annotated `TestRunner` that is our entrance to the Citrus Java fluent API.

We can use the Citrus Java DSL fluent API in the JUnit test in order to exchange messages with the todo application system under test. The test is a normal JUnit test that is executable via Java IDE or command line using Maven or Gradle.

In order to setup Maven for JUnit we need to add the dependency to the project POM file.

{% highlight xml %}
<dependency>
  <groupId>junit</groupId>
  <artifactId>junit</artifactId>
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