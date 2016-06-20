---
layout: docs
title: Conventions
permalink: /docs/conventions/
---

We think style matters. Not for beauty but for speaking a common language when talking about code and getting familiar 
with foreign code very fast. So please read this guide carefully when contributing extensions and patches.

If you don't like a rule, open a discussion - we can always talk about changes.

## Naming conventions

### Getters and setters

Name your access and setting method according to the Java Bean scheme. For instance use *getApple()* and *setApple(Apple a)* 
**not** *fetchApple()* and *storeApple(Apple a)* if you merely want to set an object's attribute. Name the accessors/setter method 
after the object it is going to get/set if possible (e.g. *getStatus()* **not** *getMyStatus()*).

### Class, method and member variable names

**Classes**, **Methods** and **Member variables** are given names in mixed case. There are never to capital letters adjacent. 
Remember, even acronyms like *'ID'* in class or method names must follow this rule. Class names always start with an upper 
case, method names and member variables always start with lower case.

    ToDo.java  // Good
    TODO.java  // BAD, use camel back writing
    todo.java  // BAD, as reserved
 
    getTestId() //Good
    getTestID() //BAD

### Test classes

Unit test classes have to end with **Test** and should live in the appropriate package in test scoped java resource folder. 
Example: *com.consol.citrus.action.SendAction* and *com.consol.citrus.action.SendActionTest*.

## Formatting

Code formatting is important. The rules are based on and verified by a href="http://checkstyle.sourceforge.net/"Checkstyle/a. 
Please keep an eye on the checkstyle rules, for instance by monitoring the Sonar checkstyle reports generated with the 
continuous build or with local Maven checkstyle reports.

Here are the most important rules:

### Indentation

No tabs! Indentation must be 4 blanks for Java and 2 blanks for XML.

### Braces

Curly braces start on the same line as the statement

{% highlight java %}
if (foo == 12) {
    doSomething();
} else {
    doSomethingOther();
}
{% endhighlight %}
 
**NOT**
 
{% highlight java %}
if (foo == 12)
{
    doSomething();
}
else
{
    doSomethingOther();
}
{% endhighlight %}

Each block must be surrounded by curly braces even if it is only one line. The code is more readable and more error proof, 
as you can not forget to add the braces once an extra line is added to the block.

{% highlight java %}
if ( a &gt; 0 ) {
    b = 1;
}
{% endhighlight %}
 
**NOT**
 
{% highlight java %}
if ( a &gt; 0 )
    b = 1;
{% endhighlight %}

### Lines

Don't use lines larger than 120 characters.

## Logging

We use SLF4J for logging in combination with the well known Log4J API, which is provided by SLF4J. Don't use System.out 
for output production. Use the logging framework as System.out is synchronized, not flexible configurable and fills the 
log files with uncontrolled console output.

### Debug level

Always check the level when using debug trace messages

{% highlight java %}
if(log.isDebugEnalbed()) {
   log.debug("Foo");
}
{% endhighlight %}

### Exceptions logging

Log exceptions correctly. Never use printStackTrace for Exceptions. Let the logging and the log configuration deal with 
it by correctly passing the exception as an additional argument.

{% highlight java %}
...
} catch(Exception ex) {
   log.error("Foo causes a problem", ex);
   ...
}
{% endhighlight %}

## Dependencies

Dependencies to other libraries and APIs are managed with Maven in various POM files. In general new dependencies go into 
the dependencyManagement section in the Citrus parent POM. In this dependency management section the version numbers are set.

Modules only reference the dependency without any version statement, so the version is kept in a central place. This 
applies to the most of our dependencies, only very specific dependences in a module can go directly into the module's POM. 
Here is a short introduction where to put new dependencies and which rules to keep in mind when handling Maven dependencies:

- **PROJECT_HOME/pom.xml**
  Contains all available dependencies (jars) for Citrus including version numbers in the dependency management section.
- All Citrus modules do always reference a dependency library defined in the dependency management section in our parent 
  Citrus POM. This way our dependency version management resides to the parent POM only.
- If a new library in turn needs another new library the preferred way is to let Maven resolve this dependency as 
  transitive dependency. So you do not have to declare the dependency's dependencies in the Maven POM. Only add a dependency if
  it is directly referenced in the module sources, and it is not transitively inherited
- Do you need all transitive dependencies? Think of which transitive dependencies could be excluded from Citrus as they 
  are not needed in our project.
- Be aware of the [Maven scopes](http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Dependency_Scope)!
