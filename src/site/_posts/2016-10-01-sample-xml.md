---
layout: sample
title: XML sample
sample: sample-xml
description: Shows XML validation feature with schema and Xpath validation
categories: [samples]
permalink: /samples/xml/
---

This sample deals with XML message payloads when sending and receiving messages to the todo sample
application. Read about this feature in [reference guide](http://www.citrusframework.org/reference/html/#validation-xml)

Objectives
---------

The [todo-list](/samples/todo-app/) sample application provides a REST API for managing todo entries.
We call this API and receive XML message structures for validation in our test cases.

As we want to deal with XML data it is a good idea to enable schema validation for incoming messages. Just put your
known schemas to the schema repository and Citrus will automatically validate incoming messages with the available schema rules.

{% highlight java %}
@Bean
public SimpleXsdSchema todoListSchema() {
    return new SimpleXsdSchema(new ClassPathResource("schema/Todo.xsd"));
}

@Bean
public XsdSchemaRepository schemaRepository() {
    XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
    schemaRepository.getSchemas().add(todoListSchema());
    return schemaRepository;
}
{% endhighlight %}

That is all for configuration, now we can use XML as message payload in the test cases.
    
{% highlight java %}
http()
    .client(todoClient)
    .send()
    .post("/todolist")
    .contentType("application/xml")
    .payload("<todo>" +
                 "<id>${todoId}</id>" +
                 "<title>${todoName}</title>" +
                 "<description>${todoDescription}</description>" +
             "</todo>");
{% endhighlight %}
        
As you can see we are able to send the XML data as payload. You can add test variables in message payloads. In a receive 
action we are able to use an expected XML message payload. Citrus performs a XML tree comparison where each element is checked to meet
the expected values.

{% highlight java %}
http()
    .client(todoClient)
    .receive()
    .response(HttpStatus.OK)
    .payload("<todo>" +
                 "<id>${todoId}</id>" +
                 "<title>${todoName}</title>" +
                 "<description>${todoDescription}</description>" +
             "</todo>");
{% endhighlight %}         

The XMl message payload can be difficult to read when used as String concatenation. Fortunately we can also use file resources as message
payloads.

{% highlight java %}
http()
    .client(todoClient)
    .receive()
    .response(HttpStatus.OK)
    .payload(new ClassPathResource("templates/todo.xml"));    
{% endhighlight %}
        
An alternative approach would be to use Xpath expressions when validating incoming XML messages.

{% highlight java %}
http()
    .client(todoClient)
    .receive()
    .response(HttpStatus.OK)
    .validate("/t:todo/t:id", "${todoId}")
    .validate("/t:todo/t:title", "${todoName}")
    .validate("/t:todo/t:description", "${todoDescription}");
{% endhighlight %}
        
Each expression is evaluated and checked for expected values. XPath is namespace sensitive. So we need to use the correct namespaces
in the expressions. Here we have used a namespace prefix ***t:***. This prefix is defined in a central namespace context in the configuration.
       
{% highlight java %}   
@Bean
public NamespaceContextBuilder namespaceContextBuilder() {
    NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();
    namespaceContextBuilder.setNamespaceMappings(Collections.singletonMap("t", "http://citrusframework.org/samples/todolist"));
    return namespaceContextBuilder;
}
{% endhighlight %}
       
This makes sure that the Xpath expressions are able to find the elements with correct namespaces. Of course you can also specify the 
namespace context for each receive action individually.       
       
{% highlight java %}    
http()
    .client(todoClient)
    .receive()
    .response(HttpStatus.OK)
    .namespace("t", "http://citrusframework.org/samples/todolist")
    .validate("/t:todo/t:id", "${todoId}")
    .validate("/t:todo/t:title", "${todoName}")
    .validate("/t:todo/t:description", "${todoDescription}");
{% endhighlight %}

Run
---------

You can run the sample on your localhost in order to see Citrus in action. Read the instructions [how to run](/samples/run/) the sample.