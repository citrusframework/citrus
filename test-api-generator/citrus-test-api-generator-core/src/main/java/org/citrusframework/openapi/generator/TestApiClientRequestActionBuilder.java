package org.citrusframework.openapi.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.actions.OpenApiClientRequestActionBuilder;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class TestApiClientRequestActionBuilder extends OpenApiClientRequestActionBuilder {

        // TODO: do we really need this?
        protected OpenApiSpecification openApiSpec;

        private final String path;

        private final Map<String, String> pathParameters = new HashMap<>();

        private final MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();

        // TODO: can we just pass in the operation?
        public TestApiClientRequestActionBuilder(OpenApiSpecification openApiSpec, String method, String path, String operationName) {
            super(openApiSpec, "%s_%s".formatted(method, path));
            name(String.format("%s:%s", "PetStore".toLowerCase(), operationName));
            getMessageBuilderSupport().header("citrus_open_api_operation_name", operationName);
            getMessageBuilderSupport().header("citrus_open_api_method", method);
            getMessageBuilderSupport().header("citrus_open_api_path", path);

            this.openApiSpec = openApiSpec;
            this.path = path;
        }

        protected void pathParameter(String name, String value) {
            pathParameters.put(name, value);
        }

        protected void formData(String name, String value) {
            formData.add(name, value);
        }

        protected String qualifiedPath(String path) {

            String qualifiedPath = path;
            for (Entry<String, String> entry : pathParameters.entrySet()) {
                qualifiedPath = qualifiedPath.replace("{%s}".formatted(entry.getKey()), entry.getValue());
            }
            return qualifiedPath;
        }

        protected String toQueryParam(String...arrayElements) {
            return String.join(",", arrayElements);
        }

        @Override
        public SendMessageAction doBuild() {
            // TODO: register callback to modify builder
            path(qualifiedPath(path));
            if (!formData.isEmpty()) {
                // TODO: do we have to explicitly set the content type or is this done by citrus
                messageBuilderSupport.contentType(MediaType.MULTIPART_FORM_DATA_VALUE);
                getMessageBuilderSupport().body(formData);
            }
            return super.doBuild();
        }

    }