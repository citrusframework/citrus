---
layout: sample
title: JDBC sample
sample: sample-jdbc
description: Validates stored data in relational database
categories: [samples]
permalink: /samples/jdbc/
---

This sample uses JDBC database connection to verify stored data in SQL query results sets.

Objectives
---------

The [todo-list](/samples/todo-app/) sample application stores data to a relational database. This sample shows 
the usage of database JDBC validation actions in Citrus. We are able to execute SQL statements on a database target. 
See the [reference guide](http://www.citrusframework.org/reference/html/index.html#actions-database) database chapter for details.

The database source is configured as Spring datasource in the application context ***citrus-context.xml***.
  
{% highlight xml %}
<bean id="todoListDataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
  <property name="driverClassName" value="org.hsqldb.jdbcDriver"/>
  <property name="url" value="jdbc:hsqldb:hsql://localhost/testdb"/>
  <property name="username" value="sa"/>
  <property name="password" value=""/>
  <property name="initialSize" value="1"/>
  <property name="maxActive" value="5"/>
  <property name="maxIdle" value="2"/>
</bean>
{% endhighlight %}
    
As you can see we are using a H2 in memory database here.    

Before the test suite is started we create the relational database tables required.

{% highlight xml %}
<citrus:before-suite id="createDatabase">
  <citrus:actions>
    <citrus-test:sql datasource="todoListDataSource">
      <citrus-test:statement>CREATE TABLE todo_entries (id VARCHAR(50), title VARCHAR(255), description VARCHAR(255))</citrus-test:statement>
    </citrus-test:sql>
  </citrus:actions>
</citrus:before-suite>
{% endhighlight %}

After the test we delete all test data again.

{% highlight xml %}
<citrus:after-suite id="cleanUpDatabase">
  <citrus:actions>
    <citrus-test:sql datasource="todoListDataSource">
      <citrus-test:statement>DELETE FROM todo_entries</citrus-test:statement>
    </citrus-test:sql>
  </citrus:actions>
</citrus:after-suite>
{% endhighlight %}

In the test case we can reference the datasource in order to access the stored data and
verify the result sets.

{% highlight java %}
query(todoDataSource)
    .statement("select count(*) as cnt from todo_entries where title = '${todoName}'")
    .validate("cnt", "1");    
{% endhighlight %}
                
Run
---------

You can run the sample on your localhost in order to see Citrus in action. Read the instructions [how to run](/samples/run/) the sample.