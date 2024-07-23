package org.citrusframework.openapi.generator;

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.actions.OpenApiClientResponseActionBuilder;
import org.citrusframework.openapi.actions.OpenApiSpecificationSource;

public class TestApiClientResponseActionBuilder extends OpenApiClientResponseActionBuilder {

    protected OpenApiSpecification openApiSpec;

    private final String path;

    // TODO: can we just pass in the operation?
    public TestApiClientResponseActionBuilder(OpenApiSpecification openApiSpec, String method,
        String path, String operationName, String statusCode) {
        super(new OpenApiSpecificationSource(openApiSpec), "%s_%s".formatted(method, path), statusCode);
        name(String.format("receive-%s:%s", "PetStore".toLowerCase(), operationName));
        getMessageBuilderSupport().header("citrus_open_api_operation_name", operationName);
        getMessageBuilderSupport().header("citrus_open_api_method", method);
        getMessageBuilderSupport().header("citrus_open_api_path", path);

        this.openApiSpec = openApiSpec;
        this.path = path;
    }

    @Override
    public ReceiveMessageAction doBuild() {
        return super.doBuild();
    }

}