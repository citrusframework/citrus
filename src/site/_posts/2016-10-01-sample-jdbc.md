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
See the [reference guide](http://www.citrusframework.org/reference/html/#actions-sql) database chapter for details.

The database source is configured as Spring datasource in the application context ***citrus-context.xml***.
  
{% highlight java %}
@Bean(destroyMethod = "close")
public BasicDataSource todoListDataSource() {
    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
    dataSource.setUrl("jdbc:hsqldb:hsql://localhost/testdb");
    dataSource.setUsername("sa");
    dataSource.setPassword("");
    dataSource.setInitialSize(1);
    dataSource.setMaxActive(5);
    dataSource.setMaxIdle(2);
    return dataSource;
}
{% endhighlight %}
    
As you can see we are using a H2 in memory database here.    

Before the test suite is started we create the relational database tables required.

{% highlight java %}
@Bean
public SequenceBeforeSuite beforeSuite() {
    return new TestDesignerBeforeSuiteSupport() {
        @Override
        public void beforeSuite(TestDesigner designer) {
            designer.sql(todoListDataSource())
                .statement("CREATE TABLE todo_entries (id VARCHAR(50), title VARCHAR(255), description VARCHAR(255), done BOOLEAN)");
        }
    };
}
{% endhighlight %}

After the test we delete all test data again.

{% highlight java %}
@Bean
public SequenceAfterSuite afterSuite() {
    return new TestDesignerAfterSuiteSupport() {
        @Override
        public void afterSuite(TestDesigner designer) {
            designer.sql(todoListDataSource())
                .statement("DELETE FROM todo_entries");
        }
    };
}
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