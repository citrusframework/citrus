---
layout: post
title: Testing webMethods with Citrus Part II
short-title: Testing webMethods II
author: Jan Zahalka
github: jaza089 
categories: [blog]
---

In [part I](/news/2014/03/06/testing-webmethods-with-citrus-part-i) of this tutorial I introduced the basic concepts and benefits of Citrus as a test driver for ESB projects in general and webMethods in particular. 
In this second part I want to discuss some Citrus project setup options and provide a quickstart template project for Ant users.

### Maven or Ant?

Basically, Citrus support both options – you are free in your choice of the build system to use to drive your Citrus tests. Typically a Maven setup is preferred since Maven reduces the necessary project setup 
clutter one has to face when using Ant. Check this [quickstart](/docs/setup-maven.html) if you want to let Maven create your project structure for you.

However, everything in the webMethods world is ant-based (like tooling, deployment etc.), at least this was the case during my experience with the 8.2 version of webMethods, let me know if things have changed meanwhile. 
So it might make pretty good sense to setup the Citrus project with Ant as well to reuse existing project knowledge. That’s what the rest of this blog entry is basically about.

### Ant Setup

The Citrus homepage provides an awesome Ant [quickstart](/docs/setup-ant). Everything you need is described there in detail, however, it is quite a tough job to collect all the necessary dependency jar’s manually. 
Since version 1.4, Citrus provides a maven assembly goal which creates a directory with all dependency jar’s which can be thrown into the lib folder of your newly created Citrus project. You can trigger this assembly 
by grabbing the Citrus sources and execute

{% highlight shell %}
mvn assembly:assembly -Passembly-antlibs
{% endhighlight %}

from the project root. Unfortunately I faced some issues on my Macbook with Maven version 3.0.5 so this way was blocked for me. Additionally, not everyone might have interest or possibilities to check out the sources 
or have maven installed and setup on his machine. That's basically why I want to provide a template project to speed up Ant project setup.

### Citrus Ant Template Project

For those who prefer the easiest way (like me), I have set up up a little Eclipse template project, where everything needed to run Citrus with Ant is already in place. It contains

- The correct folder structure
- All necessary Citrus (1.4) core libs and dependencies
- A prepared citrus citrus-context.xml and log4j.xml
- A template build.xml file

You can grab the template project on Github [here](https://github.com/jaza089/citrus-wm-samples/tree/master/CitrusTemplateProject) , import it (Select Import --&gt; General/Archive File in Eclipse/SAG Designer) 
and kick start developing integration tests for your webMethods project. Typically, you will work in Software AG Designer to develop your webMethods solution, luckily it is based on Eclipse and you can perfectly 
work with your Citrus test projects in the Java perspective of SAG Designer. In fact, the template project is created and exported from SAG Designer 9.5.

In the next part, I will enhance our empty project with a first test: a SOAP request/reply scenario with Citrus against a webMethods package.