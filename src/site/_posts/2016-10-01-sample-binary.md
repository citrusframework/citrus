---
layout: sample
title: Binary sample
sample: sample-binary
description: Shows binary message content handling in Citrus
categories: [samples]
permalink: /samples/binary/
---

This sample demonstrates how Citrus handles binary message content. The sample send some binary content to a JMS queue destination and receives
that same content in a next step from the JMS destination.

Objectives
---------

We demonstrate the binary content handling by using binary JMS messages.

The Citrus project needs a JMS connection factory that is defined in the Spring application context as bean:

{% highlight java %}
@Bean
public ConnectionFactory connectionFactory() {
    return new ActiveMQConnectionFactory("tcp://localhost:61616");
}
{% endhighlight %}
    
We use ActiveMQ as message broker so we use the respective connection factory implementation here. The message broker is automatically
started with the Maven build lifecycle.
    
No we can send some content as binary message to the JMS queue destination.
    
{% highlight java %}
send(todoJmsEndpoint)
    .messageType(MessageType.BINARY)
    .message(new DefaultMessage("{ \"title\": \"${todoName}\", \"description\": \"${todoDescription}\" }".getBytes()));
{% endhighlight %}
        
The sample uses the **getBytes** method of Java String class in order to get binary content as byte array. Citrus will automatically
take care on this binary content by creating a binary JMS message.

Now the next step is to receive the same binary message in Citrus in order to do some validation. We can receive the binary message content
by marking the message type as **BINARY**. As binary content is not comparable we use a special message validator implementation that converts the
binary content to a String representation for comparison.

{% highlight java %}
receive(todoJmsEndpoint)
    .messageType(MessageType.BINARY)
    .validator(new BinaryMessageValidator())
    .payload("{ \"title\": \"${todoName}\", \"description\": \"${todoDescription}\" }");
{% endhighlight %}
        
The binary message validator implementation is very simple and performs String equals for validation:

{% highlight java %}    
private class BinaryMessageValidator extends AbstractMessageValidator<DefaultValidationContext> {
    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage,
                                TestContext context, DefaultValidationContext validationContext) {
        Assert.isTrue(new String(receivedMessage.getPayload(byte[].class))
                .equals(new String(controlMessage.getPayload(byte[].class))), "Binary message validation failed!");
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return messageType.equalsIgnoreCase(MessageType.BINARY.name());
    }

    @Override
    protected Class getRequiredValidationContextType() {
        return DefaultValidationContext.class;
    }
}    
{% endhighlight %}

This way you can implement your own validation as you know best how to handle the binary content.

We can also use base64 encoding for handling binary data in Citrus. The base64 encoding can be used to process the binary content
with basic comparison in **BINARY_BASE64** message validator:

{% highlight java %}
receive(todoJmsEndpoint)
    .messageType(MessageType.BINARY_BASE64)
    .payload("citrus:encodeBase64('{ \"title\": \"${todoName}\", \"description\": \"${todoDescription}\" }')");
{% endhighlight %}
        
Just use the `encodeBase64` function in Citrus to provide the expected payload content. Citrus will automatically convert the received 
binary content to base64 encoded Strings then for you.    
                
Run
---------

You can run the sample on your localhost in order to see Citrus in action. Read the instructions [how to run](/samples/run/) the sample.