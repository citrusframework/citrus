package org.citrusframework.openapi.generator;

import jakarta.servlet.http.Cookie;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.actions.OpenApiClientRequestActionBuilder;
import org.citrusframework.openapi.actions.OpenApiSpecificationSource;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class TestApiClientRequestActionBuilder extends OpenApiClientRequestActionBuilder {

    // TODO: do we really need this?
    protected OpenApiSpecification openApiSpec;

    private final String path;

    private final Map<String, String> pathParams = new HashMap<>();

    private final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<>();

    // TODO: can we just pass in the operation?
    public TestApiClientRequestActionBuilder(OpenApiSpecification openApiSpec, String method,
        String path, String operationName) {
        super(new OpenApiSpecificationSource(openApiSpec), "%s_%s".formatted(method, path));
        name(String.format("send-%s:%s", "PetStore".toLowerCase(), operationName));
        getMessageBuilderSupport().header("citrus_open_api_operation_name", operationName);
        getMessageBuilderSupport().header("citrus_open_api_method", method);
        getMessageBuilderSupport().header("citrus_open_api_path", path);

        this.openApiSpec = openApiSpec;
        this.path = path;
    }

    protected void pathParam(String name, String value) {
        pathParams.put(name, value);
    }

    protected void formParam(String name, String value) {
        formParams.add(name, value);
    }

    protected void headerParam(String name, String value) {
        getMessageBuilderSupport().header(name, value);
    }

    protected void cookieParam(String name, String value) {
        getMessageBuilderSupport().cookie(new Cookie(name, value));
    }

    protected String qualifiedPath(String path) {

        String qualifiedPath = path;
        for (Entry<String, String> entry : pathParams.entrySet()) {
            qualifiedPath = qualifiedPath.replace("{%s}".formatted(entry.getKey()),
                entry.getValue());
        }
        return qualifiedPath;
    }

    protected String toQueryParam(String... arrayElements) {
        return String.join(",", arrayElements);
    }

    @Override
    public SendMessageAction doBuild() {
        // TODO: register callback to modify builder
        path(qualifiedPath(path));
        if (!formParams.isEmpty()) {
            // TODO: do we have to explicitly set the content type or is this done by citrus
            messageBuilderSupport.contentType(MediaType.MULTIPART_FORM_DATA_VALUE);
            getMessageBuilderSupport().body(formParams);
        }
        return super.doBuild();
    }

}