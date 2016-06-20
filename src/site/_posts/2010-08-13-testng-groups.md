---
layout: post
title: Citrus and TestNG groups
short-title: TestNG groups
author: Christoph Deppisch
github: christophd
categories: [blog]
---

TestNG groups add great flexibility to the Citrus test execution. We are able to divide all tests into several groups reaching a sophisticated seperation of concerns in our test setup. As an example I want to classify some of my functional Citrus tests as "long-running". These tests may not apply to continuous execution every time I package my project. Instead of this I want to set up a scheduled integration build to execute those long-running tests in a time schedule.

We simply declare the TestNG groups in the Java part of the Citrus tests, like this:

{% highlight java %}
@Test(groups = {"functional", "long-running"})
public void sampleCitrusITest(ITestContext testContext) {
    executeTest(testContext);
}
{% endhighlight %}

In Maven I can configure the TestNG groups to be included and excluded during build lifecycle. For better usability I add properties and profiles to my Maven POM, so the configuration looks like follows: 

{% highlight xml %}
[...]
<properties>
    <!-- TestNG groups (functional, performance, long-running) -->
    <testGroups>functional, performance</testGroups>
    <testGroupsExcluded>long-running</testGroupsExcluded>
</properties>
[...]
<profiles>
    <!-- Several profiles activating single testng groups for execution -->
    <profile>
      <id>all-tests</id>
      <properties>
        <testGroups>functional, performance, long-running</testGroups>
        <testGroupsExcluded></testGroupsExcluded>
      </properties>
    </profile>
    <profile>
      <id>performance</id>
      <properties>
        <testGroups>performance</testGroups>
      </properties>
    </profile>
    <profile>
      <id>long-running</id>
      <properties>
        <testGroups>long-running</testGroups>
        <testGroupsExcluded></testGroupsExcluded>
      </properties>
    </profile>
</profiles>
{% endhighlight %}

With this setup I am not forced to wait for the long-running tests every time I build the project locally as these test group is excluded by default. However with Maven profiles I am able to run all TestNG groups together or explicitly execute a single group:

{% highlight xml %}
mvn install (runs only short-running tests, long-running tests are excluded)
mvn install -Pfunctional,long-running (runs all tests)
mvn install -Plong-running (runs only long-running test group)
{% endhighlight %}

TestNG groups in combination with Maven are extremely useful to break down Citrus tests into logical units. For additional reading and other ways to execute TestNG groups without using Maven please see the official TestNG documentation.