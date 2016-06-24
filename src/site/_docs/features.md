---
layout: docs
title: Features
permalink: /docs/features/
---

See below a list of features that Citrus provides as a test automation framework for enterprise integration testing. 
If you are missing a feature in Citrus please help us and submit a feature request. You can also just vote for ideas 
and features on [github](http://github.com/christophd/citrus/issues), so we can start developing those things that 
are most relevant to you.

## Control the test flow

- Send and receive messages and specify control messages for validation
- Simulate interface partners supporting a wide range of protocols (Http, JMS, SOAP WebServices, TCP/IP, ...)
- Force timeouts and error situations
- Define the sequence of messages
- Wait for messages to arrive
- Trigger messages and validate responses
- Save dynamic message content and reuse those later in test case (proper response generation with dynamic identifiers)
- Test complex messaging scenarios with step by step sequence of operations
- Use advanced logic in test cases (loops, delays, retries, parallel sections, ...)
- Message validation

## Message header validation

- XML message payload validation
- XML tree comparison
- XPath element validation
- Groovy XML validation
- XML schema and DTD validation
- Groovy message validaiton
- Database access/validation

## Validate existence of data

- Prepare/simulate database content
- Execute queries and use database content in tests
- Test writing

## Human readable tests (XML format)

- Simple test creation (cross-editor support, XML schema support)
- Separation of test cases and environment configurations (easy environment switching, improved maintenance)
- Effective testing

## Test grouping / test templates / test suites

- IDE support (execute tests as TestNG/JUnit tests from Eclipse, IntelliJ IDEA or NetBeans)
- Integration into build lifecycle (Maven, ANT)
- Reliable failure descriptions
- Reports and test results
- Parallel test execution realistic message load on tested system and faster test runs
- Provide test plan and document test coverage
- Framework extensions

## Write adapters to support more protocols

- Write customized functions
- Write customized test actions
- Execute Groovy code
