---
layout: sample
title: Dynamic endpoints sample
sample: sample-dynamic-endpoints
description: Shows dynamic endpoint component usage
categories: [samples]
permalink: /samples/dynamic-endpoints/
---

This sample shows how to use dynamic endpoints in Citrus. Read about this feature in [reference guide](http://www.citrusframework.org/reference/html/#endpoint-components)

Objectives
---------

The [todo-list](/samples/todo-app/) sample application provides a REST API for managing todo entries.
Usually we would define Http client endpoint components in Citrus configuration in order to connect to the REST API
server endpoint.

In this sample we use dynamic endpoint uri instead.

{% highlight java %}
http()
    .client("http://localhost:8080")
    .send()
    .post("/todolist")
    .messageType(MessageType.JSON)
    .contentType("application/json")
    .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\"}");
{% endhighlight %}
        
As you can see the send test action defines the Http request uri as endpoint. Citrus will automatically create a Http client
component out of this endpoint uri. Also you can use this approach when receiving the response:

{% highlight java %}
http()
    .client("http://localhost:8080")
    .receive()
    .response(HttpStatus.OK)
    .messageType(MessageType.JSON)
    .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\"}");
{% endhighlight %}

The endpoint uri can hold any Citrus endpoint type and is also capable of handling endpoint properties. Let us use that in an
JMS dynamic endpoint.

{% highlight java %}
send("jms:queue:jms.todo.inbound?connectionFactory=activeMqConnectionFactory")
    .header("_type", "com.consol.citrus.samples.todolist.model.TodoEntry")
    .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\"}");    
{% endhighlight %}
        
The JMS endpoint uri defines the queue name and a connection factory as uri parameter. This connection factory is defined 
as Spring bean in the configuration.

{% highlight java %}
@Bean
public ConnectionFactory activeMqConnectionFactory() {
    return new ActiveMQConnectionFactory("tcp://localhost:61616");
}
{% endhighlight %}
        
This is how to use dynamic endpoint components in Citrus.    
                
Run
---------

You can run the sample on your localhost in order to see Citrus in action. Read the instructions [how to run](/samples/run/) the sample.