### Plain text message validation

Plain text message validation is the easiest validation in Citrus that you can think of. This validation just performs an exact Java String match of received and expected message payloads.

As usual a default message validator for plaintext messages is active by default. Citrus will pick this message validator for all messages of **type="plaintext"** . The default message validator implementation can be overwritten by placing a Spring bean with id **defaultPlaintextMessageValidator** to the Spring application context.

```xml
<bean id="defaultPlaintextMessageValidator" class="com.consol.citrus.validation.text.PlainTextMessageValidator"/>
```

In the test case receiving action we tell Citrus to use plain text message validation.

```xml
<receive endpoint="httpMessageEndpoint">
    <message type="plaintext">
        <data>Hello World!</data>
    </message>
</receive>
```

With the message format type **type="plaintext"** set Citrus performs String equals on the message payloads (received and expected). Only exact match will pass the test.

By the way sending plain text messages in Citrus is also very easy. Just use the plain text message payload data in your sending message action.

```xml
<send endpoint="httpMessageEndpoint">
    <message>
        <data>Hello World!</data>
    </message>
</send>
```

Of course test variables are supported in the plain text payloads. The variables are replace by the referenced values before sending or receiving the message.

