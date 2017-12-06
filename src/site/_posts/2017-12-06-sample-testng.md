---
layout: sample
title: TestNG sample
sample: sample-testng
description: Use TestNG test framework
categories: [samples]
permalink: /samples/testng/
---

This sample demonstrates the TestNG support in Citrus. We write some TestNG Citrus test cases that test the REST API of the todo sample application. The TestNG support is
also described in detail in [reference guide](http://www.citrusframework.org/reference/html/index.html#run-with-testng)

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
    
In test cases we can reference this client component in order to send REST calls to the server. Citrus is able to integrate with TestNG as test execution framework. You can use
the `TestNGCitrusTestRunner` implementation as base for your test.
    
{% highlight java %}
public class TodoListIT extends TestNGCitrusTestRunner {

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
        
The `TestNGCitrusTestRunner` makes sure that Citrus framework is loaded at startup and all configuration is done properly. Also we need to set the annotation `@CitrusTest` on our test methods in
addition to the normal TestNG `@Test` annotation. This way we can inject Citrus endpoints such as the `todoClient` and we can use the runner Java fluent API in Citrus to send and receive messages using that client component. 

As an alternative to that you can also use the test designer fluent API. You need to extend from `TestNGCitrusTestDesigner` base class then. The other concepts and configuration stays the same.

Last not least we can also use resource injection to the test methods using `@CitrusResource` method parameter annotations.

{% highlight java %}
public class TodoListInjectIT extends TestNGCitrusTest {

    @Autowired
    private HttpClient todoClient;

    @Test
    @Parameters("runner")
    @CitrusTest
    public void testPost(@Optional @CitrusResource TestRunner runner) {
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
  
We can inject method parameters such as `@CitrusResource` annotated `TestRunner` that is our entrance to the Citrus Java fluent API. In TestNG we need to add the `@Optional` annotation in order to tell
TestNG that the method parameter is not injected with TestNG but with Citrus. Also we need to give a parameter name in the `@Parameters` annotation.

We can use the Citrus Java DSL fluent API in the TestNG test in order to exchange messages with the todo application system under test. The test is a normal TestNG test that is executable via Java IDE or command line using Maven or Gradle.

In order to setup Maven for TestNG we need to add the dependency to the project POM file.

{% highlight xml %}
<dependency>
  <groupId>testng</groupId>
  <artifactId>testng</artifactId>
  <version>${testng.version}</version>
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