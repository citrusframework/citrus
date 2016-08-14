### Wait

With this action you can make your test wait until a certain condition is satisfied. The attribute **seconds** defines the amount of time to wait in seconds. You can also use the milliseconds attribute for a more fine grained time value. The attribute **interval** defines the amount of time to wait **between** each check. The interval is always specified as millisecond time interval.

If the check does not exceed within the defined overall waiting time then the test execution fails with an appropriate error message. There are different types of conditions to check.

*  **http** : This condition is based on a Http request call on a server endpoint. Citrus will wait until the Http response is as defined (e.g. Http 200 OK). This is useful when you want to wait for a server to start.

*  **file** : This condition checks for the existence of a file on the local file system. Citrus will wait until the file is present.



Next let us have a look at a simple example:

 **XML DSL** 

```xml
<testcase name="waitTest">
    <actions>
        <wait seconds="10" interval="2000" >
          <http url="http://sample.org/resource" statusCode="200" timeout="2000" />
        <wait/>
    </actions>
</testcase>
```

 **Java DSL designer and runner** 

```java
@CitrusTest
public void waitTest() {
    waitFor().http("http://sample.org/resource").seconds(10L).interval(2000L);
}
```

The example waits for some Http server resource to be available with **Http 200 OK** response. Citrus will use **HEAD** request method by default. You can set the request method with the **method** attribute on the Http condition.

Next let us have a look at the file condition usage:

 **XML DSL** 

```xml
<testcase name="waitTest">
    <actions>
        <wait seconds="10" interval="2000" >
          <file path="path/to/resource/file.txt" />
        <wait/>
    </actions>
</testcase>
```

 **Java DSL designer and runner** 

```java
@CitrusTest
public void waitTest() {
    waitFor().file("path/to/resource/file.txt");
}
```

Citrus checks for the file to exist under the given path. Only if the file exists the test will continue with further test actions.

When should somebody use this action? This action is very useful when you want your test to wait for a certain event to occur before continuing with the test execution. For example if you wish that your test waits until a Docker container is started or for an application to create a log file before continuing, then use this action. You can also create your own condition statements and bind it to the test action.

