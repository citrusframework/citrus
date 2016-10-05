---
layout: sample
title: Java config sample
sample: sample-javaconfig
description: Uses pure Java POJOs for configuration
categories: [samples]
permalink: /samples/javaconfig/
---

This sample uses pure Java POJOs as configuration.

Objectives
---------

Citrus uses Spring Framework as glue for everything. Following from that Citrus components are
defined as Spring beans in an application context. You can use XML
configuration files and you can also use Java POJOs.

This sample uses pure Java code for both Citrus configuration and tests. The
Citrus TestNG test uses a context configuration annotation.

{% highlight java %}
@ContextConfiguration(classes = { EndpointConfig.class })
{% endhighlight %}
    
This tells Spring to load the configuration from the Java class ***EndpointConfig***.

{% highlight java %}
@Bean
public HttpClient todoListClient() {
    return CitrusEndpoints.http()
                .client()
                .requestUrl("http://localhost:8080")
                .build();
}
{% endhighlight %}
    
In the configuration class we are able to define Citrus components for usage in tests. As usual
we can autowire the Http client component as Spring bean in the test cases.
  
{% highlight java %}
@Autowired
private HttpClient todoListCLient;
{% endhighlight %}
     
Secondly we can use the ***CitrusEndpoint*** annotation to automatically create a new endpoint component in a test.
    
{% highlight java %}
@CitrusEndpoint
@HttpClientConfig(requestUrl = "http://localhost:8080")
private HttpClient todoClient;
{% endhighlight %}
    
In contrast to adding the bean to the Spring application context we define the endpoint using annotation configurations.    
                
Run
---------

You can run the sample on your localhost in order to see Citrus in action. Read the instructions [how to run](/samples/run/) the sample.