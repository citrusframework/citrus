---
layout: post
title: Testing webMethods with Citrus Part I
short-title: Testing webMethods I
author: Jan Zahalka
github: jaza089 
categories: [blog]
---

Continuous integration is almost mainstream nowadays. Probably no one wants to argue against the value of having an all-embracing integration test suite in place, 
which is lightweight enough to be executed on each code change. In this blog series I want to show the interplay between [Citrus](http://www.citrusframework.org), 
the integration test framework written and maintained by [ConSol](http://www.consol.com) and a commonly used Enterprise Service Bus, the [webMethods Integration Server](http://www.softwareag.com/corporate/products/wm/integration/overview/default.asp).

### Meet the participants

Citrus is a lightweight integration test framework specially suited for enterprise integration challenges. It supports a broad set of communication protocols and is able to 
simulate partner applications of any type to provide proper end-to-end testing in a sandbox environment.

Our system-under-test is Software AG’s webMethods Integration Server, which is an industry-proof ESB solving complex integration and B2B challenges in large-scale deployments around the globe.

### Why Citrus?

On a closer look at the built-in support for automated testing, webMethods provides integrated tooling (namely the wMTestSuite), directly incorporated into the Software AG Designer to support 
developers with tests operating on the basic building blocks inside webMethods – the Flow service.

Comparing this tooling to traditional software development, this means there is a good framework for Unit/white box testing in place, but: wmTestSuite is not suited for automated tests from an 
integration/black box perspective. For such tasks, several commercial tools are available, but this is where Citrus with its support for automated tests on interface level comes into play as a 
smart and fully open source alternative.

## What’s coming up?

Over the next parts of this series I want to share the basic steps in order to setup a Citrus project and develop automated tests for several webMethods integration scenarios, including SOAP 
request/reply, SOAP and JMS mocking as well as flat file integration. In the end I might also cover the topic BPMN processes on top of webMethods which can be tested with Citrus as well.

Stay tuned for the next upcoming part II: basic project setup