---
layout: post
title: Introducing Citrus Admin Web UI
short-title: Admin UI
author: Christoph Deppisch
github: christophd
categories: [blog]
---

It has been a while since the last release in the [Citrus](http://christophd.github.io/citrus/) universe. It took us some time to get the new Citrus release [2.7.2](/news/2017/07/07/release-2-7-2/) ready for you. 
Of course we were not being lazy in that time. Besides the new Citrus 2.7.2 release we are proud to announce a new player in the Citrus team. The *Citrus administration UI* is a 
web-based user interface that helps you to manage your Citrus projects and test cases.

Often users complained about the complexity of having to learn all about Citrus and the Spring framework in particular as Citrus uses Spring for configuration and dependency injection. 
Especially non-developers had problems to master the learning curve for Citrus and Spring when starting to use the framework. Also people asked for a way to have a user interface for managing
components and tests.

We heard you and introduced a new administration user interface for Citrus! There is a detailed [Citrus Admin documentation](http://christophd.github.io/citrus-admin/) (which is still ongoing). 
However I would like to outline the main features of that web UI here in a short post for you.

## Download

You can start the web UI on your local machine with an executable web archive available at

https://labs.consol.de/maven/repository/com/consol/citrus/citrus-admin-web

Download the web archive to a local directory:

    curl -o citrus-admin.war https://labs.consol.de/maven/repository/com/consol/citrus/citrus-admin-web/1.0.0/citrus-admin-web-1.0.0-executable.war

Once loaded you can start the admin UI as Spring boot web application from command line like this:

    java -jar citrus-admin.war

You will see the application starting up. The web server should start within seconds. Once the application is up and running you can open your browser and point to [http://localhost:8080]().

The administration web UI is able to open any Citrus project on your local machine. When opened you can display the project information such as the latest test results. In addition to that you are able 
to view the Citrus components configured in the in the Spring application context. The web UI is also able to navigate to all test cases in your project. You can open the tests and execute them.
 
![project-dashboard.png](${site.path}/img/citrus-admin/project-dashboard.png)

## Edit configuration components

One of the major goals in the web UI is to give new users an easier way to get started with the Citrus Spring configuration. All configuration components get loaded from the Spring application context. 
You can view and edit those components such as Citrus endpoints via HTML forms:

![config-endpoints.png](${site.path}/img/citrus-admin/config-endpoints.png)

In case you add new components or save changes to configuration items the administration web UI directly changes the Spring configuration files on your local machine in that particular project. Of course you can
open the Spring configuration files in another editor (e.g. your favorite Java IDE) and review the changes made. In addition to that all configuration changes made from external editors are directly visible to the admin UI.

## Test management

You can see all available Citrus test cases in the opened project. The list of tests contains XML and Java DSL tests. 

![test-list.png](${site.path}/img/citrus-admin/test-list.png)

When opening a particular test case the UI will display the test details to you. This includes all test actions, source code, log output and the latest test results.

![test-info.png](${site.path}/img/citrus-admin/test-info.png)
![test-sources.png](${site.path}/img/citrus-admin/test-sources.png)

If you execute the test you will see the log output of that process and you will get a detailed access to all messages exchanged in that test run.

![test-execute.png](${site.path}/img/citrus-admin/test-execute.png)

As you can see the test log output is forwarded to your browser. Also the test progress and result (success or failure) is tracked by the administration UI. 
In the messages table you are able to review all messages (inbound/outbound) that were part of the test run.

![test-messages.png](${site.path}/img/citrus-admin/test-messages.png)

## Reporting

The administration UI is able to read the test results in your project. Typically these are JUnit or TestNG reports that are generated from each test run. If present the UI will read and display detailed
test results of the latest test run.

![test-report.png](${site.path}/img/citrus-admin/test-report.png)

When a test case is failing for some reason exception and failure information will be provided.

![test-results.png](${site.path}/img/citrus-admin/test-results.png)

The administration UI aims to give you an additional tooling for Citrus integration testing. The administration web UI is not there to eliminate your favorite IDE (IntelliJ, Eclipse or whatever)! The UI is a helping 
instrument for getting in touch with Citrus and its concepts and works side by side with your local Java IDE as well as other text editors of your choice.
                                                                                             
Also the UI is helpful when executing the Citrus integration tests in different stages (test, acceptance, explorative) of your release process. There is not always a full capable development environment available for 
executing integration tests. You can run the Citrus administrative UI as Docker container or Kubernetes pod in order to make the tests portable to your containerized test environment.

## Docker image

The Citrus administration UI is available as [Docker](https://www.docker.com/) image. So you can easily load and run the web UI as Docker container.

    docker run -d -p 8080:8080 -v $PWD:/maven -e CITRUS_ADMIN_PROJECT_HOME=/maven consol/citrus-admin:1.0.0
     
The command above loads the Docker image and runs a new Citrus web UI container. The container is provided with a volume mount that makes the current directory accessible from within the container.
This current directory is then used as project home so the admin UI will automatically open the Citrus project from that directory. Once the container is running
you can point your local browser to [http://localhost:8080]() as usual.
     
The Citrus admin Docker image also works fine with the Fabric8 [docker-maven-plugin](http://github.com/fabric8io/docker-maven-plugin). So you can add the following image configuration to your Citrus Maven project:

{% highlight xml %}
  <plugin>
    <groupId>io.fabric8</groupId>
    <artifactId>docker-maven-plugin</artifactId>
    <version>0.21.0</version>
    <configuration>
      <verbose>true</verbose>
      <images>
        <image>
          <alias>citrus-tests</alias>
          <name>sample-app/citrus-tests:${project.version}</name>
          <build>
            <from>consol/citrus-admin:1.0.0</from>
            <assembly>
              <descriptorRef>project</descriptorRef>
            </assembly>
          </build>
          <run>
            <namingStrategy>alias</namingStrategy>
            <env>
              <CITRUS_ADMIN_PROJECT_HOME>/maven</CITRUS_ADMIN_PROJECT_HOME>
            </env>
            <ports>
              <port>8080:8080</port>
            </ports>
            <wait>
              <http>
                <url>http://localhost:8080</url>
                <method>GET</method>
                <status>200</status>
              </http>
              <time>60000</time>
              <shutdown>500</shutdown>
            </wait>
            <log>
              <enabled>true</enabled>
              <color>green</color>
            </log>
          </run>
        </image>
      </images>
    </configuration>
  </plugin>    
{% endhighlight %}

With that configuration you can just call:
  
    mvn docker:build
    mvn docker:start
     
This loads and builds the Docker image and starts a new Docker container with running Citrus Admin UI pointing to that very same Maven project.    
     
Stopping the container is as easy as calling:
   
   mvn docker:stop
   
This is a very comfortable way to build and ship a Citrus admin UI container with your project. You can deploy the tests to any Docker environment or even use
the container in Kubernetes as pod.

## What's next!?

Now it is your turn! Open your Citrus project with that web UI and tell us how you like it! There are many different approaches to using Citrus in a development project. 
We tried to cover all aspects and we are sure that the web UI is able to read most of the Citrus project out there. In case there is a time when the web UI is not able to 
read your project for some reason please tell us. When there is something wrong or simply not working out for you just open an issue on github. 

We are keen to answer your questions and discuss any doubts and we are looking forward to receive your feedback! 





 