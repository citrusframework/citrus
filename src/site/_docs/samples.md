---
layout: docs
title: Samples
permalink: /docs/samples/
---

We try to give you example projects as a good starting point for your very own Citrus testing project. 

The Citrus sample applications try to demonstrate how Citrus works in different integration test scenarios. The projects are executable with Maven
and should give you a detailed picture how Citrus testing works.

Please have a look at the repository on [Github](https://github.com/christophd/citrus-samples). There you can find all our sample projects.

Each sample folder demonstrates a special aspect of how to use Citrus. Most of the samples use a simple todo-list application as
system under test. Please find following list of samples and their primary objective:

| Sample                                | Objective |
|---------------------------------------|:---------:|
| [sample-javaconfig](https://github.com/christophd/citrus-samples/sample-javaconfig)| Uses pure Java POJOs for configuration |
| [sample-jdbc](https://github.com/christophd/citrus-samples/sample-jdbc)| Validates stored data in relational database |
| [sample-jms](https://github.com/christophd/citrus-samples/sample-jms)| Shows JMS queue connectivity |
| [sample-soap](https://github.com/christophd/citrus-samples/sample-soap)| Shows SOAP web service support |
| [sample-xhtml](https://github.com/christophd/citrus-samples/sample-xhtml)| Shows XHTML validation feature |
| [sample-camel-context](https://github.com/christophd/citrus-samples/sample-camel-context)| Interact with Apache Camel context and routes |
| [sample-cucumber](https://github.com/christophd/citrus-samples/sample-cucumber)| Shows BDD integration with Cucumber |
| [sample-cucumber-spring](https://github.com/christophd/citrus-samples/sample-cucumber-spring)| Shows BDD integration with Cucumber using Spring Framework injection |
|                                       |           |

Following sample projects cover message transports and technologies. Each of these samples provides a separate system under test applicaiton
that demonstrates the messaging aspect.

| Transport                             | JMS | Http | SOAP | Channel | Camel | Arquillian | JDBC | SYNC | ASYNC |
|---------------------------------------|:---:|:----:|:----:|:-------:|:-----:|:----------:|:----:|:----:|:-----:|
| [sample-bakery](https://github.com/christophd/citrus-samples/sample-bakery)               |  X  |  X   |      |         |       |            |      |  X   |   X   |
| [sample-flightbooking](https://github.com/christophd/citrus-samples/sample-flightbooking) |  X  |  X   |      |         |       |            |  X   |      |   X   |
| [sample-greeting](https://github.com/christophd/citrus-samples/sample-greeting)           |  X  |      |      |    X    |       |            |      |  X   |   X   |
| [sample-bookstore](https://github.com/christophd/citrus-samples/sample-bookstore)         |  X  |      |  X   |         |       |            |      |  X   |       |
| [sample-incident](https://github.com/christophd/citrus-samples/sample-incident)           |  X  |  X   |  X   |         |       |            |      |  X   |   X   |
| [sample-javaee](https://github.com/christophd/citrus-samples/sample-javaee)               |  X  |  X   |      |         |       |     X      |      |  X   |   X   |
|                                                                                |     |      |      |         |       |            |      |      |       |

Pick your sample application for try out and got to the respective folder. Each sample is executable on your host. You just need Maven, Java and a git client.
Refer to the detailed instructions in the sample [README](https://github.com/christophd/citrus-samples/blob/master/README.md).

Of course we appreciate any kind of help that you can give us in order to make things easy for Citrus users. So if you have
a sample worth reading for others please let us know. Also if you are missing a sample because you do not know how to accomplish the Citrus
use case please do not hesitate to contact us. You may also open a new issue in the [Citrus samples](https://github.com/christophd/citrus-samples/issues) 
repository.

