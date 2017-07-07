---
layout: sample
title: Cucumber sample
sample: sample-cucumber
description: Shows BDD integration with Cucumber
categories: [samples]
permalink: /samples/cucumber/
---

The sample uses Cucumber behavior driven development (BDD) library. The tests combine BDD feature stories with the famous 
Gherkin syntax and Citrus integration test capabilities. Read about this feature in [reference guide](http://www.citrusframework.org/reference/html/#cucumber)
 
Objectives
---------

This sample application shows the usage of both Cucumber and Citrus in combination. Step definitions are able to use *CitrusResource*
annotations for injecting a TestDesigner instance. The test designer is then used in steps to build a Citrus integration test.

At the end the Citrus test is automatically executed. We can use normal step definition classes that use Gherkin annotations
(@Given, @When, @Then) provided by Cucumber.

Get started
---------

We start with a feature test using JUnit and Cucumber runner.

{% highlight java %}
@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = { "com.consol.citrus.cucumber.CitrusReporter" } )
public class TodoFeatureIT {
}
{% endhighlight %}

The test feature is described in a story using Gherkin syntax.

    Feature: Todo app
    
      Scenario: Add todo entry
        Given Todo list is empty
        When I add entry "Code something"
        Then the number of todo entries should be 1
    
      Scenario: Remove todo entry
        Given Todo list is empty
        When I add entry "Remove me"
        Then the number of todo entries should be 1
        When I remove entry "Remove me"
        Then the todo list should be empty
        
The steps executed are defined in a separate class where a Citrus test designer is used to build integration test logic.

{% highlight java %}
public class TodoSteps {

    @CitrusResource
    private TestDesigner designer;

    @Given("^Todo list is empty$")
    public void empty_todos() {
        designer.http()
            .client("todoListClient")
            .send()
            .delete("/todolist");

        designer.http()
            .client("todoListClient")
            .receive()
            .response(HttpStatus.FOUND);
    }

    @When("^I add entry \"([^\"]*)\"$")
    public void add_entry(String todoName) {
        designer.http()
            .client("todoListClient")
            .send()
            .post("/todolist")
            .contentType("application/x-www-form-urlencoded")
            .payload("title=" + todoName);

        designer.http()
            .client("todoListClient")
            .receive()
            .response(HttpStatus.FOUND);
    }
    
    [...]
}    
{% endhighlight %}

Configuration
---------

In order to enable Citrus Cucumber support we need to specify a special object factory in *cucumber.properties*.
    
    cucumber.api.java.ObjectFactory=cucumber.runtime.java.CitrusObjectFactory
    
The object factory takes care on creating all step definition instances. The object factory is able to inject *@CitrusResource*
annotated fields in step classes.
    
The usage of this special object factory is mandatory in order to combine Citrus and Cucumber capabilities. 
   
We also have the usual *citrus-context.xml* Citrus Spring configuration that is automatically loaded within the object factory.
So you can define and use Citrus components as usual within your test. In this sample we use a Http client component to call some
REST API on the [todo-list](/samples/todo-app/) application.    
                
Run
---------

You can run the sample on your localhost in order to see Citrus in action. Read the instructions [how to run](/samples/run/) the sample.