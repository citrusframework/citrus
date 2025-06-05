package org.citrusframework.openapi.actions;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;

public interface OpenApiSpecificationSourceAwareBuilder<T extends TestAction> extends TestActionBuilder<T> {

    OpenApiSpecificationSource getOpenApiSpecificationSource();
}
