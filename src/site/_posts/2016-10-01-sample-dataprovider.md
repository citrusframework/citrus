---
layout: sample
title: TestNG data provider sample
sample: sample-dataprovider
description: Shows TestNG data provider usage in Citrus
categories: [samples]
permalink: /samples/dataprovider/
---

This sample demonstrates how to use TestNG data providers in Citrus tests. You can also read about this in [reference guide](http://www.citrusframework.org/reference/html/#run-testng-data-providers).

Objectives
---------

The [todo-list](/samples/todo-app/) sample application provides a REST API for managing todo entries.
Citrus is able to call the API methods as a client in order to add new todo entries. In this sample we make use of
the TestNG data provider feature in terms of adding multiple todo entries within on single test.

The data provider is defined in the test case.

{% highlight java %}
@DataProvider(name = "todoDataProvider")
public Object[][] todoDataProvider() {
    return new Object[][] {
        new Object[] { "todo1", "Description: todo1" },
        new Object[] { "todo2", "Description: todo2" },
        new Object[] { "todo3", "Description: todo3" }
    };
}
{% endhighlight %}
    
The provider gives us two parameters **todoName** and **todoDescription**. The parameters can be bound to test variables
in the Citrus test with some annotation magic.
  
{% highlight java %}
@Test(dataProvider = "todoDataProvider")
@CitrusTest
@CitrusParameters( { "todoName", "todoDescription" })
public void testProvider(String todoName, String todoDescription) {
    variable("todoId", "citrus:randomUUID()");

    http()
        .client(todoClient)
        .send()
        .post("/todolist")
        .messageType(MessageType.JSON)
        .contentType("application/json")
        .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\"}");
    
    [...]    
}            
{% endhighlight %}
        
As you can see we are able to use the name and description values provided by the data provider. When executed the test performs
multiple times with respective values:

    CITRUS TEST RESULTS
    TodoListIT.testPost([todo1, Description: todo1]) ............... SUCCESS
    TodoListIT.testPost([todo2, Description: todo2]) ............... SUCCESS
    TodoListIT.testPost([todo3, Description: todo3]) ............... SUCCESS    
                
Run
---------

You can run the sample on your localhost in order to see Citrus in action. Read the instructions [how to run](/samples/run/) the sample.