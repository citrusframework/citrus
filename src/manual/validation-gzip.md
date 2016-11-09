### Gzip message validation

Gzip is a famous message compression library. When dealing with large message content the compression might be a good way to optimize the message transportation.
Citrus is able to handle gzipped message payloads with binary message validation. As usual binary messages are compared with a given control message in binary base64 String representation.
The gzip binary data is automatically unzipped and encoded as base64 character sequence in order to compare with an expected content.

The received message content is using gzip format but the actual message conent does not have to be base64 encoded. Citrus is doing this conversion automatically before validation takes place. 
The binary data can be anything e.g. images, pdf or plaintext content.

The default message validator for gzip messages is active by default. Citrus will pick this message validator for all messages of **type="gzip_base64"** . The default message validator implementation 
can be overwritten by placing a Spring bean with id **defaultGzipBinaryBase64MessageValidator** to the Spring application context.

```xml
<bean id="defaultGzipBinaryBase64MessageValidator" class="com.consol.citrus.validation.text.GzipBinaryBase64MessageValidator"/>
```

In the test case receiving action we tell Citrus to use gzip message validation.

```xml
<receive endpoint="httpMessageEndpoint">
    <message type="gzip_base64">
        <data>citrus:encodeBase64('Hello World!')</data>
    </message>
</receive>
```

With the message format type **type="gzip_base64"** Citrus performs the gzip base64 character sequence validation. Incoming message content is automatically unzipped and encoded as base64 String and 
compared to the expected data. This way we can make sure that the binary content is as expected.

By the way sending binary messages with gzip in Citrus is also very easy. Just use the **type="gzip"** message type in the send operation. Citrus now converts the message payload to a gzip binary stream as payload.

```xml
<send endpoint="httpMessageEndpoint">
    <message type="gzip">
        <data>Hello World!</data>
    </message>
</send>
```
