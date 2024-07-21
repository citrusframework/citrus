package org.citrusframework.openapi;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class OpenApiMessageTypeTest {

    @Test
    public void testToHeaderNameRequest() {
        assertEquals(OpenApiMessageType.REQUEST.toHeaderName(), OpenApiMessageHeaders.REQUEST_TYPE);
    }

    @Test
    public void testToHeaderNameResponse() {
        assertEquals(OpenApiMessageHeaders.REQUEST_TYPE, OpenApiMessageHeaders.RESPONSE_TYPE);
    }
}
