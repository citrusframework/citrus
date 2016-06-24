---
layout: docs
title: Setup with Maven
permalink: /docs/setup-maven/
---

This quickstart shows you how to setup a new Citrus project with Maven. You will see it is very easy and you are finished 
within minutes.

### Preconditions

You need following software on your computer, in order to use the Citrus Framework:

- **Java 7 or higher**
  Installed JDK plus JAVA_HOME environment variable set up and pointing to your Java installation directory
- **Java IDE**
  A Java IDE will help you manage your Citrus project, create and execute test cases. Just use the Java IDE that you are 
  used to (e.g. [Eclipse](http://www.eclipse.org/) or [IntelliJ IDEA](http://www.jetbrains.com/idea/)).
- **Maven 3.0.x or higher**
  Citrus tests will be executed with the [Apache Maven](http://maven.apache.org) build tool. You can also run 
  tests via [ANT](http://ant.apache.org/) but Maven is my preferred way.
  
In case you already use Maven build tool in your project it is most suitable for you to include Citrus into your Maven build 
lifecycle. In this tutorial we will setup a project with Maven and configure the Maven POM to execute all Citrus tests 
during the Maven integration-test phase. First of all we create a new Java project called *citrus-sample*. We 
use the Maven command line tool in combination with Maven's archetype plugin. In case you do not have Maven installed yet 
it is time for you to do so before continuing this tutorial. See the [Maven](http://maven.apache.org) 
site for detailed installation instructions. So let's start with creating the Citrus Java project:

{% highlight shell %}
mvn archetype:generate -DarchetypeCatalog=http://citrusframework.org

[...]

Choose archetype:
1: http://citrusframework.org -> com.consol.citrus.mvn:citrus-quickstart (Citrus quickstart project)
2: http://citrusframework.org -> com.consol.citrus.mvn:citrus-quickstart-soap (Citrus quickstart project with SOAP client and server)
3: http://citrusframework.org -> com.consol.citrus.mvn:citrus-quickstart-jms (Citrus quickstart project with JMS consumer and producer)
Choose a number: 1 

Define value for groupId: com.consol.citrus.samples
Define value for artifactId: citrus-sample
Define value for version: 1.0-SNAPSHOT
Define value for package: com.consol.citrus.samples

[...]
{% endhighlight %}
    
In the sample above we used the Citrus archetype catalog located on the Citrus homepage. Citrus archetypes are also 
available in Maven central repository. So can also just use *mvn archetype:generate*. As the list of default 
archetypes available in Maven central is very long you might want to filter the list with *citrus* and you will 
get just a few possibilities to choose from.

Citrus provides custom Maven archetypes. We load the archetype information from *http://citrusframework.org* 
and choose the Citrus basic archetype. Now you have to define several values for your project: the groupId, the artifactId, 
the package and the project version. After that we are done! Maven created a Citrus project structure for us which is 
ready for testing. You should see the following basic project folder structure.

    citrus-sample
      |   + src
      |   |   + main
      |   |    |   + java
      |   |    |   + resources
      |   |   + citrus
      |   |    |   + java
      |   |    |   + resources
      |   |    |   + tests
      pom.xml
      
The Citrus project is absolutely ready for testing. With Maven we can build, package, install and test our project right 
away without any adjustments. Try to execute the following commands:

{% highlight shell %}
cd citrus-sample
mvn package
mvn integration-test
mvn install
{% endhighlight %}


Congratulations! You just have built the complete project and you also have executed the first Citrus tests in your 
project. The project comes with a sample Citrus test *SampleIT*. You can find this test in *src/it/tests*; 
and *src/it/java*. The Citrus test was automatically executed in the integration test phase in Maven project lifecycle.

The next step would be to import our project into our favorite IDE (e.g. Eclipse, IntelliJ or NetBeans). With Eclipse for 
instance you have to execute the following command:

{% highlight shell %}
mvn eclipse:eclipse
{% endhighlight %}

Now let's import the new Citrus project into the IDE and have a closer look at the basic project files that were generated 
for you. First of all open the Maven POM (pom.xml). You see the basic Maven project settings, all Citrus project dependencies 
as well as the ConSol* Labs Maven repositories here. In future you may add other project dependencies, Maven plugins in 
this file. For now you do not have to change the Citrus Maven settings in your project's POM, however we have a closer 
look at them:

First of all the ConSol* Labs Maven repositories. Maven will load new versions of Citrus from these servers.

{% highlight xml %}
<repositories>
  [...]

  <repository>
    <id>consol-labs-release</id>
    <url>http://labs.consol.de/maven/repository/</url>
    <snapshots>
      <enabled>false</enabled>
    </snapshots>
    <releases>
      <enabled>true</enabled>
    </releases>
  </repository>
  <repository>
    <id>consol-labs-snapshots</id>
    <url>http://labs.consol.de/maven/snapshots-repository/</url>
    <snapshots>
      <enabled>true</enabled>
      <!-- Policy: always, daily, interval:xxx (xxx=#minutes, 60*24*7=10080), never -->
      <updatePolicy>10080</updatePolicy>
    </snapshots>
    <releases>
      <enabled>false</enabled>
    </releases>
  </repository>
  
  [...]
</repositories>
{% endhighlight %}

{% highlight xml %}
<pluginRepositories>
  [...]

  <pluginRepository>
    <id>consol-labs-release</id>
    <url>http://labs.consol.de/maven/repository/</url>
    <snapshots>
      <enabled>false</enabled>
    </snapshots>
    <releases>
      <enabled>true</enabled>
    </releases>
  </pluginRepository>
  <pluginRepository>
    <id>consol-labs-snapshots</id>
    <url>http://labs.consol.de/maven/snapshots-repository/</url>
    <snapshots>
      <enabled>true</enabled>
      <updatePolicy>10080</updatePolicy>
    </snapshots>
    <releases>
      <enabled>false</enabled>
    </releases>
  </pluginRepository>
  
  [...]
</pluginRepositories>
{% endhighlight %}

Now we let's have a look at the Citrus project libraries as dependencies in our Maven POM.

{% highlight xml %}
<dependency>
  <groupId>com.consol.citrus</groupId>
  <artifactId>citrus-core</artifactId>
  <version>${project.version}</version>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>com.consol.citrus</groupId>
  <artifactId>citrus-jms</artifactId>
  <version>${project.version}</version>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>com.consol.citrus</groupId>
  <artifactId>citrus-ws</artifactId>
  <version>${project.version}</version>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>com.consol.citrus</groupId>
  <artifactId>citrus-http</artifactId>
  <version>${project.version}</version>
  <scope>test</scope>
</dependency>
{% endhighlight %}

The Citrus Maven plugin capable of test creation and report generation.

{% highlight xml %}
<plugin>
  <groupId>com.consol.citrus.mvn</groupId>
  <artifactId>citrus-maven-plugin</artifactId>
  <version>${project.version}</version>
  <configuration>
    <author>Mickey Mouse</author>
    <targetPackage>com.consol.citrus</targetPackage>
  </configuration>
</plugin>
{% endhighlight %}

The surefire plugin is responsible for executing all available tests in your project when you run the integration-test 
phase in Maven:
        
{% highlight xml %}
<plugin>
  <artifactId>maven-surefire-plugin</artifactId>
  <version>2.4.3</version>
  <configuration>
    <skip>true</skip>
  </configuration>
  <executions>
    <execution>
      <id>citrus-tests</id>
      <phase>integration-test</phase>
      <goals>
        <goal>test</goal>
      </goals>
      <configuration>
        <skip>false</skip>
      </configuration>
    </execution>
  </executions>
</plugin>        
{% endhighlight %}

Last not least the Citrus source directories defined as test sources for Maven:

{% highlight xml %}
<testSourceDirectory>src/it/java</testSourceDirectory>
<testResources>
  <testResource>
    <directory>src/it/resources</directory>
    <includes>
      <include>**/*</include>
    </includes>
    <filtering>true</filtering>
  </testResource>
  <testResource>
    <directory>src/it/tests</directory>
    <includes>
      <include>**/*.xml</include>
    </includes>
    <excludes>
    </excludes>
  </testResource>
</testResources>
{% endhighlight %}

Finally we are ready to proceed with creating new test cases. So let's add a new Citrus test case to our project. We use 
the Citrus Maven plugin here, just type the following command:

{% highlight shell %}
mvn citrus:create-test
Enter test name: MyTest
Enter test author: Unknown: : Christoph
Enter test description: TODO: Description: : 
Enter test package: com.consol.citrus.samples: : 
Choose unit test framework testng: :
{% endhighlight %}

You have to specify the test name, author, description, package and the test framework. The plugin successfully generates 
the new test files for you. On the one hand a new Java class in src/it/java and a new XML test file in src/it/tests. The 
test is runnable right now. Try it and execute &quot;mvn integration-test&quot; once more. In the Citrus test results you 
will see that the new test was executed during integration-test phase along with the other existing test case. You can 
also run the test manually in your IDE with a TestNG plugin.

So now you are ready to use Citrus! Write test cases and add more logic to the test project. Have fun with it!
