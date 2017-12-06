---
layout: sample
title: Test behavior sample
sample: sample-behavior
description: Add custom test behavior
categories: [samples]
permalink: /samples/behavior/
---

This sample the usage of Citrus test behaviors when sending and receiving messages to the todo sample
application. Read about test behaviors in [reference guide](http://www.citrusframework.org/reference/html/index.html#java-dsl-test-behaviors)

Objectives
---------

The [todo-list](/samples/todo-app/) sample application provides a REST API for managing todo entries.
We call this API and receive Json message structures for validation in our test cases.

This time we want to reuse specific message exchange logic by using test behaviors in our tests. The test behavior is a
class that provides a set of test actions for reuse in other test cases. A typical test behavior looks like this:

{% highlight java %}
public class HelloBehavior extends AbstractTestBehavior {
    private final String name;

    public HelloBehavior(String name) {
        this.name = name;
    }

    @Override
    public void apply() {
        echo("Hello " + name);
    }
}
{% endhighlight %}
    
The behavior extends `AbstractTestBehavior` and defines its logic in the apply method where you can use all Citrus Java fluent API methods as you would do in a normal test.
The behavior is then applied in your test as follows:

{% highlight java %}
@Test
@CitrusTest
public void testHelloBehavior() {
    applyBehavior(new HelloBehavior("Howard"));
    applyBehavior(new HelloBehavior("Leonard"));
    applyBehavior(new HelloBehavior("Penny"));
}   
{% endhighlight %}
    
The sample above applies the behavior in the test multiple times with different names. The result will be multiple echo test actions that print out the messages to the console logging.
Now we can use behaviors in order to add new todo entries via Http POST request.             
   
{% highlight java %}
public class AddTodoBehavior extends AbstractTestBehavior {

    private String payloadData;
    private Resource resource;
    
    @Override
    public void apply() {
        HttpClientRequestActionBuilder request = http()
            .client(todoClient)
            .send()
            .post("/todolist")
            .messageType(MessageType.JSON)
            .contentType("application/json");

        if (StringUtils.hasText(payloadData)) {
            request.payload(payloadData);
        } else if (resource != null) {
            request.payload(resource);
        }

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.PLAINTEXT)
            .payload("${todoId}");
    }

    AddTodoBehavior withPayloadData(String payload) {
        this.payloadData = payload;
        return this;
    }

    AddTodoBehavior withResource(Resource resource) {
        this.resource = resource;
        return this;
    }
}
{% endhighlight %}
    
As you can see the behavior provides support for Json payload inline data as well as file resource payloads. You can use the behavior in test then as follows:

{% highlight java %}
@Test
@CitrusTest
public void testJsonPayloadValidation() {
    variable("todoId", "citrus:randomUUID()");
    variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
    variable("todoDescription", "Description: ${todoName}");
    variable("done", "false");
    
    applyBehavior(new AddTodoBehavior()
                        .withPayloadData("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}"));
    
    applyBehavior(new AddTodoBehavior()
                        .withResource(new ClassPathResource("templates/todo.json")));
}
{% endhighlight %}

The behavior is reused multiple times with different payload input. Now we can add more behaviors for getting todo entries via Http GET requests.

{% highlight java %}
public class GetTodoBehavior extends AbstractTestBehavior {

    private String payloadData;
    private Resource resource;

    private Map<String, Object> validateExpressions = new LinkedHashMap<>();

    @Override
    public void apply() {
        http()
            .client(todoClient)
            .send()
            .get("/todo/${todoId}")
            .accept("application/json");

        HttpClientResponseActionBuilder response = http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.JSON);

        if (StringUtils.hasText(payloadData)) {
            response.payload(payloadData);
        } else if (resource != null) {
            response.payload(resource);
        }

        validateExpressions.forEach(response::validate);
    }

    GetTodoBehavior validate(String payload) {
        this.payloadData = payload;
        return this;
    }

    GetTodoBehavior validate(Resource resource) {
        this.resource = resource;
        return this;
    }

    GetTodoBehavior validate(String expression, Object expected) {
        validateExpressions.put(expression, expected);
        return this;
    }
}
{% endhighlight %}
    
This time the behavior provides different approaches how to validate the todo entry that was sent as a Json response payload. We can use payload inline data, file resource and JsonPath expressions:

{% highlight java %}
applyBehavior(new GetTodoBehavior()
                    .validate("$.id", "${todoId}")
                    .validate("$.title", "${todoName}")
                    .validate("$.description", "${todoDescription}")
                    .validate("$.done", false));   
{% endhighlight %}
                        
This completes the usage of test behaviors in Citrus. This is a great way to centralize common tasks in your project to reusable pieces of Citrus Java DSL code.

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