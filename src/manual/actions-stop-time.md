### Stop time

Time measurement during a test can be very helpful. The <trace-time> action creates and monitors multiple timelines. The action offers the attribute "id" to identify a time line. The tester can of course use more than one time line with different ids simultaneously.

Read the next example and you will understand the mix of different time lines:

 **XML DSL** 

```xml
<testcase name="StopTimeTest">
    <actions>
        <trace-time/>
        
        <trace-time id="time_line_id"/>
        
        <sleep seconds="3.5"/>
        
        <trace-time id=" time_line_id "/>
        
        <sleep milliseconds="5000"/>
        
        <trace-time/>
        
        <trace-time id=" time_line_id "/>
    </actions>
</testcase>
```

 **Java DSL designer and runner** 

```java
@CitrusTest
public void stopTimeTest() {
    stopTime();
    stopTime("time_line_id");
    sleep(3.5); // do something
    stopTime("time_line_id");
    sleep(5000); // do something
    stopTime();
    stopTime("time_line_id");
}
```

The test output looks like follows:

```xml
Starting TimeWatcher:
Starting TimeWatcher: time_line_id
TimeWatcher time_line_id after 3500 milliseconds
TimeWatcher after 8500 seconds
TimeWatcher time_line_id after 8500 milliseconds
```

**Note**
In case no time line id is specified the framework will measure the time for a default time line.To print out the current elapsed time for a time line you simply have to place the <trace-time> action into the action chain again and again, using the respective time line identifier. The elapsed time will be printed out to the console every time.

