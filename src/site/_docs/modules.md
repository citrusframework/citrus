---
layout: docs
title: Modules
permalink: /docs/modules/
---

### citrus (POM)

This is the main parent project containing general configurations, such as

- site generation
- dependency management
- plugin management
- overall project information, such as mailing lists, team-members, scm, bug-tracking, ...
- sub modules

### citrus-core (JAR)

The citrus-core module builds the core functionality such as

- domain model (TestCase, TestAction, CitrusApplication, ...)
- test actions (send, receive, sleep, sql, ...)
- validation implementation (XML tree validation, XPath element validation) utility
- The module also includes basic JMS and Spring integration message channel support in these two sections:
- citrus-channel (message channel sender and receiver implementations)

### citrus-jms (JAR)

Module contains classes for JMS support for connection to JMS message brokers.

### citrus-http (JAR)

This module contains special HTTP client/server implementation. With this adapter Citrus is able to send and receive 
messages over HTTP.

### citrus-websocket (JAR)

This module contains special WebSocket client/server implementation. With this adapter Citrus is able to send and receive 
messages over bidirectional HTTP WebSocket connections.

### citrus-ws (JAR)

Special adapter implementation for SOAP WebServices. Module contains classes for SOAP client and server connections over 
SOAP/HTTP.

### citrus-ssh (JAR)

SSH adapter implementation for connecting to SSH servers. The module contains classes for SSH client and server connectivity.

### citrus-vertx (JAR)

Vert.x adapter implementation for connecting to the Vert.x event bus. The module contains classes for accessing the event 
bus as producer or consumer.

### citrus-camel (JAR)

Apache Camel adapter implementation for connecting to camel context and route definitions. The module contains classes 
for sending and receiving messages both to and from Apache Camel routes.

### citrus-ftp (JAR)

Ftp module connects to a ftp server as a client and provides full qualified ftp server for simulation. Server accepts 
incoming commands that can be validated in a test case. Server supports user management with different home directories 
and permissions per user.

### citrus-java-dsl (JAR)

Module for writing Citrus test cases with Java only. This is an alternative to writing test cases in XML syntax.

### citrus-arquillian (JAR)

Module providing utility classes and helper for Arquillian test framework integration with Citrus.

### citrus-docker (JAR)

Module providing utility classes and helper for Docker integration with Citrus.

### citrus-rmi (JAR)

Module providing client and server implementations for Java RMI integration with Citrus.

### citrus-jmx (JAR)

Module providing client and server implementations for Java JMX mbean integration with Citrus.

### citrus-integration (JAR)

Internal integration tests executed during the Citrus build lifecycle. The tests help to ensure the integration test 
quality of Citrus.

### citrus-samples (POM)

Samples module holding several self runnable sample applications that are tested with Citrus. Currently this module 
knows following sample applications:

- citrus-samples-flightbooking
- citrus-samples-greeting
- citrus-samples-bookstore
- citrus-samples-incident
- citrus-tools (POM)

Tool collection with Maven plugin, archetypes and ANT tasks for executing Citrus tests:

citrus-anttasks
citrus-maven-plugin
citrus-quickstart
citrus-quickstart-soap