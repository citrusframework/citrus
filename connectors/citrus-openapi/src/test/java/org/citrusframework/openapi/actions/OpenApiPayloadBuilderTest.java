package org.citrusframework.openapi.actions;

import org.citrusframework.context.TestContext;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OpenApiPayloadBuilderTest {

    private TestContext context;

    @BeforeClass
    public void setUp() {
        context = new TestContext();
    }

    @Test
    public void testBuildPayloadWithMultiValueMap() {
        // Given
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("key1", "value1");
        multiValueMap.add("key2", "Hello ${user}, welcome!");
        multiValueMap.add("key2", "Another ${user} message");
        multiValueMap.add("${k3}", "a");
        multiValueMap.add("${k3}", "b");
        multiValueMap.add("${k3}", "${user}");

        context.setVariable("user", "John");
        context.setVariable("k3", "key3");

        OpenApiPayloadBuilder payloadBuilder = new OpenApiPayloadBuilder(multiValueMap);

        // When
        Object payload = payloadBuilder.buildPayload(context);

        // Then
        Assert.assertTrue(payload instanceof MultiValueMap);
        MultiValueMap<String, String> result = (MultiValueMap<String, String>) payload;
        
        Assert.assertEquals(result.get("key1").get(0), "value1");
        Assert.assertEquals(result.get("key2").get(0), "Hello John, welcome!");
        Assert.assertEquals(result.get("key2").get(1), "Another John message");
        Assert.assertEquals(result.get("key3").get(0), "a");
        Assert.assertEquals(result.get("key3").get(1), "b");
        Assert.assertEquals(result.get("key3").get(2), "John");
    }

    @Test
    public void testBuildPayloadWithPlainObject() {
        // Given
        String simplePayload = "This is a simple ${message}";
        context.setVariable("message", "test");

        OpenApiPayloadBuilder payloadBuilder = new OpenApiPayloadBuilder(simplePayload);

        // When
        Object payload = payloadBuilder.buildPayload(context);

        // Then
        Assert.assertTrue(payload instanceof String);
        Assert.assertEquals(payload, "This is a simple test");
    }
}
