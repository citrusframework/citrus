---
layout: sample
title: Http REST sample
sample: sample-http
description: Shows REST API calls as a client
categories: [samples]
permalink: /samples/http/
---

This sample demonstrates the Http REST capabilities in Citrus where Citrus calls REST API on a todo web application. REST features are
also described in detail in [reference guide](http://www.citrusframework.org/reference/html/#http-rest)

Objectives
---------

The [todo-list](/samples/todo-app/) sample application provides a REST API for managing todo entries.
Citrus is able to call the API methods as a client in order to validate the Http response messages.

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
   
In test cases we can reference this client component in order to send REST calls to the server.

{% highlight java %}
http()
    .client(todoClient)
    .send()
    .post("/todolist")
    .contentType("application/x-www-form-urlencoded")
    .payload("title=${todoName}&description=${todoDescription}");
{% endhighlight %}
        
As you can see we are able to send **x-www-form-urlencoded** message content as **POST** request. The response is then validated as **Http 200 OK**.

{% highlight java %}
http()
    .client(todoClient)
    .receive()
    .response(HttpStatus.OK);    
{% endhighlight %}
                
Run
---------

You can run the sample on your localhost in order to see Citrus in action. Read the instructions [how to run](/samples/run/) the sample.