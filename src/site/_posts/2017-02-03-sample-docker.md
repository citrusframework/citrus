---
layout: sample
title: Docker sample
sample: sample-docker
description: Shows how to use Citrus within Docker
categories: [samples]
permalink: /samples/docker/
---

This sample uses [Docker](https://www.docker.com/) for containerized application infrastructure. Both the system under test [todo-list](/samples/todo-app/) application and 
the Citrus integration tests are run as Docker container. The Citrus tests are then able to use Docker networking and DNS features in order to access the exposed services on the
todo-list container. This way Citrus is able to participate on the Docker infrastructure for in-container testing.

The sample uses the [Fabric8 Docker Maven plugin](https://maven.fabric8.io/) for smooth Docker integration into the Maven build lifecylce.

Read about the Citrus Docker integration in the [reference guide](http://www.citrusframework.org/reference/html/#docker)

Objectives
---------

The [todo-list](/samples/todo-app/) sample application provides a REST API for managing todo entries. We want to access this REST API in a integration test scenario while the application is
run as Docker container. This means we need to build the todo-app as Docker image, run that image on Docker and start the integration tests. The Citrus tests
are built and run as container, too. The Citrus test container is then able to connect with the todo-list REST API via Docker networking between containers. Also
with Citrus Docker client integration we can check and manipulate the todo-app Docker container at test runtime.

The sample uses the Fabric8 Docker Maven plugin building and running the Docker images. So lets take a deep dive into the Maven POM configuration.
  
{% highlight xml %}
<plugin>
  <groupId>io.fabric8</groupId>
  <artifactId>docker-maven-plugin</artifactId>
  <version>${fabric8.plugin.version}</version>
  <configuration>
    <verbose>true</verbose>
    <images>
      <image>
        <alias>todo-app</alias>
        <name>citrus/todo-app:${project.version}</name>
        <build>
          <from>fabric8/tomcat-8:latest</from>
          <tags>
            <tag>latest</tag>
          </tags>
          <assembly>
            <inline>
              <files>
                <file>
                  <source>${settings.localRepository}/com/consol/citrus/samples/citrus-sample-todo/${project.version}/citrus-sample-todo-${project.version}.war</source>
                  <destName>ROOT.war</destName>
                  <outputDirectory>.</outputDirectory>
                </file>
              </files>
            </inline>
          </assembly>
        </build>
        <run>
          <namingStrategy>alias</namingStrategy>
          <env>
            <CATALINA_OPTS>-Xmx64m</CATALINA_OPTS>
          </env>
          <ports>
            <port>8080:8080</port>
          </ports>
          <wait>
            <http>
              <url>http://localhost:8080/todolist</url>
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
      <image>
        <alias>todo-app-tests</alias>
        <name>citrus/todo-app-tests:${project.version}</name>
        <build>
          <from>consol/citrus:2.7</from>
          <tags>
            <tag>latest</tag>
          </tags>
          <assembly>
            <descriptorRef>project</descriptorRef>
          </assembly>
        </build>
        <run>
          <namingStrategy>alias</namingStrategy>
          <volumes>
            <bind>
              <volume>/var/run/docker.sock:/var/run/dockerhost/docker.sock</volume>
            </bind>
          </volumes>
          <links>
            <link>todo-app</link>
          </links>
          <dependsOn>
            <dependsOn>todo-app</dependsOn>
          </dependsOn>
          <wait>
            <log>BUILD SUCCESS</log>
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

Wow that is lots of configuration. Let us understand this step by step. First of all we take a look at the todo-app system under test image build configuration:

{% highlight xml %}
<build>
  <from>fabric8/tomcat-8:latest</from>
  <tags>
    <tag>latest</tag>
  </tags>
  <assembly>
    <inline>
      <files>
        <file>
          <source>${settings.localRepository}/com/consol/citrus/samples/citrus-sample-todo/${project.version}/citrus-sample-todo-${project.version}.war</source>
          <destName>ROOT.war</destName>
          <outputDirectory>.</outputDirectory>
        </file>
      </files>
    </inline>
  </assembly>
</build>
{% endhighlight %}

The todolist application is a Java Spring Boot web application. So we want to deploy that WAR artifact on a Tomcat web server when running the application as Docker container. Fortunately the
Fabric8 team provides ready to use Docker images for such purpose. So we can extend our Docker image from *fabric8/tomcat-8:latest* image. These Docker images are well prepared to work with
artifacts that are assembled within Maven and especially those Maven POMs that use the Farbic8 Docker Maven plugin.
 
In the assembly section we define the target WAR that should be deployed to the Tomcat web server in the Docker image. That is very comfortable as the assembly can be done in various ways 
(also see [Fabric8 Docker assembly](https://dmp.fabric8.io/#build-assembly)). In this sample above we just copy the built WAR from our local Maven repository. 

Now let us move on to the Citrus tests. We want to also run the tests as Docker container so lets use another image configuration:

{% highlight xml %}
<build>
  <from>consol/citrus:2.7</from>
  <tags>
    <tag>latest</tag>
  </tags>
  <assembly>
    <descriptorRef>project</descriptorRef>
  </assembly>
</build>
{% endhighlight %}

This time we extend from *consol/citrus:2.7* Docker image which is ready to execute a Citrus Maven build at container runtime. The image also works with the Docker Maven plugin assembly
mechanism. This time the add the complete project assembly.

We can build the Docker images by calling:

{% highlight xml %}
mvn docker:build
{% endhighlight %}

Of course you need a running Docker installation on your host. After that you should see two new images built seconds ago:

{% highlight xml %}
docker images

REPOSITORY              TAG                 IMAGE ID            CREATED             SIZE
citrus/todo-app-tests   latest              e35695d5db2c        5 seconds ago       757.2 MB
citrus/todo-app         latest              b7f03a0e751b        5 seconds ago       452.2 MB
{% endhighlight %}

So we are now ready to run the images as containers in Docker. If you inspect the Docker Maven plugin configuration you will fin run sections that describe how the containers will be started.

{% highlight xml %}
<image>
    <alias>todo-app</alias>
    <name>citrus/todo-app:${project.version}</name>
    <run>
      <namingStrategy>alias</namingStrategy>
      <env>
        <CATALINA_OPTS>-Xmx64m</CATALINA_OPTS>
      </env>
      <ports>
        <port>8080:8080</port>
      </ports>
      <wait>
        <http>
          <url>http://localhost:8080/todolist</url>
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
{% endhighlight %}

When the todo-app container is started we expose port *8080* for clients. This is the default Tomcat port. In addition to that we tell the Docker Maven plugin
to wait for the application to start up. This is done by probing the http url *http://localhost:8080/todolist* with a *Http GET* request. Once the application is
started and Tomcat is ready we can start the Citrus test container.

{% highlight xml %}
<image>
    <alias>todo-app-tests</alias>
    <name>citrus/todo-app-tests:${project.version}</name>
    <run>
      <namingStrategy>alias</namingStrategy>
      <volumes>
        <bind>
          <volume>/var/run/docker.sock:/var/run/dockerhost/docker.sock</volume>
        </bind>
      </volumes>
      <links>
        <link>todo-app</link>
      </links>
      <dependsOn>
        <dependsOn>todo-app</dependsOn>
      </dependsOn>
      <wait>
        <log>BUILD SUCCESS</log>
        <time>60000</time>
        <shutdown>500</shutdown>
      </wait>
      <log>
        <enabled>true</enabled>
        <color>green</color>
      </log>
    </run>
 </image>
{% endhighlight %}

This time the configuration links the container to the *todo-app* container. This enabled the Docker networking feature where DNS host resolving will
be available for the Citrus test container. Exposed ports (8080) in the *todo-app* container are then accessible. The Citrus http client uses the following endpoint url
for accessing the REST API:

{% highlight java %}
@Bean
public HttpClient todoClient() {
    return CitrusEndpoints.http()
                        .client()
                        .requestUrl(String.format("http://%s:%s", todoServerHost, todoServerPort))
                        .build();
}
{% endhighlight %}

As you can see the client will be able to resolve the hostname *todo-app* via Docker networking feature. The test may then access the REST API over http in order to
add new todo items.

{% highlight java %}
http()
    .client(todoClient)
    .send()
    .post("/todolist")
    .messageType(MessageType.JSON)
    .contentType("application/json")
    .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}");

http()
    .client(todoClient)
    .receive()
    .response(HttpStatus.OK)
    .messageType(MessageType.PLAINTEXT)
    .payload("${todoId}");
{% endhighlight %}

In addition to that the Citrus configuration also defines a Docker client component:

{% highlight java %}
@Bean
public DockerClient dockerClient() {
    return CitrusEndpoints.docker()
            .client()
            .url("unix:///var/run/dockerhost/docker.sock")
            .build();
}
{% endhighlight %}

This client is then able to access the Docker API from within the Citrus test container in order to check the deployment state of the system under test.

{% highlight java %}
docker()
    .client(dockerClient)
    .inspectContainer("todo-app")
    .validateCommandResult((container, context) -> Assert.assertTrue(container.getState().getRunning()));
{% endhighlight %}
   
The test action above verifies that the *todo-app* container is up and running. We can also think of manipulating the Docker container. With the Docker Citrus
Java DSL we have full access to the Docker API. Please note that this is only applicable for testing purpose. In production environment the Docker API may
not be accessible to containers at all. In this local test environment we have mounted the Docker socket into the Citrus test container (`<volume>/var/run/docker.sock:/var/run/dockerhost/docker.sock</volume>`). 
This should definitely not possible on any production like environment.

However this is how we access other Docker containers from within the Citrus test container either by using the Docker socket or by using the normal exposed service
ports of the system under test.

No finally lets start the Docker containers with:

{% highlight xml %}
mvn docker:start
{% endhighlight %}

You will see that the Docker Maven plugin first of all is starting the *todo-app* Tomcat container with Spring Boot running. After that the Citrus test container is
started to perform all integration tests. You will see the Citrus logging output. The Docker Maven plugin waits for the *BUILD SUCCESS* log entry that marks that all tests
were executed successfully.

You can access the log output also by

{% highlight xml %}
docker logs todo-app-tests
{% endhighlight %}

Once the build is finished the Citrus test container is automatically stopped but the container is still there. You can see the container with:

{% highlight xml %}
docker ps -a

CONTAINER ID        IMAGE                       COMMAND                  CREATED             STATUS                     PORTS                                        NAMES
413b6c68076d        citrus/todo-app-tests:2.7   "/bin/sh -c 'mvn inst"   37 seconds ago      Exited (0) 7 seconds ago                                                todo-app-tests
f26583516453        citrus/todo-app:2.7         "/bin/sh -c /opt/tomc"   52 seconds ago      Up 46 seconds              8778/tcp, 0.0.0.0:8080->8080/tcp, 9779/tcp   todo-app
{% endhighlight %}

With `mvn docker:stop` we can stop and cleanup all containers.
 
Lets add a Maven profile that binds the plugin goals to the integration-test build phase for automated Docker build, setup, start, test and stop.
  
{% highlight xml %}
<profile>
  <id>docker</id>
  <properties>
    <test>skip</test>
    <it.test>skip</it.test>
  </properties>
  <build>
    <plugins>
      <plugin>
        <groupId>io.fabric8</groupId>
        <artifactId>docker-maven-plugin</artifactId>
        <executions>
          <execution>
            <id>start</id>
            <phase>pre-integration-test</phase>
            <goals>
              <goal>build</goal>
              <goal>start</goal>
            </goals>
          </execution>
          <execution>
            <id>stop</id>
            <phase>post-integration-test</phase>
            <goals>
              <goal>stop</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</profile>
{% endhighlight %}  

Now we are ready to call `mvn clean install -Pdocker` and everything is done automatically. We get a successful build when everything worked and
in case a Citrus test failed we get a failed build. This Maven build is perfectly working with continuous integration on Jenkins for instance.