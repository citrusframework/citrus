package org.citrusframework.ws.actions;

import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.spi.AbstractReferenceResolverAwareTestActionBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SoapServerActionBuilderTest {

    private SoapServerActionBuilder fixture;

    @BeforeEach
    public void beforeMethod() {
        fixture = new SoapServerActionBuilder(mock(Endpoint.class));
    }

    @Test
    public void isReferenceResolverAwareTestActionBuilder() {
        assertTrue(fixture instanceof AbstractReferenceResolverAwareTestActionBuilder<?>, "Is instanceof AbstractReferenceResolverAwareTestActionBuilder");
    }
}
