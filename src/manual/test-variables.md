## Test variables

The usage of test variables is a core concept when writing good maintainable tests. The key identifiers of a test case should be exposed as test variables at the very beginning of a test. This way hard coded identifiers and multiple redundant values inside the test can be avoided from scratch. As a tester you define all test variables at the very beginning of your test.

 **XML DSL** 

```xml
<variables>
    <variable name="text" value="Hello Test Framework"/>
    <variable name="customerId" value="123456789"/>
</variables>
```

 **Java DSL designer and runner** 

```xml
variable("text", "Hello Test Framework");
variable("customerId", "123456789");
```

The concept of test variables is essential when writing complex tests with lots of identifiers and semantic data. Test variables are valid for the whole test case. You can reference them several times using a common variable expression ***"${variable-name}"*** . It is good practice to provide all important entities as test variables. This makes the test easier to maintain and more flexible. All essential entities and identifiers are present right at the beginning of the test, which may also give the opportunity to easily create test variants by simply changing the variable values for other test scenarios.

The name of the variable is arbitrary. Feel free to specify any name you can think of. Of course you need to be careful with special characters and reserved XML entities like '&', '<', '>'. If you are familiar with Java or any other programming language simply think of the naming rules there and you will be fine with working on Citrus variables, too. The value of a variable can be any character sequence. But again be aware of special XML characters like "<" that need to be escaped ("&lt;") when used in variable values.

The advantage of variables is obvious. Once declared the variables can be referenced many times in the test. This makes it very easy to vary different test cases by adjusting the variables for different means (e.g. use different error codes in test cases).

### Global variables

The last section told us to use variables as they are very useful and extend the maintainability of test cases. Now that every test case defines local variables you can also define global variables. The global variables are valid in all tests by default. This is a good opportunity to declare constant values for all tests. As these variables are global we need to add those to the basic Spring application context file. The following example demonstrates how to add global variables in Citrus:

```xml
<citrus:global-variables>
    <citrus:variable name="projectName" value="Citrus Integration Testing"/>
    <citrus:variable name="userName" value="TestUser"/>
</citrus:global-variables>
```

We add the Spring bean component to the application context file. The component receives a list of name-value variable elements. You can reference the global variables in your test cases as usual.

Another possibility to set global variables is to load those from external property files. This may give you more powerful global variables with user specific properties for instance. See how to load property files as global variables in this example:

```xml
<citrus:global-variables>
    <citrus:file path="classpath:global-variable.properties"/>
</citrus:global-variables>
```

We have just added a file path reference to the global variables component. Citrus loads the property file content as global test variables. You can mix property file and name-value pair variable definitions in the global variables component.

**Note**
The global variables can have variable expressions and Citrus functions. It is possible to use previously defined global variables as values of new variables, like in this example:

```xml
user=Citrus
greeting=Hello ${user}!
date=citrus:currentDate('yyyy-MM-dd')
```

### Create variables with CDATA

When using th XML test case DSL we can not have XML variable values out of the box. This would interfere with the XML DSL elements defined in the Citrus testcase XSD schema. You can use CDATA sections within the variable value element in order to do this though.

```xml
<variables>
    <variable name="persons">
        <value>
            <data>
                <![CDATA[
                  <persons>
                    <person>
                      <name>Theodor</name>
                      <age>10</age>
                    </person>
                    <person>
                      <name>Alvin</name>
                      <age>9</age>
                    </person>
                  </persons>
                ]]>
            </data>
        </value>
    </variable>
</variables>
```

That is how you can use XML variable values in the XML DSL. In the Java DSL we do not have these problems.

### Create variables with Groovy

You can also use a script to create variable values. This is extremely handy when you have very complex variable values. Just code a small Groovy script for instance in order to define the variable value. A small sample should give you the idea how that works:

```xml
<variables>
    <variable name="avg">
        <value>
            <script type="groovy">
                <![CDATA[
                    a = 4
                    b = 6
                    return (a + b) / 2
                ]]>
            </script>
        </value>
    </variable>
    <variable name="sum">
        <value>
            <script type="groovy">
                <![CDATA[
                    5 + 5
                ]]>
            </script>
        </value>
    </variable>
</variables>
```

We use the script code right inside the variable value definition. The value of the variable is the result of the last operation performed within the script. For longer script code the use of ***<![CDATA[ ]]>*** sections is recommended.

Citrus uses the javax ScriptEngine mechanism in order to evaluate the script code. By default Groovy is supported in any Citrus project. So you can add additional ScriptEngine implementations to your project and support other script types, too.
