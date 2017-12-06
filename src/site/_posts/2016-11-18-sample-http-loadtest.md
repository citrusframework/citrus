---
layout: sample
title: Http load testing sample
sample: sample-http-loadtest
description: Calls REST API on Http server with multiple threads for load testing
categories: [samples]
permalink: /samples/http-loadtest/
---

This sample demonstrates how to setup a simple load test with TestNG parallel testing. Please note that Citrus is not a performance testing tool per say.
But you can quickly setup some load on a system under test. For more sophisticated load testing Citrus may cooperate with tools like JMeter for instance.

Objectives
---------

The [todo-list](/samples/todo-app/) sample application provides a REST API for managing todo entries.
In this sample we want to call the server API in parallel multiple times in order to create a load on the system under test.

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
        
We want to call the REST API on the todolist server with that client using multiple threads in order to create 
some load on that server. We can do so by adding a TestNG parameter to the annotation of the test.
        
{% highlight java %}
@Test(invocationCount = 250, threadPoolSize = 25)
{% endhighlight %}
        
TestNG will start *25* threads in parallel that will send **250** requests in total per test to the todolist application. This creates load on that server. When you execute
this test you will see lots of requests and responses exchanged during the test run. At the end you will have 250 test instances per test reporting success or failure.

This creates very basic load testing scenarios. Of course the tests need to be stateless in order to perform in parallel. You may add message selectors on receive
operations in the test and you may have to correlate response messages so the test instances will not steal messages from each other during the test run.

{% highlight java %}
@Test(invocationCount = 250, threadPoolSize = 25)
public class TodoListLoadTestIT extends TestNGCitrusTest {

    @Autowired
    private HttpClient todoClient;

    @Parameters( { "designer" })
    @CitrusTest
    public void testAddTodo(@Optional @CitrusResource TestDesigner designer) {
        designer.http()
            .client(todoClient)
            .send()
            .post("/todolist")
            .contentType("application/x-www-form-urlencoded")
            .payload("title=citrus:concat('todo_', citrus:randomNumber(10))");

        designer.http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.FOUND);
    }

    @Parameters( { "designer" })
    @CitrusTest
    public void testListTodos(@Optional @CitrusResource TestDesigner designer) {
        designer.http()
            .client(todoClient)
            .send()
            .get("/todolist")
            .accept("text/html");

        designer.http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK);
    }

}
{% endhighlight %}
    
There are two test methods one adding a new todo entry with form url encoded Http POST request and one getting the whole list of todo entries with GET request.
Both methods are executed in parallel creating load on the server. The server must respond to all requests with success otherwise the whole test will fail.   

The test uses resource injection with method parameters. This is required for parallel testing. So each test method instance gets a separate test designer instance
to work with.

Run
---------

You can run the sample on your localhost in order to see Citrus in action. Read the instructions [how to run](/samples/run/) the sample.