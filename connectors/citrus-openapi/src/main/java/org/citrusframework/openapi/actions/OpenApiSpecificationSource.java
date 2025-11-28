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

package org.citrusframework.openapi.actions;

import java.util.Objects;

import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.openapi.OpenApiRepository;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.spi.ReferenceResolver;

import static org.citrusframework.openapi.util.OpenApiUtils.getKnownOpenApiAliases;
import static org.citrusframework.util.StringUtils.isEmpty;

/**
 * The {@code OpenApiSpecificationSource} class is responsible for managing and resolving an
 * {@link OpenApiSpecification} instance. It can either directly contain an
 * {@link OpenApiSpecification} or reference one by an alias.
 */
public class OpenApiSpecificationSource {

    private OpenApiSpecification openApiSpecification;

    private String openApiAlias;

    private String httpClient;

    public OpenApiSpecificationSource() {
    }

    public OpenApiSpecificationSource(OpenApiSpecification openApiSpecification) {
        this.openApiSpecification = openApiSpecification;
    }

    public OpenApiSpecificationSource(String openApiAlias) {
        this.openApiAlias = openApiAlias;
    }

    public OpenApiSpecification resolve(ReferenceResolver resolver) {
        if (openApiSpecification == null) {

            if (!isEmpty(openApiAlias)) {
                openApiSpecification = resolver.resolveAll(OpenApiRepository.class).values()
                    .stream()
                    .map(openApiRepository -> openApiRepository.openApi(openApiAlias))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseGet(() -> resolver.resolveAll(OpenApiSpecification.class).values().stream()
                        .filter(specification -> specification.getAliases().contains(openApiAlias))
                        .findFirst()
                        .orElseThrow(() ->
                            new CitrusRuntimeException(
                                "Unable to resolve OpenAPI specification from alias '%s'. Known aliases are '%s'".formatted(
                                    openApiAlias, getKnownOpenApiAliases(resolver)))
                        ));
            } else {
                throw new CitrusRuntimeException(
                    "Unable to resolve OpenApiSpecification. Neither OpenAPI specification, nor OpenAPI alias are specified.");
            }
        }

        if (httpClient != null) {
            openApiSpecification.setHttpClient(httpClient);
        }

        return openApiSpecification;
    }

    public void setHttpClient(String httpClient) {
        this.httpClient = httpClient;
    }

    public void setHttpClient(Endpoint endpoint) {
        if (endpoint instanceof HttpClient client) {
            if (client.getEndpointConfiguration().getRequestUrl() != null) {
                this.httpClient = client.getEndpointConfiguration().getRequestUrl();
            }
        }
    }

    public void setOpenApiAlias(String openApiAlias) {
        this.openApiAlias = openApiAlias;
    }

    public void setOpenApiSpecification(OpenApiSpecification openApiSpecification) {
        this.openApiSpecification = openApiSpecification;
    }
}
