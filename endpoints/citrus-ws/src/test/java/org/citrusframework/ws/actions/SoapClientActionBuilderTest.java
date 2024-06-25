package org.citrusframework.ws.actions;

import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.spi.AbstractReferenceResolverAwareTestActionBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertTrue;

public class SoapClientActionBuilderTest {

    private SoapClientActionBuilder fixture;

    @BeforeMethod
    public void beforeMethod() {
        fixture = new SoapClientActionBuilder(mock(Endpoint.class));
    }

    @Test
    public void isReferenceResolverAwareTestActionBuilder() {
        assertTrue(fixture instanceof AbstractReferenceResolverAwareTestActionBuilder<?>, "Is instanceof AbstractReferenceResolverAwareTestActionBuilder");
    }
}
