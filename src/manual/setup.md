## Setup

This chapter discusses how to get started with Citrus. It deals with the installation and set up of the framework,
so you are ready to start writing test cases after reading this chapter. 

Usually you would use Citrus as a dependency library in your project. In Maven you would just add Citrus as a test-scoped
dependency in your POM. When using ANT you can also run Citrus as normal Java application from your build.xml. As Citrus tests
are nothing but normal unit tests you could also use JUnit or TestNG ant tasks to execute the Citrus test cases.

This chapter describes the Citrus project setup possibilities, choose one of them that fits best to include Citrus into
your project.

### Using Maven

Citrus uses [http://maven.apache.org/](Maven) internally as a project build tool and provides extended
support for Maven projects. Maven will ease up your life as it manages project dependencies and provides extended build life cycles and 
conventions for compiling, testing, packaging and installing your Java project. Therefore it is recommended to use the Citrus Maven 
project setup. In case you already use Maven it is most suitable for you to include Citrus as a test-scoped dependency.
  
As Maven handles all project dependencies automatically you do not need to download any Citrus project artifacts in 
advance. If you are new to Maven please refer to the official Maven documentation to find out how to set up a Maven project. 
  
#### Use Citrus Maven archetype

If you start from scratch or in case you would like to have Citrus operating in a separate Maven module you can use the Citrus 
Maven archetype to create a new Maven project. The archetype will setup a basic Citrus project structure with basic settings and files.

```
mvn archetype:generate -DarchetypeCatalog=http://citrusframework.org

Choose archetype:
1: http://citrusframework.org -> citrus-archetype (Basic archetype for Citrus integration test project)
Choose a number: 1 

Define value for groupId: com.consol.citrus.samples
Define value for artifactId: citrus-sample
Define value for version: 1.0-SNAPSHOT
Define value for package: com.consol.citrus.samples
```

In the sample above we used the Citrus archetype catalog located on the Citrus homepage. Citrus archetypes
are also available in Maven central repository. So can also just use **"mvn archetype:generate"**. As the list of default
archetypes available in Maven central is very long you might want to filter the list with **"citrus"** and you will get just a few
possibilities to choose from.

We load the archetype information from "http://citrusframework.org" and choose the Citrus basic archetype. Now you have 
to define several values for your project: the groupId, the artifactId, the package and the project version. After that we are done! Maven created a Citrus project structure
for us which is ready for testing. You should see the following basic project folder structure.

```
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
```

The Citrus project is absolutely ready for testing. With Maven we can build, package, install and test our project right away without any adjustments. 
Try to execute the following commands:

```
mvn integration-test 
mvn integration-test -Dtest=MyFirstCitrusTest 
```

    If you need additional assistance in setting up a Citrus Maven project please visit our Maven setup tutorial
    on [http://www.citrusframework.org/tutorials.html](http://www.citfrusframework.org)
  
#### Add Citrus to existing Maven project
In case you already have a proper Maven project you can also integrate Citrus with it. Just add the Citrus project dependencies in your Maven pom.xml 
as a dependency like follows.

- We add Citrus as test-scoped project dependency to the project POM (pom.xml)
  
```xml
<dependency>
    <groupId>com.consol.citrus</groupId>
    <artifactId>citrus-core</artifactId>
    <version>2.7-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

- In case you would like to use the Citrus Java DSL also add this dependency to the project

```xml
<dependency>
    <groupId>com.consol.citrus</groupId>
    <artifactId>citrus-java-dsl</artifactId>
    <version>2.7-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```
    
- Add the citrus Maven plugin to your project
  
```xml
<plugin>
    <groupId>com.consol.citrus.mvn</groupId>
    <artifactId>citrus-maven-plugin</artifactId>
    <version>2.7-SNAPSHOT</version>
    <configuration>
      <author>Donald Duck</author>
      <targetPackage>com.consol.citrus</targetPackage>
    </configuration>
</plugin>
```    

Now that we have added Citrus to our Maven project we can start writing new test cases with the Citrus Maven
plugin:

```
mvn citrus:create-test
```
    
Once you have written the Citrus test cases you can execute them automatically in your Maven software build lifecylce. 
The tests will be included into your projects integration-test phase using the Maven surefire plugin. Here is a sample
surefire configuration for Citrus.
  
```xml
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
```

The Citrus source directories are defined as test sources like follows:
  
```xml
<testSourceDirectory>src/it/java</testSourceDirectory>
<testResources>
<testResource>
  <directory>src/it/java</directory>
  <includes>
    <include>**</include>
  </includes>
  <excludes>
    <exclude>*.java</exclude>
  </excludes>
</testResource>
<testResource>
  <directory>src/it/tests</directory>
  <includes>
    <include>**/*</include>
  </includes>
  <excludes>
  </excludes>
</testResource>
</testResources>
```
  
Now everything is set up and you can call the usual Maven **install** goal (mvn clean install) 
in order to build your project. The Citrus integration tests are executed automatically during the build process. 
Besides that you can call the Maven integration-test phase explicitly to execute all Citrus tests or a specific test 
by its name:

```
mvn integration-test 
mvn integration-test -Dtest=MyFirstCitrusTest 
```

    If you need additional assistance in setting up a Citrus Maven project please visit our Maven setup tutorial
    on [http://www.citrusframework.org/tutorials.html](http://www.citfrusframework.org)

### Using Ant

Ant is a very popular way to compile, test, package and execute Java projects. The Apache project has 
effectively become a standard in building Java projects. You can run Citrus test cases with Ant as Citrus is nothing but a
Java application. This section describes the steps to setup a proper Citrus Ant project.

#### Preconditions
    
Before we start with the Citrus setup be sure to meet the following preconditions. The following software should
be installed on your computer, in order to use the Citrus framework:

- Java 7 or higher
  Installed JDK plus JAVA_HOME environment variable set up and pointing
  to your Java installation directory
  
- Java IDE (optional)
  A Java IDE will help you to manage your Citrus project (e.g. creating 
  and executing test cases). You can use the any Java IDE (e.g. Eclipse or IntelliJ IDEA) but also
  any convenient XML Editor to write new test cases.
  
- Ant 1.8 or higher
  Ant ([http://ant.apache.org/](http://ant.apache.org/)) will run tests and 
  compile your Citrus code extensions if necessary.

#### Download
    
First of all we need to download the latest Citrus release archive from the official website 
[http://www.citrusframework.org](http://www.citrusframework.org)
    
Citrus comes to you as a zipped archive in one of the following packages:
    
- **citrus-x.x-release**
- **citrus-x.x-src**
    
The release package includes the Citrus binaries as well as the reference documentation and some sample applications.
    
In case you want to get in touch with developing and debugging Citrus you can also go with the source archive 
which gives you the complete Citrus Java code sources. The whole Citrus project is also accessible for you on 
[http://github.com/christophd/citrus](http://github.com/christophd/citrus). This open git repository 
on GitHub enables you to build Citrus from scratch with Maven and contribute code changes.

#### Installation

After downloading the Citrus archives we extract those into an appropriate location on the local 
storage. We are seeking for the Citrus project artifacts coming as normal Java archives (e.g. citrus-core.jar,
citrus-ws.jar, etc.)
    
You have to include those Citrus Java archives as well as all dependency libraries to your Apache Ant Java classpath. Usually
you would copy all libraries into your project's lib directory and declare those libraries in the Ant build file. As this approach
can be very time consuming I recommend to use a dependency management API such as Apache Ivy which gives you automatic dependency
resolution like that from Maven. In particular this comes in handy with all the 3rd party dependencies that would be resolved
automatically.

No matter what approach you are using to set up the Apache Ant classpath see the following sample Ant build script which uses
the Citrus project artifacts in combination with the TestNG Ant tasks to run the tests.
    
```xml
<project name="citrus-sample" basedir="." default="citrus.run.tests" xmlns:artifact="antlib:org.apache.maven.artifact.ant">

    <property file="src/it/resources/citrus.properties"/>
    
    <path id="maven-ant-tasks.classpath" path="lib/maven-ant-tasks-2.1.3.jar" />
    <typedef resource="org/apache/maven/artifact/ant/antlib.xml"
      uri="antlib:org.apache.maven.artifact.ant"
      classpathref="maven-ant-tasks.classpath" />
    
    <artifact:pom id="citrus-pom" file="pom.xml" />
    <artifact:dependencies filesetId="citrus-dependencies" pomRefId="citrus-pom" />
    
    <path id="citrus-classpath">
      <pathelement path="src/it/java"/>
      <pathelement path="src/it/resources"/>
      <pathelement path="src/it/tests"/>
      <fileset refid="citrus-dependencies"/>
    </path>
    
    <taskdef resource="testngtasks" classpath="lib/testng-6.8.8.jar"/>
    
    <target name="compile.tests">
      <javac srcdir="src/it/java" classpathref="citrus-classpath"/>
      <javac srcdir="src/it/tests" classpathref="citrus-classpath"/>
    </target>
    
    <target name="create.test" description="Creates a new empty test case">
      <input message="Enter test name:" addproperty="test.name"/>
      <input message="Enter test description:" addproperty="test.description"/>
      <input message="Enter author's name:" addproperty="test.author" defaultvalue="${default.test.author}"/>
      <input message="Enter package:" addproperty="test.package" defaultvalue="${default.test.package}"/>
      <input message="Enter framework:" addproperty="test.framework" defaultvalue="testng"/>
    
      <java classname="com.consol.citrus.util.TestCaseCreator">
        <classpath refid="citrus-classpath"/>
        <arg line="-name ${test.name} -author ${test.author} -description ${test.description} -package ${test.package} -framework ${test.framework}"/>
      </java>
    </target>
    
    <target name="citrus.run.tests" depends="compile.tests" description="Runs all Citrus tests">
      <testng classpathref="citrus-classpath">
        <classfileset dir="src/it/java" includes="**/*.class" />
      </testng>
    </target>
    
    <target name="citrus.run.single.test" depends="compile.tests" description="Runs a single test by name">
      <touch file="test.history"/>
      <loadproperties srcfile="test.history"/>
    
      <echo message="Last test executed: ${last.test.executed}"/>
      <input message="Enter test name or leave empty for last test executed:" addproperty="testclass" defaultvalue="${last.test.executed}"/>
    
      <propertyfile file="test.history">
        <entry key="last.test.executed" type="string" value="${testclass}"/>
      </propertyfile>
    
      <testng classpathref="citrus-classpath">
        <classfileset dir="src/it/java" includes="**/${testclass}.class" />
      </testng>
    </target>

</project>
```
    
    If you need detailed assistance for building Citrus with Ant do also visit our tutorials section on 
    [http://www.citrusframework.org](http://www.citrusframework.org). There you can find a 
    tutorial which describes the Citrus Java project set up with Ant from scratch.
    
  
  

