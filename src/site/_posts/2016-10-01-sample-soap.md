---
layout: sample
title: SOAP WebServices sample
sample: sample-soap
description: Shows SOAP web service support
categories: [samples]
permalink: /samples/soap/
---

This sample uses SOAP web services to add new todo entries on the todo app system under test. You can read more about the 
Citrus SOAP features in [reference guide](http://www.citrusframework.org/reference/html/#soap)

Objectives
---------

The [todo-list](/samples/todo-app/) sample application manages todo entries. The application provides a SOAP web service
endpoint for adding new entries and listing all entries.

The sample tests show how to use this SOAP endpoint as a client. First we define the schema and a global namespace for the SOAP
messages.

{% highlight java %}
@Bean
public SimpleXsdSchema todoListSchema() {
    return new SimpleXsdSchema(new ClassPathResource("schema/TodoList.xsd"));
}

@Bean
public XsdSchemaRepository schemaRepository() {
    XsdSchemaRepository schemaRepository = new XsdSchemaRepository();
    schemaRepository.getSchemas().add(todoListSchema());
    return schemaRepository;
}

@Bean
public NamespaceContextBuilder namespaceContextBuilder() {
    NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();
    namespaceContextBuilder.setNamespaceMappings(Collections.singletonMap("todo", "http://citrusframework.org/samples/todolist"));
    return namespaceContextBuilder;
}
{% endhighlight %}
   
The schema repository hold all known schemas in this project. Citrus will automatically check the syntax rules for incoming messages
then. Next we need a SOAP web service client component:

{% highlight java %}
@Bean
public SoapMessageFactory messageFactory() {
    return new SaajSoapMessageFactory();
}

@Bean
public WebServiceClient todoClient() {
    return CitrusEndpoints.soap()
                        .client()
                        .defaultUri("http://localhost:8080/services/ws/todolist")
                        .build();
}
{% endhighlight %}
    
The client connects to the web service endpoint on the system under test. In addition to that we define a SOAP message factory that is
responsible for creating the SOAP envelope. 

Now we can use the web service client in the Citrus test.

{% highlight java %}
soap()
    .client(todoClient)
    .send()
    .soapAction("addTodoEntry")
    .payload(new ClassPathResource("templates/addTodoEntryRequest.xml"));
    
soap()
    .client(todoClient)
    .receive()
    .payload(new ClassPathResource("templates/addTodoEntryResponse.xml"));
{% endhighlight %}
        
The Citrus test sends a request and validates the SOAP response message. The message payload is loaded from external file resources.    
                
Run
---------

You can run the sample on your localhost in order to see Citrus in action. Read the instructions [how to run](/samples/run/) the sample.