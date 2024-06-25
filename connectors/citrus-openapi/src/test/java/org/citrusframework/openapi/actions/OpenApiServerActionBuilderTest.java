package org.citrusframework.openapi.actions;

import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.spi.AbstractReferenceResolverAwareTestActionBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertTrue;

public class OpenApiServerActionBuilderTest {

    private OpenApiServerActionBuilder fixture;

    @BeforeMethod
    public void beforeMethod() {
        fixture = new OpenApiServerActionBuilder(mock(Endpoint.class), mock(OpenApiSpecification.class));
    }

    @Test
    public void isReferenceResolverAwareTestActionBuilder() {
        assertTrue(fixture instanceof AbstractReferenceResolverAwareTestActionBuilder<?>, "Is instanceof AbstractReferenceResolverAwareTestActionBuilder");
    }
}
