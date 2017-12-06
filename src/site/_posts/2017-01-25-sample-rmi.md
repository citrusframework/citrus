---
layout: sample
title: RMI sample
sample: sample-rmi
description: Shows Remote Method Invocation support
categories: [samples]
permalink: /samples/rmi/
---

This sample demonstrates interaction with Remote Method Invocation (RMI) technology.  Citrus provides client and server components for connecting to services via RMI. 
Read more about this in detail in [reference guide](http://www.citrusframework.org/reference/html/#rmi)

Objectives
---------

In this sample project a remote interface is exposed to clients via RMI. The remote interface follows the Java RMI specification.

{% highlight java %}
public interface TodoListService extends Remote {
    void addTodo(String id, String description) throws RemoteException;
    Map<String, String> getTodos() throws RemoteException;
}
{% endhighlight %}

There are two operations available. The **addTodo** operation and the **getTodos** operation. The remote interface has to be registered in
a lookup registry. Citrus can do this with the server component:

{% highlight java %}
@Bean
public RmiServer rmiServer() {
    return CitrusEndpoints.rmi()
            .server()
            .autoStart(true)
            .host("localhost")
            .port(1099)
            .remoteInterfaces(TodoListService.class)
            .binding("todoService")
            .createRegistry(true)
            .build();
}
{% endhighlight %}
                     
The server has its property **create-registry** set to true. So we create a new lookup registry on port **1099** on the **localhost**. The
remote interface is automatically registered. In addition to that the server creates a service binding with the name **todoService**.

After that clients can lookup and access the service with:
 
{% highlight xml %}
rmi://localhost:1099/todoService
{% endhighlight %}
    
Lets create a client component that uses this service url:
    
{% highlight java %}
@Bean
public RmiClient rmiClient() {
    return CitrusEndpoints.rmi()
            .client()
            .serverUrl("rmi://localhost:1099/todoService")
            .build();
}
{% endhighlight %}
    
Now there is both client and server configured in the Citrus Spring application context. Of course in a real world scenario we would act as 
client or server and the system under test is the respective partner on the other side. You can use the RMI client and server component in 
tests as usual with the Citrus Java DSL.
    
{% highlight java %}
@Test
@CitrusTest
public void testAddTodo() {
    send(todoRmiClient)
        .fork(true)
        .message(RmiMessage.invocation(TodoListService.class, "addTodo")
                .argument("todo-star")
                .argument("Star me on github"));

    receive(todoRmiServer)
        .message(RmiMessage.invocation(TodoListService.class, "addTodo")
                .argument("todo-star")
                .argument("Star me on github"));

    send(todoRmiServer)
        .message(RmiMessage.result());

    receive(todoRmiClient)
        .message(RmiMessage.result());
}    
{% endhighlight %}
    
The test method above calls the **addTodo** operation on the remote service. The operation defines arguments that
get set in the service invocation. The client automatically performs the service lookup using the service registry on port
**1099**. In the next step the test receives this very same request as a server. Remember we perform both sides of the communication 
client and server in this demonstration sample. In a real world test case you would access some foreign remote service more likely.
   
The server receive operation defines an expected service invocation with the interface **TodoListService** and the operation **addTodo**.
Even the method arguments are validated with respective values as expected.   
        
Lets also test the second operation in this remote interface **getTodos**.
  
{% highlight java %}
@Test
@CitrusTest
public void testGetTodos() {
    send(todoRmiClient)
            .fork(true)
            .message(RmiMessage.invocation(TodoListService.class, "getTodos"));

    receive(todoRmiServer)
            .message(RmiMessage.invocation(TodoListService.class, "getTodos"));

    send(todoRmiServer)
            .payload("<service-result xmlns=\"http://www.citrusframework.org/schema/rmi/message\">" +
                        "<object type=\"java.util.Map\" value=\"{todo-follow=Follow us on github}\"/>" +
                    "</service-result>");

    receive(todoRmiClient)
            .payload("<service-result xmlns=\"http://www.citrusframework.org/schema/rmi/message\">" +
                        "<object type=\"java.util.LinkedHashMap\" value=\"{todo-follow=Follow us on github}\"/>" +
                    "</service-result>");
}    
{% endhighlight %}
    
In this sample test we see that Citrus is finding a way to generify the service invocation as well as the service result.
Citrus is able to use any remote interface that you like. The operations are not implemented but do forward incoming calls to the
test for verification. Also the test case defines the service result with the returned object data.

Both ways service invocation and service result are validated with the client and server components in the test. In there is any
argument different to the expected data the test fails with respective errors.
                
Run
---------

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
     mvn clean install
    
This executes the complete Maven build lifecycle. During the build you will see Citrus performing some integration tests.

Execute all Citrus tests by calling

     mvn integration-test

You can also pick a single test by calling

     mvn integration-test -Ptest=TodoListIT

You should see Citrus performing several tests with lots of debugging output. 
And of course green tests at the very end of the build.

Of course you can also start the Citrus tests from your favorite IDE.
Just start the Citrus test using the TestNG IDE integration in IntelliJ, Eclipse or Netbeans.