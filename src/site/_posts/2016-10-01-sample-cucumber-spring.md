---
layout: sample
title: Cucumber Spring sample
sample: sample-cucumber-spring
description: Shows BDD integration with Cucumber using Spring Framework injection
categories: [samples]
permalink: /samples/cucumber-spring/
---

The sample uses Cucumber behavior driven development (BDD) library. The tests combine BDD feature stories with the famous 
Gherkin syntax and Citrus integration test capabilities. Read about this feature in [reference guide](http://www.citrusframework.org/reference/html/#cucumber)
 
Objectives
---------

This sample application shows the usage of both Cucumber and Citrus in combination. The sample also uses Cucumber Spring
support in order to inject autowired beans to step definitions. The step definitions add *@ContextConfiguration(classes = CitrusSpringConfig.class)*
annotation in order to load the Citrus Spring application context with Cucumber Spring support.

All bean definitions ready for dependency injection using autowiring are located in the default Citrus Spring configuration file
*classpath:citrus-context.xml*. This way step definitions can use *@Autowired* annotations.

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
@ContextConfiguration(classes = CitrusSpringConfig.class)
public class TodoSteps {

    @CitrusResource
    private TestDesigner designer;

    @Autowired
    private HttpClient todoListClient;

    @Given("^Todo list is empty$")
    public void empty_todos() {
        designer.http()
                .client(todoListClient)
                .send()
                .delete("/todolist");

        designer.http()
                .client(todoListClient)
                .receive()
                .response(HttpStatus.FOUND);
    }

    @When("^I add entry \"([^\"]*)\"$")
    public void add_entry(String todoName) {
        designer.http()
                .client(todoListClient)
                .send()
                .post("/todolist")
                .contentType("application/x-www-form-urlencoded")
                .payload("title=" + todoName);

        designer.http()
                .client(todoListClient)
                .receive()
                .response(HttpStatus.FOUND);
    }
    
    [...]
}    
{% endhighlight %}
    
As you can see we are now able to use Spring **@Autowired** annotations in order to enable dependency injection. The **CitrusSpringConfig**
class is also loaded as Spring context configuration in order to load the Citrus default Spring application context.   

Configuration
---------

There are some configuration aspects that should be highlighted in particular. The sample uses Cucumber Spring support. Therefore
we have included the respective Maven dependency to the project:

{% highlight xml %}
<dependency>
  <groupId>io.cucumber</groupId>
  <artifactId>cucumber-spring</artifactId>
</dependency>
{% endhighlight %}
    
Secondly we choose Citrus Spring object factory in *cucumber.properties* in order to enable Cucumber Spring support in all tests.
    
    cucumber.api.java.ObjectFactory=cucumber.runtime.java.spring.CitrusSpringObjectFactory
    
These two steps are required to make Citrus work with Cucumber Spring features.

The object factory takes care on creating all step definition instances. The object factory is able to inject *@CitrusResource*
annotated fields in step classes.
    
The usage of this special object factory is mandatory in order to combine Citrus and Cucumber capabilities. 
   
We also have the usual *citrus-context.xml* Citrus Spring configuration that is automatically loaded within the object factory.
So you can define and use Citrus components as usual within your test. In this sample we use a Http client component to call some
REST API on the [todo-list](/samples/todo-app/) application.    
                
Run
---------

You can run the sample on your localhost in order to see Citrus in action. Read the instructions [how to run](/samples/run/) the sample.