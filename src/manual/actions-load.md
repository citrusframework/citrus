### Load

You are able to load properties from external property files and store them as test variables. The action will require a file resource either from class path or file system in order to read the property values.

Let us look at an example to get an idea about this action:

 **Content of load.properties:** 

```xml
username=Mickey Mouse
greeting.text=Hello Test Framework
```

 **XML DSL** 

```xml
<testcase name="loadPropertiesTest">
    <actions>
        <load>
            <properties file="file:tests/resources/load.properties"/>
        </load>
        
        <trace-variables/>
    </actions>
</testcase>
```

 **Java DSL designer and runner** 

```java
@CitrusTest
public void loadPropertiesTest() {
    load("file:tests/resources/load.properties");
    
    traceVariables();
}
```

 **Output:** 

```xml
Current value of variable username = Mickey Mouse
Current value of variable greeting.text = Hello Test Framework
```

The action will load all available properties in the file load.properties and store them to the test case as local variables.

**Important**
Please be aware of the fact that existing variables are overwritten!

