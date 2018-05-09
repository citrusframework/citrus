---
layout: docs
title: Setup with Maven
permalink: /docs/setup-maven/
---

This quickstart shows you how to setup a new Citrus project with Maven. You will see it is very easy and you are finished 
within minutes.

### Preconditions

You need following software on your computer, in order to use the Citrus Framework:

- **Java 8 or higher**
  Installed JDK plus JAVA_HOME environment variable set up and pointing to your Java installation directory
- **Java IDE**
  A Java IDE will help you manage your Citrus project, create and execute test cases. Just use the Java IDE that you are 
  used to (e.g. [Eclipse](http://www.eclipse.org/) or [IntelliJ IDEA](http://www.jetbrains.com/idea/)).
- **Maven 3.0.x or higher**
  Citrus tests will be executed with the [Apache Maven](http://maven.apache.org) build tool.
  
In case you already use Maven build tool in your project it is most suitable for you to include Citrus into your Maven build 
lifecycle. In this tutorial we will setup a project with Maven and configure the Maven POM to execute all Citrus tests 
during the Maven integration-test phase. 

### Maven project

First of all we create a new Java project called *citrus-sample*. We 
use the Maven command line tool in combination with Maven's archetype plugin. In case you do not have Maven installed yet 
it is time for you to do so before continuing this tutorial. See the [Maven](http://maven.apache.org) 
site for detailed installation instructions. So let's start with creating the Citrus Java project:

{% highlight shell %}
mvn archetype:generate -Dfilter=com.consol.citrus.mvn:citrus

[...]

Choose archetype:
1: remote -> com.consol.citrus.mvn:citrus-quickstart (Citrus quickstart project)
2: remote -> com.consol.citrus.mvn:citrus-quickstart-jms (Citrus quickstart project with JMS consumer and producer)
3: remote -> com.consol.citrus.mvn:citrus-quickstart-soap (Citrus quickstart project with SOAP client and producer)
Choose a number: 1 

Define value for groupId: com.consol.citrus.samples
Define value for artifactId: citrus-sample
Define value for version: 1.0-SNAPSHOT
Define value for package: com.consol.citrus.samples

[...]
{% endhighlight %}
    
In the sample above we used the Citrus archetype available in Maven central repository. 
As the list of default archetypes available in Maven central is very long, it has been filtered for official Citrus archetypes.

After choosing the Citrus quickstart archetype you have to define several values for your project: the groupId, the artifactId, 
the package and the project version. After that we are done! Maven created a Citrus project structure for us which is 
ready for testing. You should see the following basic project folder structure.

    citrus-sample
      |   + src
      |   |   + main
      |   |    |   + java
      |   |    |   + resources
      |   |   + test
      |   |    |   + java
      |   |    |   + resources
      pom.xml
      
The Citrus project is absolutely ready for testing. With Maven we can build, package, install and test our project right 
away without any adjustments. The project comes with a sample Citrus test *SampleIT*. You can find this test in *src/test/resources*; 
and *src/test/java*. The Citrus test was automatically executed in the integration test phase in Maven project lifecycle.

The next step would be to import our project into our favorite IDE (e.g. Eclipse, IntelliJ or NetBeans). Please follow the instructions
of your IDE to integrate with Maven projects.

Now open the new Citrus project with the Java IDE and have a closer look at the project files that were generated 
for you. First of all open the Maven POM (pom.xml). You see the basic Maven project settings, all Citrus project dependencies 
as well as Maven plugin configurations. In future you may add new project dependencies and Maven plugins in 
this file. For now you do not have to change the Citrus Maven settings in your project POM, however we have a closer 
look at them:

The Citrus project libraries are loaded as dependencies in our Maven POM.

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

The surefire and failsafe plugin configuration is responsible for executing all available tests in your project. Unit tests are traditionally executed with
**maven-surefire**. Integration tests are executed with **maven-failsafe**. As Citrus tests are integration tests these tests will be executed in the Maven 
**integration-test** phase using **maven-failsafe**:
        
{% highlight xml %}
<plugin>
  <artifactId>maven-surefire-plugin</artifactId>
  <version>2.19.1</version>
  <configuration>
    <failIfNoTests>false</failIfNoTests>
    <workingDirectory>${project.build.directory}</workingDirectory>
  </configuration>
</plugin>

<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-failsafe-plugin</artifactId>
  <version>2.19.1</version>
  <executions>
    <execution>
      <id>integration-tests</id>
      <goals>
        <goal>integration-test</goal>
        <goal>verify</goal>
      </goals>
    </execution>
  </executions>
</plugin>        
{% endhighlight %}

The tests are separated by naming convention. All integration tests follow the ****/IT*.java** or ****/IT*.java** naming pattern. These tests are explicitly 
excluded from surefire plugin. This makes sure that the tests are not executed twice. Now you are ready to use both unit and integration tests in your Maven project.

Another notable POM configuration is the repository section. The Citrus stable release versions are available on Maven central repository. So Maven will automatically
download the artifacts from that servers. Now if you want to use the latest snapshot preview version of Citrus you need to add the ConSol Labs repository in your POM.

{% highlight xml %}
<repositories>
  [...]

  <repository>
    <id>consol-labs-snapshots</id>
    <url>http://labs.consol.de/maven/snapshots-repository/</url>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
    <releases>
      <enabled>false</enabled>
    </releases>
  </repository>
  
  [...]
</repositories>
{% endhighlight %}

Please note that this setting is optional and only applies for snapshot versions of Citrus.

### Run

The sample application uses Maven as build tool. So you can use the Maven commands to compile, package and test the
sample.

{% highlight shell %}
mvn clean install
{% endhighlight %}

Congratulations! You just have built the complete project and you also have executed the first Citrus tests in your 
project.
 
If you just want to execute the Citrus tests with a Maven build execute
 
{% highlight shell %}
mvn clean verify
{% endhighlight %} 

This executes all Citrus test cases during the build and you will see Citrus performing some integration test logging output. You can also execute
single test cases by defining the test name as Maven system property.

{% highlight shell %}
mvn clean verify -Dit.test=SampleIT
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
test is runnable right now. Try it and execute &quot;mvn clean verify&quot; once more. In the Citrus test results you 
will see that the new test was executed during integration-test phase along with the other existing test case. You can 
also run the test manually in your IDE with a TestNG plugin.

So now you are ready to use Citrus! Write test cases and add more logic to the test project. Have fun with it!
