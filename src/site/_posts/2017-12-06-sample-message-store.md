---
layout: sample
title: Message store sample
sample: sample-message-store
description: Use the local message store
categories: [samples]
permalink: /samples/message-store/
---

When Citrus exchanges messages with foreign services the messages are stored internally in a message store. This is an in memory
cache that is filled with messages as they are sent and received within the test case. Read about this feature in [reference guide](http://www.citrusframework.org/reference/html/index.html#local-message-store)

Objectives
---------

The [todo-list](/samples/todo-app/) sample application provides a REST API for managing todo entries.
We call this API and receive Json message structures for validation in our test cases. While exchanging messages with
the todo application Citrus saves all messages to a local message store.

You can access the message store at any time in the test case using message store functions.

{% highlight java %}
http()
    .client(todoClient)
    .send()
    .post("/todolist")
    .name("todoRequest")
    .messageType(MessageType.JSON)
    .contentType("application/json")
    .payload("{\"id\": \"citrus:randomUUID()\", \"title\": \"citrus:concat('todo_', citrus:randomNumber(4))\", \"description\": \"ToDo Description\", \"done\": false}");

echo("citrus:message(todoRequest)");
{% endhighlight %}

The send operation above create a new todo entry as Json message payload and sends it to the todo application via Http POST request. The message
receives a name `todoRequest`. This is the name that is used to store the message right before it is sent out. As soon as the message processing is complete the
local store is saving the message for later usage in the test.

You can access the message store using the message store function `citrus:message(name)`. Using the name of the message provides us the message content as it has been sent to the
todo application.

We are also able to apply some JsonPath expression on the stored message:

{% highlight java %}
echo("citrus:jsonPath(citrus:message(todoRequest.payload()), '$.title')");
{% endhighlight %}

The echo expression above makes access to the local store reading the message named `todoRequest`. The content is then passed to a JsonPath function that is evaluating the todo title with
`$.title` path expression. The result is the title of the todo entry that has been sent before.

{% highlight java %}
http()
    .client(todoClient)
    .receive()
    .response(HttpStatus.OK)
    .messageType(MessageType.PLAINTEXT)
    .payload("citrus:jsonPath(citrus:message(todoRequest.payload()), '$.id')");
{% endhighlight %}
        
The receive operation has a special message payload which accesses the message store during validation and reads the dynamic todo entry id   that was created in the `todoRequest` message.

This gives us the opportunity to access message content of previously handled messages in Citrus. The local message store is per test instance so messages in the store are only visible to the
current test case instance that has created the message in the store.

Also received messages are automatically saved to the local store. So you can access the message in later test actions very easy.        

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