---
layout: sample
title: Object marshalling sample
sample: sample-oxm
description: Shows XML object marshalling feature when sending and receiving messages
categories: [samples]
permalink: /samples/oxm/
---

This sample demonstrates the usage of object mapping in Citrus. We are able to handle automatic object mapping
when sending and receiving message payloads. Read about this feature in [reference guide](http://www.citrusframework.org/reference/html/validation-callback.html)

Objectives
---------

The [todo-list](/samples/todo-app/) sample application provides a REST API for managing todo entries.
We call this API with object mapping in Citrus so that we do not need to write message payload JSON or XML
structures but use the model objects directly in our test cases.

We need to include the Spring oxm module in the dependencies:

{% highlight xml %}
<dependency>
  <groupId>org.springframework</groupId>
  <artifactId>spring-oxm</artifactId>
  <version>${spring.version}</version>
  <scope>test</scope>
</dependency>
{% endhighlight %}
    
Also we need to provide a marshaller component in our Spring configuration:

{% highlight xml %}
<oxm:jaxb2-marshaller id="marshaller" context-path="com.consol.citrus.samples.todolist.model"/>
{% endhighlight %}
    
Please note that the marshaller supports model object classes in package **com.consol.citrus.samples.todolist.model**. Also
we need a special **oxm** namespace that we add to the Spring application context root element:

{% highlight xml %}
<beans xmlns="http://www.springframework.org/schema/beans"
       [...]
       xmlns:oxm="http://www.springframework.org/schema/oxm">
{% endhighlight %}
    
That is all for configuration, now we can use model objects as message payload in the test cases.
  
{% highlight java %}
http()
    .client(todoClient)
    .send()
    .post("/todolist")
    .contentType("application/json")
    .payload(new TodoEntry("${todoName}", "${todoDescription}"), objectMapper);
{% endhighlight %}
        
As you can see we are able to send the model object as payload. The test variable support is also given. Citrus will automatically marshall the object to a **application/json** message content 
as **POST** request. In a receive action we are able to use a mapping validation callback in order to get access to the model objects of an incoming message payload.

{% highlight java %}
http()
    .client(todoClient)
    .receive()
    .response(HttpStatus.OK)
    .validationCallback(new JsonMappingValidationCallback<TodoEntry>(TodoEntry.class, objectMapper) {
        @Override
        public void validate(TodoEntry todoEntry, Map<String, Object> headers, TestContext context) {
            Assert.assertNotNull(todoEntry);
            Assert.assertEquals(todoEntry.getId(), uuid);    
        }
    });
{% endhighlight %}
        
The validation callback gets the model object as first method parameter. You can now add some validation logic with assertions on the model object.    
                
Run
---------

You can run the sample on your localhost in order to see Citrus in action. Read the instructions [how to run](/samples/run/) the sample.