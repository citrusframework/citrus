---
layout: docs
title: Development
permalink: /docs/development/
---

This quickstart gets the Citrus project sources running in a few minutes, starting with initial git repository clone and 
ending with a built Citrus project ready for coding.

## Preconditions

- **[Git](http://git-scm.com/)**
  This can be either a command line client or some graphical UI. For simplicity, we assume you have the command line client installed.
- **Java 7 (or higher version)**
  You can verify the Java installation via command line with

{% highlight shell %}  
java -version
java version &quot;1.7.0_xx&quot;
Java(TM) 2 Runtime Environment, Standard Edition (build 1.7.0_xx)
Java HotSpot(TM) Client VM (build 1.7.0_xx, mixed mode, sharing)</pre>
{% endhighlight %}
  
- **Maven 3.0.x (or higher version)**
  Download <a href="http://maven.apache.org">maven</a> and install Maven on your machine. Please verify correct version and **MAVEN_HOME** setup using following command
          
{% highlight shell %}  
mvn -version
Maven version: 3.0.x
Java version: 1.7.0_xx
Default locale: en_US, platform encoding: MacRoman
OS name: &quot;mac os x&quot; version: &quot;10.9&quot; arch: &quot;x86_64&quot; Family: &quot;mac&quot;</pre>
{% endhighlight %}

## Initial git clone

First of all we get the Citrus sources from the repository on [GitHub](http://www.github.com/). You can use the following command to do this

{% highlight shell %}  
git clone git://github.com/christophd/citrus.git
{% endhighlight %}

This will clone the Citrus project to the target directory **citrus**. In the following this project directory is referred 
to as **PROJECT_HOME**. For detailed instructions about the version control system git, please consult the [official git 
website](http://git-scm.com/).

## Build the Citrus artifacts

Now everything is setup properly and you can use Maven for all the rest:

{% highlight shell %}  
mvn install
{% endhighlight %}

This command runs the full Maven build lifecycle with compilation, testing, packaging and installation of all artifacts. 
You will find the freshly built Citrus JAR files in your local Maven repository. Using this new own Citrus version is 
quite simple. Just add the SNAPSHOT dependency to your projects POM like this

{% highlight shell %}  
<dependency>
  <groupId>com.consol.citrus</groupId>
  <artifactId>citrus-core</artifactId>
  <version>${project.version}</version>
  <scope>test</scope>
</dependency>
{% endhighlight %}

## Create IDE project files

You can easily create the project files for your favorite IDE (IntelliJ IDEA, Eclipse or Netbeans). In your **<PROJECT_HOME>** call

{% highlight shell %}
mvn idea:idea
mvn eclipse:eclipse
mvn netbeans:netbeans
{% endhighlight %}

The project files are now ready for import in your IDE. This is the preferred way for creating IDE project files in Maven. 
Please do not create IDE projects manually. Maven takes care of the whole project classpath construction.

Make sure that you have set the **M2_REPO** classpath variable set in Eclipse (or Netbeans). The variable value points to 
your local Maven repository (typically found in *C:\Documents and Settings\username\.m2\repo* or *~/.m2/repo*).

Maven ofers a suitable command to do this automatically:

{% highlight shell %}
mvn -Declipse.workspace=<path-to-eclipse-workspace> eclipse:add-maven-repo 
{% endhighlight %}

## What's next?

Have fun with Citrus!
