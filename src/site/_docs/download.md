---
layout: docs
title: Download
permalink: /docs/download/
---

Citrus ${project.version} is the latest stable release . You may also go for the [latest snapshot versions](#use-latest-snapshots) 
of Citrus always being up to date with development changes. Citrus is available on [Maven central repository](http://search.maven.org/#search%7Cga%7C1%7Ccom.consol.citrus) 
so you can add Citrus as [Maven dependency](#maven) to your project. All available versions and production releases for 
manual download are listed below:

## Release artifacts

| Version | Artifact | Sources |
|:--------|:--------|:--------|
| ${project.version} | Release [zip](${site.url}/dist/citrus-${project.version}-release.zip)/[tar.gz](${site.url}/dist/citrus-${project.version}-release.tar.gz) | Sources [zip](${site.url}/dist/citrus-${project.version}-src.zip)/[tar.gz](${site.url}/dist/citrus-${project.version}-src.tar.gz) |
| 2.6 | Release [zip](${site.url}/dist/citrus-2.6-release.zip)/[tar.gz](${site.url}/dist/citrus-2.6-release.tar.gz) | Sources [zip](${site.url}/dist/citrus-2.6-src.zip)/[tar.gz](${site.url}/dist/citrus-2.6-src.tar.gz) |
| 2.5 | Release [zip](${site.url}/dist/citrus-2.5.2-release.zip)/[tar.gz](${site.url}/dist/citrus-2.5.2-release.tar.gz) | Sources [zip](${site.url}/dist/citrus-2.5.2-src.zip)/[tar.gz](${site.url}/dist/citrus-2.5.2-src.tar.gz) |
| 2.4 | Release [zip](${site.url}/dist/citrus-2.4-release.zip)/[tar.gz](${site.url}/dist/citrus-2.4-release.tar.gz) | Sources [zip](${site.url}/dist/citrus-2.4-src.zip)/[tar.gz](${site.url}/dist/citrus-2.4-src.tar.gz) |
| 2.3 | Release [zip](${site.url}/dist/citrus-2.3-release.zip)/[tar.gz](${site.url}/dist/citrus-2.3-release.tar.gz) | Sources [zip](${site.url}/dist/citrus-2.3-src.zip)/[tar.gz](${site.url}/dist/citrus-2.3-src.tar.gz) |
| 2.2 | Release [zip](${site.url}/dist/citrus-2.2-release.zip)/[tar.gz](${site.url}/dist/citrus-2.2-release.tar.gz) | Sources [zip](${site.url}/dist/citrus-2.2-src.zip)/[tar.gz](${site.url}/dist/citrus-2.2-src.tar.gz) |
| 2.1 | Release [zip](${site.url}/dist/citrus-2.1-release.zip)/[tar.gz](${site.url}/dist/citrus-2.1-release.tar.gz) | Sources [zip](${site.url}/dist/citrus-2.1-src.zip)/[tar.gz](${site.url}/dist/citrus-2.1-src.tar.gz) |
| 2.0 | Release [zip](${site.url}/dist/citrus-2.0-release.zip)/[tar.gz](${site.url}/dist/citrus-2.0-release.tar.gz) | Sources [zip](${site.url}/dist/citrus-2.0-src.zip)/[tar.gz](${site.url}/dist/citrus-2.0-src.tar.gz) |
| 1.4 | Release [zip](${site.url}/dist/citrus-1.4.1-release.zip)/[tar.gz](${site.url}/dist/citrus-1.4.1-release.tar.gz) | Sources [zip](${site.url}/dist/citrus-1.4.1-src.zip)/[tar.gz](${site.url}/dist/citrus-1.4.1-src.tar.gz) |
| 1.3 | Release [zip](${site.url}/dist/citrus-1.3.1-release.zip)/[tar.gz](${site.url}/dist/citrus-1.3.1-release.tar.gz) | Sources [zip](${site.url}/dist/citrus-1.3.1-src.zip)/[tar.gz](${site.url}/dist/citrus-1.3.1-src.tar.gz) |
| 1.2 | Release [zip](${site.url}/dist/citrus-1.2-release.zip)/[tar.gz](${site.url}/dist/citrus-1.2-release.tar.gz) | Sources [zip](${site.url}/dist/citrus-1.2-src.zip)/[tar.gz](${site.url}/dist/citrus-1.2-src.tar.gz) |

The Citrus project requires Java 7 (or newer version) to run.

## Maven 

You can easily use Citrus in a Maven project by defining test-scoped dependencies. Simply add the ConSol Labs repository 
and the following dependencies to your POM (pom.xml). See also our Maven tutorial for a detailed description.

The Citrus core module dependency.

{% highlight xml %}
<dependency>
  <groupId>com.consol.citrus</groupId>
  <artifactId>citrus-core</artifactId>
  <version>${project.version}</version>
  <scope>test</scope>
</dependency>
{% endhighlight %}

In case you need Citrus modules add following dependencies. See also our modules section for more information on Citrus modules:

{% highlight xml %}
<dependency>
  <groupId>com.consol.citrus</groupId>
  <artifactId>citrus-jms</artifactId>
  <version>${project.version}</version>
  <scope>test</scope>
</dependency>

<dependency>
  <groupId>com.consol.citrus</groupId>
  <artifactId>citrus-http</artifactId>
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
  <artifactId>citrus-websocket</artifactId>
  <version>${project.version}</version>
  <scope>test</scope>
</dependency>

<dependency>
  <groupId>com.consol.citrus</groupId>
  <artifactId>citrus-camel</artifactId>
  <version>${project.version}</version>
  <scope>test</scope>
</dependency>

<dependency>
  <groupId>com.consol.citrus</groupId>
  <artifactId>citrus-ssh</artifactId>
  <version>${project.version}</version>
  <scope>test</scope>
</dependency>

<dependency>
  <groupId>com.consol.citrus</groupId>
  <artifactId>citrus-vertx</artifactId>
  <version>${project.version}</version>
  <scope>test</scope>
</dependency>
{% endhighlight %}

If you would like to use the new Java DSL test writing language you have to add this dependency to your project accordingly.

{% highlight xml %}
<dependency>
  <groupId>com.consol.citrus</groupId>
  <artifactId>citrus-java-dsl</artifactId>
  <version>${project.version}</version>
  <scope>test</scope>
</dependency>
{% endhighlight %}

## Use latest snapshots

Stable releases are available on Maven central repository. We also provide nightly snapshot releases that are available on
ConSol Labs repository. So if you want to use latest snapshot releases of Citrus please add the following repository to 
your Maven POM.

{% highlight xml %}
<repository>
  <id>consol-labs-snapshots</id>
  <url>http://labs.consol.de/maven/snapshots-repository/</url>
  <snapshots>
    <enabled>true</enabled>
    <updatePolicy>10080</updatePolicy>
  </snapshots>
  <releases>
    <enabled>false</enabled>
  </releases>
</repository>
{% endhighlight %}

## Logging framework notice

We use [SLF4J](http://www.slf4j.org/) as logging abstraction framework, which means that you as a user are not forced to use a specific logging 
implementation. SLF4J is similar to commons-logging, so you may use whatever logging framework you want to. All you have
to do is add an SLF4J logging implementation to your classpath.

In case you are currently using [log4j](http://logging.apache.org/log4j) as logging framework just include slf4j-log4j12.jar on your classpath and Citrus 
will use log4j too. If you want to use some other framework than please see the [SLF4J](http://www.slf4j.org/) documentation for help.