---
layout: post
title: Testing WebSockets with a Citrus twist
short-title: Testing WebSockets
author: Martin Maher
github: martinmaher
categories: [blog]
---

In a previous [article](https://labs.consol.de/cache/java/2016/03/12/caching-with-jcache.html) we went through how to build a chat room web application that used REST and STOMP for communicating between the client and server. 
In this article I use the very same application and show how to write automated integration tests using the open source Citrus integration test framework.

If you haven't read the first article don't worry. A quick summary of all the important bits will be shown shortly below. But before I get to that lets talk a little bit about automated integration testing and citrus.

One of the biggest challenges when testing any application is being able to simulate all endpoints. 

<!--more-->

If you take an online web shop for example it typically interacts with numerous backend services (product catalogue, credit check, shipping, billing, etc.) during the course of processing the order. 
When writing an automated integration test that tests the placement of an order you'll have to simulate each of these services. Some services may expose a REST/HTTP interface whereas others may expose a SOAP/JMS interface. 
In some scenarios the online web shop will be acting as a client, consuming the backend services. In other cases it will be acting as a server, processing customer requests. 

The point is that testing such a scenario can be very complex. A simple application today with one or two interfaces may quickly grow into a complex application with 10s or even 100s of interfaces later on. 
When you look at integration test tools then don't loose sight of this. Sure some tools are great at simulating REST interfaces. Others are great at simulating SOAP. 
However for me the most important criteria is to find a test tool that combines these and many other messaging protocols. The tool should be flexible and extensible. And this is where citrus comes into the equation.

Before I dive into integration testing, let's do a quick recap on the chat room application.

# Chat Room

The basic architecture of the chat room application is presented below:

<div align="center">
<img src="https://labs.consol.de/assets/2016-03-15-jcache-chat-citrus/02_Architecture.png" width="500">
</div>

We have a client-server architecture, using web sockets and REST for communicating between the client and the server. The system under test is the blue box above and I'm going to write some tests that simulate the two green arrows.

The application's entity model is very modest, using just the two entities shown below:

<div align="center">
<img src="https://labs.consol.de/assets/2016-03-15-jcache-chat-citrus/03_DomainModel.png" width="500">
</div>

A room contains a list of messages and the name of the user that created the room. A message contains some text and the name of the user that sent the message. That's basically it.

The following REST operations can be sent from the client to the server:

- Join or leave the chat application
- Get the list of logged in users
- Get the list of rooms
- Create or remove a room
- Send a message to a room

The server can push the following notifications to connected clients using STOMP over WebSocket:

- Notify room created or removed
- Notify chat message sent
- Notify user logged-in or logged-out

# Starting the application

The source code for the application is available on [github](https://github.com/martinmaher/jcache-chat).

To run the application you need to have Java 8+ and Maven 3.3.3+ installed and configured on your system.

The application comes shipped with the H2 database that you need to start firing up the application:

- Open up a shell
- Change to the H2 directory under _../support/h2\_v1.3.176/_
- Run the H2 start-up script (either h2.bat or h2.sh).

Now you can start the application. This can be done either from the command line or from inside your favourite IDE.

From the command line type:

{% highlight shell %}
shell
$ mvn clean install spring-boot:run
{% endhighlight %}

This will start up one instance on port 8090.

Alternatively you can start the application from inside your IDE by running the following main class:

{% highlight shell %}
de.consol.chat.ChatApplication
{% endhighlight %}

Now go ahead and check that the application is running by opening the application in your browser:

[http://localhost:8090/](http://localhost:8090/)

# Testing the application

All the source code I'll be displaying below is available here: [https://github.com/martinmaher/jcache-chat-citrus](https://github.com/martinmaher/jcache-chat-citrus). 
I will show you how to build the project from scratch so you can either clone the above repository or just follow the instructions below, whatever you prefer.

The first thing I need to do is to create an empty maven project for storing and executing our tests. The quickest way of doing this is to execute the maven archtetype goal, 
which will create a citrus project with a basic pom file and some sample integration tests. This can be done as follows:

- Create a new directory for the project.
- Change to this directory
- Execute: mvn archetype:generate -DarchetypeCatalog=http://citrusframework.org
- When prompted select archetype '1'
- For the groupId, artifactId and package enter whatever you like. I entered
  - groupId: de.consol
  - artifactId: chat-citrus
  - package: de.consol.chat.citrus

Assuming all goes well you'll find the following files in the project:

- pom.xml – contains the maven project configuration
- src/test/resources/citrus-context.xml – contains the citrus configuration
- src/test/resources/citrus.properties – contains global properties used by citrus
- src/test/resources/log4j.xml – contains the log configuration used by citrus

There may be some other files like SampleJavaIT and SampleXmlIT that can be removed if you like, since I wont be using here.

## Simulating the REST requests

To keep things simple I'll start by simulating the REST requests sent by the client. To begin with lets take the requests where a user joins the chat room.

The request I want to simulate is:

{% highlight shell %}
POST /users/<USERNAME> HTTP/1.1
Accept: text/plain, application/json, application/*+json, */*
Content-Type: application/json;charset=UTF-8
{% endhighlight %}

and if all goes well I should get the response:

{% highlight shell %}
HTTP/1.1 200 OK
{% endhighlight %}

The first thing I need to do is to create a new java class for executing my test. I extend the _TestNGCitrusTestDesigner_ class, which allows me to use Citrus's Java DSL and add I'll add a variable for storing a random name, which will be used for the user name when joining the chat room.

{% highlight java %}
@Test
public class Test_01_RestIT extends TestNGCitrusTestDesigner {
    @CitrusTest
    public void testJoiningAndLeaving() {
        variable("username", "citrus:randomString(10, UPPERCASE)");

        echo("Joining with user: ${username}");
    }
}
{% endhighlight %}

If you like you can run this test and it should output "Joining with user .." to the console.

Next I add the test actions to join the chat room.

{% highlight java %}
[...]

echo("Joining with user: ${username}");
send("chatRestClient")
        .payload("")
        .http()
        .method(HttpMethod.POST)
        .path("/users/${username}");
receive("chatRestClient")
        .messageType(MessageType.JSON)
        .http()
        .status(HttpStatus.OK);
{% endhighlight %}

The first test action sends the request to the HTTP server. The action basically says:

- send("chatRestClient") -> send using the _chatRestClient_
- payload("") -> an empty payload
- http() -> using the HTTP protocol
- method(HttpMethod.POST) -> using HTTP's _POST_ method
- path("/users/${username}") -> to URI to send the HTTP request to

The syntax ${..} is a variable placeholder used in citrus that gets resolved to the value of the variable at runtime. In the example above I use it for adding the variable _username_ to the URI.

The second action is used for verifying that the server processes the request successfully. It expects a HTTP 200 code to be returned from the server.

You may be wondering how citrus knows which host and port to send the request to. It doesn't, well not yet. I still have to configure this. This strange string _chatRestClient_ is instructing citrus to find an endpoint using this name in the citrus configuration and use this endpoint for sending and receiving requests.

So I'm going to add this endpoint to the citrus-context.xml file now.

{% highlight xml %}
<citrus-http:client id="chatRestClient"
                    request-url="http://localhost:8090/"
                    request-method="POST"
                    content-type="application/json"
                    timeout="60000"/>
{% endhighlight %}

I have added a new HTTP client endpoint _chatRestClient_ that sends requests to the base url _http://localhost:8090/_. By default the request method _POST_ will be used with the content-type _application/json_, but both of these can be overloaded in the test. After sending a request citrus will wait 60 seconds for a reply from the server before timing out. And that's it.

Now go ahead and run this test. If you have the application opened up in your browser then you will notice a new user joins the chat room when the test executes.

To complete the test I'm going to add the test actions for leaving the chat.

{% highlight java %}
[...]

echo("Leaving chat: ${username}");
send("chatRestClient")
        .payload("")
        .http()
        .method(HttpMethod.DELETE)
        .path("/users/${username}");
receive("chatRestClient")
        .messageType(MessageType.JSON)
        .http()
        .status(HttpStatus.OK);
{% endhighlight %}

You will notice a lot of similarities to the join test actions. The only difference is that the HTTP method _DELETE_ is used this time instead of _POST_.

You can run this test again if  you like and depending on how sharp your eyes are you may notice in your browser that a user joins and almost immediately leaves the chat room.

That's it for the REST requests for the moment. Now I'm going to move onto WebSockets.

## Simulating the STOMP notifications

Before I get into simulating STOMP notifications, I think its important to understand what STOMP is and how it's used here.

STOMP is a **S** imple **T** ext **O** rientated **M** essaging **P** rotocol. It uses a frame-based protocol, shown below, for communicating between multiple hosts.

{% highlight shell %}
COMMAND
header1:value1
header2:value2

Body^@
{% endhighlight %}

Each frame contains a single COMMAND, zero or more HEADERS, a BODY and the null character (^@) to terminate the frame.

In the chat room application the server sends all notifications to connected clients using this protocol. It does this using the WebSocket API since it enables bidirectional communication between the web server and clients. Each time an event occurs, like a user joining the chat room, this event data is then pushed to the clients from the server.

In STOMP there are different types of COMMANDs that can be sent:

- _CONNECT_: this is sent by a client to the server when it connects
- _CONNECTED_: this is an acknowledgement sent by the server to a client on successful connection
- SUBSCRIBE_: this is sent by a client to the server to indicate that it is interested in certain types of messages. The client sends a destination header to indicate which messages it is interested in.
- MESSAGE_: this is sent by the server to the client when a destination, that the client has subscribed to, sends a message.
- DISCONNECT_: this is sent by the client to the server when its disconnecting.

This is by no means an exhaustive list of commands but covers all commands used in the chat room application.

To put all that into perspective, this is basically what happens when the user _Joe Blogs_ joins the chat room application:

1. The client opens a WebSocket connection to the server
2. The client sends a _CONNECT_ frame to the server

{% highlight shell %}
CONNECT
accept-version:1.1
heart-beat:0,0
{% endhighlight %}

3. The sever sends a _CONNECTED_ frame acknowledgement back to the client

{% highlight shell %}
CONNECTED
version:1.1
heart-beat:0,0
{% endhighlight %}

4. The client sends a _SUBSCRIBE_ frame to the server, indicating that it is interested in messages on the destination _/topic/users/joined_

{% highlight shell %}
SUBSCRIBE
id:sub-0
destination:/topic/users/joined
{% endhighlight %}

5. The user _Joe Blogs_ joins the chat room (using the HTTP/REST interface)
6. The server sends a _MESSAGE_ frame to the client on the destination _/topic/users/joined_ indicating that a user joined.

{% highlight shell %}
MESSAGE
destination:/topic/users/joined
content-type:application/json;charset=UTF-8
subscription:sub-0
message-id:1-18
content-length:11

"Joe Blogs"
{% endhighlight %}

So I'm going to try and pack all of this into a single test. I start by creating a new Java class _Test\_02\_StompIT_.

{% highlight java %}
@Test
public class Test_02_StompIT extends TestNGCitrusTestDesigner {
    @CitrusTest
    public void testJoining () {
        variable("username", "citrus:randomString(10, UPPERCASE)");

        echo("Joining with user: ${username}");
    }
}
{% endhighlight %}

Then I add the test actions for connecting to the STOMP server.

{% highlight java %}
send(“chatWebSocketClient”)
            .payload("CONNECT\n" +
                    "accept-version:1.1\n" +
                    "heart-beat:0,0\n" +
                    "\n" + (char)0
            );

receive(“chatWebSocketClient”)
                .messageType(MessageType.PLAINTEXT)
                .payload("CONNECTED\n" +
                        "version:1.1\n" +
                        "heart-beat:0,0\n" +
                        "\n" + (char)0
                );
{% endhighlight %}

So what is going on here?

The first test action is using the endpoint _chatWebSocketClient_ to send the STOMP _CONNECT_ frame to the server. The payload I just covered above only this time control characters like carriage return and the null character have been added.

The second test action is verifying the client was successfully connected. It compares the received payload from the server against the expected payload, which is specified in the test action. Only when both payloads match exactly will the action be successful.

Before I can execute my test I have to configure the _chatWebSocketClient_ endpoint. Again this is done in the citrus-context.xml file by adding the following lines:

{% highlight xml %}
<citrus-websocket:client id="chatWebSocketClient"
                         url="ws://localhost:8090/chatEndpoint/websocket"
                         timeout="5000"/>
{% endhighlight %}

There's nothing special here. I just added a websocket client endpoint that connects to the server on the url _ws://localhost:8090/chatEndpoint/websocket_.

Now you are good to go and you can run the test. If all goes well you should see a lot of console output with _SUCCESS_ somewhere near the bottom.

Ok, so the test doesn't really do a whole lot just yet. So I'm going to add the next test actions to subscribe to the destination _/topic/users/joined_, join the chat room and then finally verify that a STOMP _MESSAGE_ frame is sent to indicate a user has joined.

{% highlight java %}
echo("Subscribe to destination: /topic/users/joined");
send("chatWebSocketClient")
        .payload("SUBSCRIBE\n" +
                "id:sub-0\n" +
                "destination:/topic/users/joined\n" +
                "\n" + (char) 0
        );

echo("Joining with user: ${username}");
send("chatRestClient")
        .payload("")
        .http()
        .method(HttpMethod.POST)
        .path("/users/${username}");
receive("chatRestClient")
        .messageType(MessageType.JSON)
        .http()
        .status(HttpStatus.OK);

echo("Verify user-joined notification is received");
receive("chatWebSocketClient")
        .messageType(MessageType.PLAINTEXT)
        .payload("MESSAGE\n" +
                "destination:/topic/users/joined\n" +
                "content-type:application/json;charset=UTF-8\n" +
                "subscription:sub-0\n" +
                "message-id:6-21\n" +
                "content-length:12\n" +
                "\n" +
                "\"${username}\"" + (char) 0
        );
{% endhighlight %}

If you look carefully at the last test action, the MESSAGE frame, you will notice the _message-id_ header. The server sets the value of this header dynamically so the exact payload comparison done here is bound to fail, unless of course you are incredibly lucky. So how do I get around this problem?

Thanks to citrus there are many ways to solve this problem:

- Remove the payload verification from the test action - This just ignores the problem for the time being but doesn't fix it.
- Do a partial payload verification – It's possible to just search for certain strings in the payload, ignoring the rest.
- Unmarshall the payload, converting it into a java object and verify the individual attributes – This is slightly more complicated but offers by far the most flexibility.

For the moment I'm going to go with partial payload verification. When using the Citrus Java DSL you can easily do this with a ValidationCallback as shown below:

{% highlight java %}
echo("Verify user-joined notification is received");
receive("chatWebSocketClient")
        .messageType(MessageType.PLAINTEXT)
        .validationCallback(new AbstractValidationCallback<String>() {
            @Override
            public void validate(String payload, Map<String, Object> headers, TestContext context) {
                Assert.assertTrue(payload.contains("MESSAGE\n"));
                Assert.assertTrue(payload.contains("destination:/topic/users/joined\n"));
                Assert.assertTrue(payload.contains("subscription:sub-0\n"));
                Assert.assertTrue(payload.contains(context.getVariable("username")));
            }
        });
{% endhighlight %}

Great. Go ahead and run the test. This time it should run successfully.

## Cleaning up the test

So far I've covered a user joining the chat room. I still have to verify that rooms can be created, removed, messages can be sent, notifications for the above are received, etc. But before I do this lets take a step back and look at our test so far.

It's not bad, but it could be cleaned it somewhat. There are too many concatenated strings, too many carriage returns and other funny characters. It's too hard to read. It would be great if I could use a fluid API for generating the STOMP frames. And since I'm planning on verifying and extracting data from the received payloads it would be equally cool if this could be somehow unmarshalled. Well guess what, you can do this with citrus. This is what I was talking about earlier when I mentioned how important it is to pick an integration test tool that is flexible. It's not that citrus supports STOMP out of the box, because it doesn't, at least not yet. However through its flexible API it enables a tester to add support for features or behaviour that is currently not supported. And this is what I'm going to show you now when I clean up or refactor this test.

I'll leave the other test class in place and create a new test class _Test\_03\_StompIT_, copying over the contents of the original.

I'll begin by creating a new StompFrame class, which is just a container for holding a STOMP frame in a structured way.

{% highlight java %}
public class StompFrame {
    private String command;
    private Map<String, String> headers = new LinkedHashMap<>();
    private String body;

    // getters/setters….

}
{% endhighlight %}

Then I add a StompFrameBuilder class that uses the builder pattern for creating STOMP frames.

{% highlight java %}
public class StompFrameBuilder {
    StompFrame stompFrame = new StompFrame();

    public StompFrameBuilder withCommand(String command) {
        stompFrame.setCommand(command);
        return this;
    }

    public StompFrameBuilder withHeader(String key, String value) {
        stompFrame.addHeader(key, value);
        return this;
    }

    public StompFrameBuilder withBody(String body) {
        stompFrame.setBody(body);
        return this;
    }

    public StompFrame build() {
        return stompFrame;
    }
}
{% endhighlight %}

And finally I'll add a static method to the StompFrame class to convert a STOMP frame into the expected wire format.

{% highlight java %}
public static String toWireFormat(StompFrame stompFrame) {
    StringBuilder builder = new StringBuilder();

    builder.append(stompFrame.getCommand());
    builder.append("\n");

    for (Map.Entry<String, String> header : stompFrame.getHeaders().entrySet()) {
        builder.append(header.getKey());
        builder.append(":");
        builder.append(header.getValue());
        builder.append("\n");
    }

    if(stompFrame.hasBody()) {
        builder.append(stompFrame.getBody());
        builder.append("\n");
    }

    builder.append("\n");
    builder.append((char)0);

    return builder.toString();
}
{% endhighlight %}

After some refactoring I end up with the following test class:

{% highlight java %}
@Test
public class Test_03_StompIT extends TestNGCitrusTestDesigner {

    @CitrusTest
    public void testJoiningAndLeaving() {
        variable("username", "citrus:randomString(10, UPPERCASE)");

        echo("Connecting to server with STOMP");
        send("chatWebSocketClient")
                .payload(StompFrame.toWireFormat(new StompFrameBuilder()
                        .withCommand("CONNECT")
                        .withHeader("accept-version", "1.1")
                        .withHeader("heart-beat", "0,0")
                        .build()));

        echo("Verify connection is successful");
        receive("chatWebSocketClient")
                .messageType(MessageType.PLAINTEXT)
                .payload(StompFrame.toWireFormat(new StompFrameBuilder()
                        .withCommand("CONNECTED")
                        .withHeader("version", "1.1")
                        .withHeader("heart-beat", "0,0")
                        .build()));

        echo("Subscribe to destination: /topic/users/joined");
        send("chatWebSocketClient")
                .payload(StompFrame.toWireFormat(new StompFrameBuilder()
                        .withCommand("SUBSCRIBE")
                        .withHeader("id", "sub-0")
                        .withHeader("destination", "/topic/users/joined")
                        .build()));

        echo("Joining with user: ${username}");
        send("chatRestClient")
                .payload("")
                .http()
                .method(HttpMethod.POST)
                .path("/users/${username}");
        receive("chatRestClient")
                .messageType(MessageType.JSON)
                .http()
                .status(HttpStatus.OK);

        echo("Verify user-joined notification is received");
        receive("chatWebSocketClient")
                .messageType(MessageType.PLAINTEXT)
                .validationCallback(new AbstractValidationCallback<String>() {
                    @Override
                    public void validate(String payload, Map<String, Object> headers, TestContext context) {
                        Assert.assertTrue(payload.contains("MESSAGE\n"));
                        Assert.assertTrue(payload.contains("destination:/topic/users/joined\n"));
                        Assert.assertTrue(payload.contains("subscription:sub-0\n"));
                        Assert.assertTrue(payload.contains(context.getVariable("username")));
                    }
                });
    }
}
{% endhighlight %}

I'm not quite there yet but at least it's slightly easier on the eye. I'm still validating the MESSAGE frame using partial string comparisons, so I'm going to clean that up as well now.

To do that I first need to add a second static method to the StompFrame class to convert the wire format into a StompFrame.

{% highlight java %}
public static StompFrame fromWireFormat(String payload) {
    StompFrame stompFrame = new StompFrame();

    int separateAt = payload.indexOf("\n\n");
    String header = payload.substring(0, separateAt);
    String body = payload.substring(separateAt + 1);

    StringTokenizer stringTokenizer = new StringTokenizer(header, "\n");

    // set command
    stompFrame.setCommand(stringTokenizer.nextToken());

    // add headers
    while (stringTokenizer.hasMoreTokens()) {
        String token = stringTokenizer.nextToken();
        if (StringUtils.hasText(token) && token.contains(Character.toString(':'))) {
            String[] keyValue = token.split(":");
            stompFrame.addHeader(keyValue[0], keyValue[1]);
        }
    }

    // add body
    stompFrame.setBody(stripNullCharacter(body));

    return stompFrame;
}
{% endhighlight %}

Now I can finally refactor the validation callback:

{% highlight java %}
echo("Verify user-joined notification is received");
receive("chatWebSocketClient")
        .messageType(MessageType.PLAINTEXT)
        .validationCallback(new AbstractValidationCallback<String>() {
            @Override
            public void validate(String payload, Map<String, Object> headers, TestContext context) {
                StompFrame stompFrame = StompFrame.fromWireFormat(payload);
                Assert.assertEquals(stompFrame.getCommand(), "MESSAGE");
                Assert.assertEquals(stompFrame.getHeader("destination"), "/topic/users/joined");
                Assert.assertEquals(stompFrame.getHeader("subscription"), "sub-0");
                String expectedUsername = String.format("\"%s\"", context.getVariable("username"));
                Assert.assertEquals(stompFrame.getBody(), expectedUsername);
            }
        });
{% endhighlight %}

I'm done.

# Wrap-up

I added one final test class _Test\_04\_CompleteIT_ which simulates all REST and STOMP communication in one single test including

- subscribing to all STOMP topics,
- joining the chat,
- creating a room,
- sending a message,
- deleting a room
- and finally leaving the chat

You can find it in my git repository along with all the other tests.

I hope I have inspired you in this blog to at least consider Citrus when you're thinking about integration tests in the next or even current project. Although I concentrated mainly on testing a web application here, you can use Citrus for just about all integration test scenarios you can imagine. It's used a lot in middleware applications, which is one of the reasons the test framework was developed to begin with, but it's not limited to middleware as is hopefully demonstrated above.
