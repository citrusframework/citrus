---
layout: sample
title: JMS sample
sample: sample-jms
description: Shows JMS queue connectivity
categories: [samples]
permalink: /samples/jms/
---

This sample uses JMS queue destinations in order to place new todo entries in the system under test. The JMS capabilities are
also described in [reference guide](http://www.citrusframework.org/reference/html/#jms)

Objectives
---------

The [todo-list](/samples/todo-app/) sample application provides a JMS inbound message listener for adding new todo entries.
We can send JSON messages in order to create new todo entries that are stored to the in memory storage.

The Citrus project needs a JMS connection factory that is defined in the Spring application context as bean:

{% highlight java %}
@Bean
public ConnectionFactory connectionFactory() {
    return new ActiveMQConnectionFactory("tcp://localhost:61616");
}
{% endhighlight %}
    
We use ActiveMQ as message broker so we use the respective connection factory implementation here. The message broker is automatically
started with the Maven build lifecycle.
    
No we can add a new todo entry by sending a JSON message to the JMS queue destination.

{% highlight java %}
send(todoJmsEndpoint)
    .header("_type", "com.consol.citrus.samples.todolist.model.TodoEntry")
    .payload("{ \"title\": \"${todoName}\", \"description\": \"${todoDescription}\" }");
{% endhighlight %}
        
We have to add a special message header **_type** which is required by the system under test for message conversion. The message payload
is the JSON representation of a todo entry model object.

The JMS operation is asynchronous so we do not get any response back. Next action in our test deals with validating that the new todo 
entry has been added successfully. The XPath expression validation makes sure the the last todo entry displayed is the todo item that 
we have added before in the test.

You can read about http and XPath validation features in the sample [xhtml](/samples/xhtml/)    
                
Run
---------

You can run the sample on your localhost in order to see Citrus in action. 

As we want to use the JMS capabilities of the too application we need to start the ActiveMQ message broker first. 
You can do this with Maven:
 
    mvn activemq:run

Read further the instructions [how to run](/samples/run/) the sample.