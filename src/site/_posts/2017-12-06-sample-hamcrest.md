---
layout: sample
title: Hamcrest matcher sample
sample: sample-hamcrest
description: Use Hamcrest matchers
categories: [samples]
permalink: /samples/hamcrest/
---

This sample shows how to use Hamcrest matcher in validation steps. Read about this feature in [reference guide](http://www.citrusframework.org/reference/html/index.html#validate-with-jsonpath)

Objectives
---------

The [todo-list](/samples/todo-app/) sample application provides a REST API for managing todo entries.
We call this API and receive Json message structures for validation in our test cases. 

This time the validation is done using Hamcrest matcher implementations in combination with JsonPath expression evaluation.

{% highlight java %}
http()
    .client(todoClient)
    .receive()
    .response(HttpStatus.OK)
    .messageType(MessageType.JSON)
    .validate("$.keySet()", hasItems("id", "title", "description", "done"))
    .validate("$.id", equalTo(todoId))
    .validate("$.title", allOf(startsWith("todo_"), endsWith(todoId)))
    .validate("$.description", anyOf(startsWith("Description:"), nullValue()))
    .validate("$.done", not(true));
{% endhighlight %}

As you can see we are able to provide Hamcrest matcher instances as expected JsonPath value. The hamcrest matcher is evaluated with the
JsonPath expression result. This way we can construct more complex validations on JsonPath expressions.

Also we can use Hamcrest matcher as condition evaluation when using iterable containers in Citrus:

{% highlight java %}
@Test
@CitrusTest
public void testHamcrestCondition() {
    iterate()
        .condition(lessThanOrEqualTo(5))
        .actions(
            createVariable("todoId", "citrus:randomUUID()"),
            createVariable("todoName", "todo_${i}"),
            createVariable("todoDescription", "Description: ${todoName}"),
            http()
                .client(todoClient)
                .send()
                .post("/todolist")
                .messageType(MessageType.JSON)
                .contentType("application/json")
                .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": false}"),

            http()
                .client(todoClient)
                .receive()
                .response(HttpStatus.OK)
                .messageType(MessageType.PLAINTEXT)
                .payload("${todoId}")
    );
}
{% endhighlight %}
   
The iteration condition uses the `lessThanOrEqualTo` Hamcrest matcher in order to evaluate the end of the iteration loop. This time we choose to execute the nested test 
action sequence five times.

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