package org.citrusframework.openapi.actions;

import org.citrusframework.context.TestContext;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.spi.Resources;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.openapi.actions.OpenApiActionBuilder.openapi;

public class OpenApiClientRequestMessageBuilderTest {

    private final OpenApiSpecification petstoreSpec = OpenApiSpecification.from(
            Resources.create("classpath:org/citrusframework/openapi/petstore/petstore-derivation-for-message-builder-test.json"));

    @Test
    public void shouldAddRandomDataForOperation() {
        Message message = openapi()
                .specification(petstoreSpec)
                .client()
                .send("addPet") // operationId
                .build()
                .getMessageBuilder()
                .build(new TestContext(), "");
        Assert.assertTrue(message instanceof HttpMessage);
        HttpMessage httpMessage = (HttpMessage) message;
        // test payload
        Object payload = httpMessage.getPayload();
        Assert.assertNotNull(payload);
        Assert.assertTrue(payload instanceof String);
        // test header
        Object header = httpMessage.getHeader("X-SAMPLE-HEADER");
        Assert.assertNotNull(header);
    }

    @Test
    public void shouldAddCustomDataForOperation() {
        String body = "{\"a\":\"b\"}";
        String sampleHeader = "X-SAMPLE-HEADER-VALUE";
        Message message = openapi()
                .specification(petstoreSpec)
                .client()
                .send("addPet") // operationId
                .message()
                .body(body)
                .header("X-SAMPLE-HEADER", sampleHeader)
                .build()
                .getMessageBuilder()
                .build(new TestContext(), "");
        Assert.assertTrue(message instanceof HttpMessage);
        HttpMessage httpMessage = (HttpMessage) message;
        // test payload
        Object payload = httpMessage.getPayload();
        Assert.assertEquals(payload, body);
        // test header
        Object header = httpMessage.getHeader("X-SAMPLE-HEADER");
        Assert.assertEquals(header, sampleHeader);
    }

}