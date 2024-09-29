package org.citrusframework.openapi.actions;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertNotNull;

import org.citrusframework.endpoint.Endpoint;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OpenApiClientActionBuilderTest {

    private OpenApiClientActionBuilder fixture;

    @BeforeMethod
    public void beforeMethod() {
        fixture = new OpenApiClientActionBuilder(mock(Endpoint.class), mock(OpenApiSpecificationSource.class));
    }

    @Test
    public void isReferenceResolverAwareTestActionBuilder() {
        assertNotNull(fixture, "Is instanceof AbstractReferenceResolverAwareTestActionBuilder");
    }
}
