/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.openapi.testapi;

import java.util.List;

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.actions.OpenApiClientResponseActionBuilder;
import org.citrusframework.openapi.actions.OpenApiSpecificationSource;

import static java.lang.String.format;
import static org.citrusframework.openapi.util.OpenApiUtils.createFullPathOperationIdentifier;

public class RestApiReceiveMessageActionBuilder extends OpenApiClientResponseActionBuilder {

    private final GeneratedApi generatedApi;

    private final List<ApiActionBuilderCustomizer> customizers;

    public RestApiReceiveMessageActionBuilder(GeneratedApi generatedApi,
                                              OpenApiSpecification openApiSpec,
                                              String method,
                                              String path,
                                              String operationName,
                                              String statusCode) {
        super(new OpenApiSpecificationSource(openApiSpec), createFullPathOperationIdentifier(method, path), statusCode);

        this.generatedApi = generatedApi;
        this.customizers = generatedApi.getCustomizers();

        name(format("receive-%s", operationName));

    }

    public RestApiReceiveMessageActionBuilder(GeneratedApi generatedApi,
                                              OpenApiSpecification openApiSpec,
                                              OpenApiClientResponseMessageBuilder messageBuilder,
                                              HttpMessage httpMessage,
                                              String method,
                                              String path,
                                              String operationName) {
        super(new OpenApiSpecificationSource(openApiSpec), messageBuilder, httpMessage, createFullPathOperationIdentifier(method, path));

        this.generatedApi = generatedApi;
        this.customizers = generatedApi.getCustomizers();

        name(format("receive-%s", operationName));
    }

    public GeneratedApi getGeneratedApi() {
        return generatedApi;
    }

    public List<ApiActionBuilderCustomizer> getCustomizers() {
        return customizers;
    }

    @Override
    public ReceiveMessageAction doBuild() {

        // If no endpoint was set explicitly, use the default endpoint given by api
        if (getEndpoint() == null && getEndpointUri() == null) {
            endpoint(generatedApi.getEndpoint());
        }

        return super.doBuild();
    }
}
