---
layout: sample
title: Annotation configuration sample
sample: sample-annotation-config
description: Use Java annotation configuration
categories: [samples]
permalink: /samples/annotation-config/
---

This sample uses on-the-fly Java annotation configuration for endpoints.

Objectives
---------

Usually the Citrus endpoint components are configured in a central Spring application context. However there is another
possibility to configure endpoint components per test case with annotations.

This sample uses Java annotations for adding Citrus endpoint configuration in tests. The test therefor uses a member
variable that is annotated with `@CitrusEndpoint` annotation in combination with `@HttpClientConfig` annotation.
    
This tells Citrus to create a new endpoint for this test class.

{% highlight java %}
@CitrusEndpoint
@HttpClientConfig(requestUrl = "http://localhost:8080")
private HttpClient todoClient;
{% endhighlight %}
    
In contrast to adding the bean to the Spring application context we define the endpoint using annotation configurations. As usual we are
able to reference this endpoint in any send and receive operation in Citrus Java fluent API.

{% highlight java %}
http()
    .client(todoClient)
    .send()
    .get("/todolist")
    .accept("text/html");
{% endhighlight %}
        
Citrus automatically injects the endpoint with respective configuration for `requestUrl = http://localhost:8080`. You can use this endpoint
within all test methods in this class.

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