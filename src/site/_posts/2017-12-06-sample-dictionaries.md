---
layout: sample
title: Data dictionary sample
sample: sample-dictionaries
description: Add data dictionaries
categories: [samples]
permalink: /samples/dictionaries/
---

This sample deals with data dictionaries that translate message content while exchanging data with the todo sample
application. Read about the Citrus data dictionary feature in [reference guide](http://www.citrusframework.org/reference/html/index.html#data-dictionaries).

Objectives
---------

The [todo-list](/samples/todo-app/) sample application provides a REST API for managing todo entries.
We call this API and receive Json message structures for validation in our test cases. The Json message content is manipulated before
exchanging with the system under test via data dictionaries. The dictionary is added as component to the Spring bean application context.

{% highlight java %}
@Bean
public JsonPathMappingDataDictionary inboundDictionary() {
    JsonPathMappingDataDictionary dataDictionary = new JsonPathMappingDataDictionary();
    dataDictionary.setGlobalScope(false);
    dataDictionary.setMappingFile(new ClassPathResource("dictionary/inbound.properties"));
    return dataDictionary;
}

@Bean
public JsonPathMappingDataDictionary outboundDictionary() {
    JsonPathMappingDataDictionary dataDictionary = new JsonPathMappingDataDictionary();
    dataDictionary.setGlobalScope(false);
    dataDictionary.setMappingFile(new ClassPathResource("dictionary/outbound.properties"));
    return dataDictionary;
}
{% endhighlight %}
                
We define two dictionaries, one for inbound messages and another for outbound messages. In the dictionary mapping files we can provide several JsonPath
expressions that should be applied to the messages before exchange.

{% highlight java %}
$.title=citrus:concat('todo_', citrus:randomNumber(4))
$.description=Description: todo_${todoId}
$.done=false
{% endhighlight %}

The outbound mappings above generate dynamic test data for message element on the todo Json payloads. The todo title is automatically set to a random string using the `citrus:randomNumber()` function.
Also the _description_ and _done_ field is set to a proper value.

The dictionary can be applied to each send operation in Citrus.

{% highlight java %}
http()
    .client(todoClient)
    .send()
    .post("/todolist")
    .messageType(MessageType.JSON)
    .dictionary("outboundDictionary")
    .contentType("application/json")
    .payload("{ \"id\": \"${todoId}\", \"title\": null, \"description\": null, \"done\": null}"); 
{% endhighlight %}
        
As you can see the outbound dictionary overwrites message content before the actual message is sent out. The message payload in the send operation
does not need to set proper values for _title_, _description_ and _done_. These values can be _null_. The dictionary makes sure that the message content is manipulated before
exchanging with the foreign service.

Same mechanism applies for inbound dictionaries. Here we define assertions on message elements that are automatically applied to the receive operation.

{% highlight java %}
$.title=todo_${todoId}
$.description=@startsWith('Description: ')@
$.done=false
{% endhighlight %}
    
The JsonPath expression mappings above make sure that the message validation is manipulated before taking action. This way we are able to set common validation and manipulation steps in
multiple data dictionaries. Multiple send and receive operations can use the dictionary mappings and we are able to manage those mappings on a very central point of
configuration.

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