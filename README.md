Citrus Integration Testing ![Logo][1]
==============

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.citrusframework/citrus/badge.svg?style=flat-square)](https://central.sonatype.com/search?q=g%253Aorg.citrusframework)
[![build](https://github.com/citrusframework/citrus/workflows/build/badge.svg?branch=main)](https://github.com/citrusframework/citrus/actions) 
[![Javadocs](http://javadoc.io/badge/org.citrusframework/citrus-core.svg)](http://javadoc.io/doc/org.citrusframework/citrus-core)
[![Licensed under Apache License version 2.0](https://img.shields.io/github/license/openshift/origin.svg?maxAge=2592000)](https://www.apache.org/licenses/LICENSE-2.0")
[![Chat on Zulip](https://img.shields.io/badge/zulip-join_chat-brightgreen.svg)](https://citrusframework.zulipchat.com)

Welcome to Citrus
---------

Citrus is a test framework written in Java that provides a complete test automation tool for integration testing of message-based enterprise applications. 

The framework supports a huge set of different message transports and protocols like Http REST, Kafka, JMS, TCP/IP, FTP, SOAP Web Services.

In addition to that Citrus integrates with many technologies and libraries such as Apache Camel, Spring, Quarkus, Testcontainers, Kubernetes, Knative, Selenium and many more.

For validation purpose Citrus is able to verify many different message data formats and protocols such as XML, Json, YAML, CSV, Plaintext, SQL ResultSet and more.

Visit the official website at 'https://citrusframework.org'
for more information and a detailed documentation.
 
Quickstart
---------

Learn how to create and run Citrus integration tests in just a few minutes. You can choose from a set of supported runtimes (JUnit, TestNG, Quarkus, SpringBoot, JBang) and domain specific languages (Java, XML, YAML, Groovy, Cucumber).

For a full guide how to get started with Citrus please visit the [quickstart section](https://citrusframework.org/quickstart/) on the official website.

The easiest way to create and run a Citrus test without a project setup is to use the Citrus JBang support.

You can just call this command to create a new test.

```shell
jbang citrus@citrusframework/citrus init MyTest.java
```

The `init` command creates a new file that represents a Citrus test. The file extension used defines the test source language. You can choose one of the supported test source languages `.java`, `.xml`, `.yaml`, `.groovy`, `.feature`.

The result looks like this:

```java
import org.citrusframework.TestActionSupport;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;

public class MyTest implements Runnable, TestActionSupport {

    @CitrusResource
    TestCaseRunner t;

    @Override
    public void run() {
        t.given(
            createVariables().variable("message", "Citrus rocks!")
        );

        t.then(
            echo().message("${message}")
        );
    }
}
```

You can now just run the test without any project setup using the Citrus JBang run command:

```shell
jbang citrus@citrusframework/citrus run MyTest.java
```

You may also install the Citrus JBang app to simplify the command line tooling:

```shell
jbang trust add https://github.com/citrusframework/citrus/
jbang app install citrus@citrusframework/citrus
```

Now you can just call:

```shell
citrus init my-test.yaml
citrus run my-test.yaml
```

A more complex Citrus integration test typically uses messaging endpoints to send and receive messages with different messaging transports. 

A typical Citrus Java JUnit Jupiter test may look like this:

```java
import org.citrusframework.TestCaseRunner;
import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.junit.jupiter.CitrusSupport;
import org.junit.jupiter.api.Test;

@CitrusSupport
public class HelloServiceIT implements TestActionSupport {

    @CitrusResource
    private TestCaseRunner t;

    @Test
    @CitrusTest
    public void shouldSayHello() {
        t.when(
            http().client("http://localhost:8080/test")
                    .send()
                    .post()
                    .contentType("text/plain")
                    .body("Hello from Citrus!")
        );

        t.then(
            receive().endpoint("kafka:my-topic")
                    .message()
                    .body("Hello from Citrus!")
        );

        t.and(
            http().client("http://localhost:8080/test")
                    .receive()
                    .response(HttpStatus.OK));
    }
}
```

As you can see the test leverages several Citrus features like sending/receiving messages with different message transports. Receiving a message always comes with an expected message content, so Citrus performs a powerful message validation on body and header content. Citrus is able to handle different message types such as XML, Json, Plaintext, YAML, CSV and more.

You can easily integrate the test into your project with Maven, or you can just run the test without any project setup with JBang.

```shell
citrus run HelloServiceIT.java
```

Developing
---------

The following software is recommended in order to code with the
Citrus framework:

* Java 17+
  Installed JDK plus JAVA_HOME environment variable set
  up and pointing to your Java installation directory. Used to compile and build the Citrus code.

* Maven 3.9.8+
  Citrus projects will fit best with [Maven](https://maven.apache.org).
  However, it is not required to use Maven. You can also run tests using [Gradle](https://gradle.org/) for instance.

* Java IDE (optional)
  A Java IDE will help you to manage your Citrus project (e.g. creating
  and executing test cases). You can use the Java IDE that you like best like Eclipse or IntelliJ IDEA.

* [JBang](https://jbang.dev) (optional)
  For fast prototyping you can use the Citrus JBang app as a command line tooling. It allows you to create and run Citrus tests without a project setup. Dependencies and Java runtime setup is automatically managed by JBang. 

Samples
---------

Our sample section is still growing. 
You can find several sample projects in the separate repository [citrusframework/citrus-samples](https://github.com/citrusframework/citrus-samples). 

Support
---------

In case you need help and support for Citrus have a look at
[https://citrusframework.org/help](https://citrusframework.org/help).
Contact `citrus-dev@googlegroups.com` directly for any request or questions.

Issues
---------

Please report any bugs and/or feature requests directly to
[citrusframework/citrus/issues](https://github.com/citrusframework/citrus/issues)

Resources
---------

* Clone the code repository [https://github.com/citrusframework/citrus.git](https://github.com/citrusframework/citrus.git) with Git to run the Maven build on your machine to get a fresh copy of the latest bits.

* Find our [blog and news articles](https://citrusframework.org/news) about Citrus and checkout the various post categories for selecting a specific topic.

* The official website [https://citrusframework.org](https://citrusframework.org) offers tutorials and more information about Citrus.

* Review the individual [release notes](https://citrusframework.org/docs/history/) to learn about the changes for a release. For detailed description of changed packages and classes do also consult the GitHub commit history.

License
---------

Copyright 2006-2025 the original author or authors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Information
---------

For more information on Citrus see [citrusframework.org][2], including
a complete [reference manual][3].

 [1]: https://citrusframework.org/img/brand-logo.png "Citrus"
 [2]: https://citrusframework.org
 [3]: https://citrusframework.org/citrus/reference/html/index.html
