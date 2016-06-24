---
layout: docs
title: Custom actions
permalink: /docs/custom-actions/
---

Citrus comes with a powerful set of actions built-in, covering a wide range of helpful SOA integration testing aspects 
like sending and receiving messages, database access, Java and Groovy scripting support or dealing with exceptions. For 
a full list of provided standard actions see the [reference documentation](${site.path}/docs/user-guide). But each project and 
especially each system-under-test (SUT) comes along with its own specifics and characteristics, so chances are high that 
during a project the need for an extension of the built-in citrus actions will arise. This tutorial shall show you how 
easy and handsome it is to provide your own custom actions and reuse them inside your citrus integration tests.

Let's quickly refresh our memory how citrus actions are used in a test case:

{% highlight xml %}  
<actions>
  <echo>
    <message>Starting integration test</message>
  </echo>
  
  <sql datasource="someDataSource">
    <statement>DELETE FROM CUSTOMERS</statement>
  </sql>
  
  <send endpoint="customerEndpoint">
    <message>
      <data>
        <![CDATA[
          <RequestMessage>
                 ...
          </RequestMessage>
        ]]>
      </data>
    </message>
  </send>
</actions>
{% endhighlight %}  

As you'll notice (or already know) actions are written in a DSL-like syntax. But under the hood they end up as Spring 
beans - so using standard Spring mechanisms for extending citrus with your own custom actions will work like a charm. 
Let's explore a first simple way to introduce a custom action into your testing project.

## Using the generic action element

This approach is almost a no-brainer, but offers less flexibility as we will see later on. Let's imagine a simplistic 
scenario: your SUT outputs a lot of file traffic during the tests, so that we need a simple way to clean up the mess in 
a specific directory right after a test run. We also put away the fact that this could be as well realized by using groovy 
or Java actions and decide to use a custom action instead.

The first step is to create our own action class which extends com.consol.citrus.actions.AbstractTestAction:

{% highlight java %}  
public class SimpleCleanupDirAction extends AbstractTestAction {
 
    private String directory;
 
    @Override
    public void doExecute(TestContext context) {
        File dir = new File(directory);
        deleteDirectory(dir);
    }
 
    //ommitted directory getter/setter and deleteDirectory() method
}
{% endhighlight %}
  
If you want to avoid inheritance here you could as well implement the interface com.consol.citrus.TestAction instead. 
The property directory is declared as field with appropriate getters and setters so that a value can be injected by the 
container.

Having your test action class ready, change to citrus-config.xml and make it available as Spring bean there:

{% highlight xml %}  
<!-- Custom action bean -->
<bean name="simpleCleanupDirAction" class="com.consol.jza.citrus.tutorial.actions.SimpleCleanupDirAction">
  <property name="directory" value="/Users/jza/tmp/test"/>    
</bean>
{% endhighlight %}  

Note that the directory value is injected here during declaration of the bean, a drawback we will address in a second. 
So for now, your first custom action is ready to be used in citrus tests using the generic action element provided by citrus:

{% highlight xml %}  
<actions>
  <echo>
      <message>Attention! Calling my first custom action bean!!</message>
  </echo>
  <action reference="simpleCleanupDirAction"/>
</actions>
{% endhighlight %}
  
Run your test project and enjoy your first custom action being executed. Easy so far, but as stated earlier, our custom 
action lacks some kind of configurability. If your SUT spammed 10 directories with files, you would have to declare one 
bean for each directory, not an optimal solution. So let's head on for a more generic approach using Spring's extensible 
XML authoring feature.  

## Using Spring's schema-based extensions

Since version 2.0, Spring provides a mechanism to define your own bean definition schemas and wire them into the Spring 
container. We will use this mechanism to define our own custom action schema, describing XML action elements which can 
then be used in citrus tests. It is much easier than it sounds, so let's get started. The functionality of our action 
bean stays the same, so just copy it and name it GenericCleanupDirAction inside the same package. Our aim is to be able 
to define actions in our tests like this:

{% highlight xml %}  
<actions>
  <custom:genericCleanupDirAction directory="/Users/jza/tmp/andSoOn"/>
</actions>
{% endhighlight %}

To achieve this, we define our custom namespace (http://www.citrusframework.org/schema/jza/custom/actions) together with 
an XSD schema for our action declaration:

{% highlight xml %}  
<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns="http://www.citrusframework.org/schema/jza/custom/actions" 
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:beans="http://www.springframework.org/schema/beans"
  targetNamespace="http://www.citrusframework.org/schema/jza/custom/actions"
  elementFormDefault="qualified"
  attributeFormDefault="unqualified">
 
  <xs:import namespace="http://www.springframework.org/schema/beans"/>
 
  <xs:element name="cleanupFileDirAction">
    <xs:complexType>
      <xs:complexContent>
        <xs:extension base="beans:identifiedType">
          <xs:attribute name="directory" type="xs:string" use="required"/>
        </xs:extension>
      </xs:complexContent>  
    </xs:complexType>
  </xs:element>
</xs:schema>
{% endhighlight %}

Important aspects are the declaration of the namespace we are using and the usage of Spring's beans namespace for the 
XSD extension mechanism to add an id attribute to our action element (as an alternative, we could simply add it ourselves). 
Place the XSD into a package of your source tree (I used com.consol.jza.citrus.tutorial.schema in this example) and head on.

Next, we have to write two simple classes to tell Spring how to handle the bean definitions of our newly created namespace. 
At first, we have to provide a NamespaceHandler for our new namespace, registering a BeanDefinitionParser for our XML element:

{% highlight java %}  
public class CustomActionsNamespaceHandler extends NamespaceHandlerSupport {
        
    public void init() {
        registerBeanDefinitionParser("cleanupFileDirAction", new GenericCleanupDirActionDefinitionParser());
    }
}
{% endhighlight %}  

We tell Spring to pass our top-level action element to our own parser implementation. The code for our custom 
BeanDefinitionParser is even simpler:

{% highlight java %}  
public class GenericCleanupDirActionDefinitionParser extends AbstractSimpleBeanDefinitionParser {
 
    @Override
    protected Class getBeanClass(Element element) {
        return GenericCleanupDirAction.class;
    }
}
{% endhighlight %}
  
We select the most simple form by choosing to extend AbstractSimpleBeanDefinitionParser, only providing the bean class 
which shall be generated - the mapping (and injection) of XSD attributes to properties is performed by Spring in this 
case. Of course parsing can be extended for more complex (nested) elements, just ase a AbstractSingleBeanDefinitionParser 
instead and overwrite the doParse() method.

Now that all our coding is done, one final step has to be performed. We have to make Spring XML parsing infrastructure 
aware our new schema. To achieve this, we have to register our XSD file and the namespace handler in two special purpose 
properties files which have to reside in the META-INF directory of your testing jar file. Spring will detect these and 
automatically pick them up as extension. At first, we declare our handler inside META-INF/spring.handlers:
  
    http\://www.citrusframework.org/schema/jza/custom/actions=com.consol.jza.citrus.tutorial.actions.xml.CustomActionsNamespaceHandler

The left side is our own namespace (with escaped ':') which gets assigned the package path to our custom handler 
implementation on the right. Having our handler registered, we notify Spring about the existence of our custom schema by 
adding META-INF/spring.schemas with the following content:

    http\://www.citrusframework.org/schema/jza/custom/actions/custom-actions.xsd=com/consol/jza/citrus/tutorial/schema/custom-actions.xsd
    
This entry defines a mapping of XML schema locations (which we will define right in a moment when using our new action 
namespace in our citrus tests) to the physical location of our custom XSD on the classpath.

So now everything is in place: we have coded our own namespace handler and bean parser, defined our custom action schema 
and namespace and registered all of it to Spring's XML extensible authoring mechanism. Time to bring the harvest in and 
use our custom action in citrus tests as desired:    

{% highlight xml %}  
<?xml version="1.0" encoding="UTF-8"?>
<spring:beans xmlns="http://www.citrusframework.org/schema/testcase" 
  xmlns:ws="http://www.citrusframework.org/schema/ws/testcase"
  xmlns:my="http://www.citrusframework.org/schema/jza/custom/actions"
  xmlns:spring="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
  xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd 
    http://www.citrusframework.org/schema/ws/testcase http://www.citrusframework.org/schema/ws/testcase/citrus-ws-testcase.xsd 
    http://www.citrusframework.org/schema/testcase http://www.citrusframework.org/schema/testcase/citrus-testcase.xsd
    http://www.citrusframework.org/schema/jza/custom/actions http://www.citrusframework.org/schema/jza/custom/actions/custom-actions.xsd">
 
  <testcase name="SampleIT">
    <meta-info>
      <!-- ommitted -->
    </meta-info>
 
    <actions>
      <echo>
        <message>Even more attention! Calling my first custom XML action!</message>
      </echo>
      <my:cleanupFileDirAction id="cleanLogsDir" directory="/Users/jza/tmp/logs"/>
      <my:cleanupFileDirAction id="cleanEtlDir" directory="/Users/jza/tmp/etl"/>
    </actions>
  </testcase>
</spring:beans>
{% endhighlight %}

Note the declaration of our new namespace as 'my' and don't forget to declare the mapping to the schemalocation as well. 
It will save you lots of curses and hours of debugging Spring - I'm speaking from experience here. ;)

That's it for the custom actions tutorial. We looked at two ways to create custom citrus actions inside your test projects. 
Using the generic citrus action element is a very easy and quick way to add your custom coded actions to a test project. 
Using the XML extensible authoring mechanism provides you with full control and improved reusability through 
parameterization in XML. Whichever approach you choose, we hope you enjoy extending citrus with custom actions as we do.