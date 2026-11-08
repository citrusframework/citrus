package org.citrusframework.camel.actions;

import org.citrusframework.spi.AbstractReferenceResolverAwareTestActionBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CamelRouteActionBuilderTest {

    private CamelRouteActionBuilder fixture;

    @BeforeEach
    public void beforeMethod() {
        fixture = new CamelRouteActionBuilder();
    }

    @Test
    public void isReferenceResolverAwareTestActionBuilder() {
        assertTrue(fixture instanceof AbstractReferenceResolverAwareTestActionBuilder<?>, "Is instanceof AbstractReferenceResolverAwareTestActionBuilder");
    }
}
