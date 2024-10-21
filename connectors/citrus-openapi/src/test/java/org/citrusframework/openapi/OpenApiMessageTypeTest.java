package org.citrusframework.openapi;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class OpenApiMessageTypeTest {

    @Test
    public void testToHeaderNameRequest() {
        assertEquals(OpenApiMessageType.REQUEST.toHeaderName(), OpenApiMessageHeaders.REQUEST_TYPE);
    }

    @Test
    public void testToHeaderNameResponse() {
        assertEquals(OpenApiMessageType.RESPONSE.toHeaderName(), OpenApiMessageHeaders.RESPONSE_TYPE);
    }
}
