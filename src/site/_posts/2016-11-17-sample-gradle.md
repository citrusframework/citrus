---
layout: sample
title: Gradle build sample
sample: sample-gradle
description: Uses Gradle build to execute tests
categories: [samples]
permalink: /samples/gradle/
---

This sample uses Gradle as build tool in order to execute the Citrus tests.

Objectives
---------

Citrus uses Maven internally for building software. But of course you can also integrate the Citrus tests in a Gradle
project. As the Citrus tests are nothing but normal JUnit or TestNG tests the integration in the Gradle build is very easy.

The Gradle build configuration is done in the **build.gradle** and **settings.gradle** files. Here we define the project name 
and the project version.

{% highlight shell %}
rootProject.name = 'citrus-sample-gradle'
group 'com.consol.citrus.samples'
version '2.6.3-SNAPSHOT'
{% endhighlight %}
    
Now as Citrus libraries are available on Maven central repository we add these repositories so Gradle knows how to download the required
Citrus artifacts.    
    
{% highlight shell %}
repositories {
    mavenCentral()
    maven {
        url 'http://labs.consol.de/maven/snapshots-repository/'
    }
}
{% endhighlight %}
    
Citrus stable release versions are available on Maven central. If you want to use the very latest next version as snapshot preview you need
to add the ConSol Labs snapshot repository which is optional. Now lets move on with adding the Citrus libraries to the project.
    
{% highlight shell %}
dependencies {
    testCompile group: 'com.consol.citrus', name: 'citrus-core', version: '2.6.3-SNAPSHOT'
    testCompile group: 'com.consol.citrus', name: 'citrus-java-dsl', version: '2.6.3-SNAPSHOT'
    testCompile group: 'org.testng', name: 'testng', version: '6.9.10'
    [...]
}
{% endhighlight %}
    
This enables the Citrus support for the project so we can use the Citrus classes and APIs. We decided to use TestNG unit test library.
    
{% highlight shell %}
test {
    useTestNG()
}
{% endhighlight %}
    
Of course JUnit is also supported. This is all for build configuration settings. We can move on to writing some Citrus integration tests. You can
find those tests in **src/test/java** directory.

This sample uses pure Java code for both Citrus configuration and tests. The
Citrus TestNG test uses a context configuration annotation.

{% highlight java %}
@ContextConfiguration(classes = { EndpointConfig.class })
{% endhighlight %}
    
This tells Spring to load the configuration from the Java class ***EndpointConfig***.
    
{% highlight java %}
public class EndpointConfig {
    @Bean
    public ChannelEndpoint testChannelEndpoint() {
        ChannelEndpointConfiguration endpointConfiguration = new ChannelEndpointConfiguration();
        endpointConfiguration.setChannel(testChannel());
        return new ChannelEndpoint(endpointConfiguration);
    }

    @Bean
    private MessageChannel testChannel() {
        return new MessageSelectingQueueChannel();
    }
}
{% endhighlight %}
    
In the configuration class we are able to define Citrus components for usage in tests. As usual
we can autowire the endpoint components as Spring beans in the test cases.
    
{% highlight java %}
@Autowired
private ChannelEndpoint testChannelEndpoint;
{% endhighlight %}
        
Run
---------

The sample application uses Gradle as build tool. So you can use the Gradle wrapper for compile, package and test the
sample with Gradle build.
 
    gradlew clean build
    
This executes all Citrus test cases during the build and you will see Citrus performing some integration test logging output.
After the tests are finished build is successful and you are ready to go for writing some tests on your own.

If you just want to execute all tests you can call

    gradlew clean check

Of course you can also start the Citrus tests from your favorite IDE.
Just start the Citrus test using the Gradle integration in IntelliJ, Eclipse or Netbeans.