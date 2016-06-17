---
layout: docs
title: Welcome
permalink: /docs/home/
redirect_from: /docs/index.html
---

Enterprise Application Integration (EAI) software usually take part in various transactions with different interface partners. 
The correct communication between the interface partners is essential for the success of the business use case. It is a very 
important task to test these use case scenarios in order to ensure the integration into the software landscape on the customer 
side.

How can someone ensure that interface partners communicate with each other as designed? And how can somebody test positive 
and negative use case scenarios in an automated way? Citrus aims to give answeres to these questions!

## Why use Citrus?

In EAI projects several software applications are using different interfaces in order to exchange data. The communication 
between interface partners becomes a very important test issue. In production the EAI system has to work as expected, especially 
regarding the end-to-end perspective including several software applications that work together.

Now when performing fully integrated end-to-end use case tests some participating applications have to be simulated in their 
behaviour. This is not an easy task when dealing with different transport protocols and complex business logic. Different 
protocols, technologies and legacy systems are therefore the beginning of many problems during the integration test.

The tester may only be able to use more or less intelligent dummy implementations of the different interface partners. These 
dummies usually are very static in their behaviour and require a lot of configuration to provide proper responses for incoming 
messages. In some cases the tester may have to interact manually during the test. An automatic verification of the message 
contents and the semantically correctness in common is not possible. The tester has to validate the success or failure of the 
test manually, which is very error prone and time consuming.

These problems arise as seemingly unsolvable challenges for the automated integration test team. Regarding the limited project 
schedule the tester may only be able to test one single use case manually using the available dummy implementations. In the 
worst case a major part of the software features is initially tested in production environment with real customers suffering 
from every software bug that is still inside the software application.

Citrus is a testing tool, that enables the test team to define whole use case tests to be executed fully automated. Incoming 
and outgoing messages are predefined in the test case. The tester defines a message flow as it is designed for a use case. 
All surrounding interface partners are simulated regardless their transport protocols (Http, JMS, TCP/IP, SOAP, and many more). 
The tests can be integrated into a continuous integration environment so Citrus gives credit to the software quality at all time.

## Project history

All software development projects at ConSol* use extensive automated testing and continuous or nightly builds to ensure highest 
possible quality of our deliveries. When we started focusing on EAI projects we searched for a tool that supports automated 
integration tests of EAI systems, simulating several connected systems using different protocols (the IT world discovered a 
surprisingly huge variety of technologies two applications may use to talk to each other). Although we evaluated several 
non-commercial and commercial tools, we didn't find one that even came close to our needs. Therefore we decided to write 
something on our own.

Back at the year 2006 we implemented a first version for our customer needs. Since then we continuously worked on improving 
the tool over several years (and will do so in the future), resulting in what you can find here today. Citrus is used for all 
EAI projects at ConSol, running in total thousands of complex test cases every day.

By this we are confident that it will be robust and stable for everything you intent to use it for. Feel free to prove us wrong!

## Why is Citrus open source?

We want to become rich and famous! Ok, at least famous. :-)

ConSol Software GmbH has a long tradition in using open source products and contributing to the open source world. As Citrus 
is largely based on open source, especially the Spring framework, and would not have been possible without the precious work of
the open source community, we simply want to return something which we think might be useful for others.
