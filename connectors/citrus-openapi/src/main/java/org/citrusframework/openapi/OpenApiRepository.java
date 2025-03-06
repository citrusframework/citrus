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

package org.citrusframework.openapi;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.openapi.validation.OpenApiValidationPolicy;
import org.citrusframework.repository.BaseRepository;
import org.citrusframework.spi.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;
import static java.util.Collections.synchronizedList;
import static org.citrusframework.openapi.OpenApiSettings.isNeglectBasePathGlobally;
import static org.citrusframework.openapi.OpenApiSettings.isRequestValidationEnabledGlobally;
import static org.citrusframework.openapi.OpenApiSettings.isResponseValidationEnabledGlobally;

/**
 * OpenApi repository holding a set of {@link OpenApiSpecification} known in the test scope.
 *
 * @since 4.4.0
 */
public class OpenApiRepository extends BaseRepository {

    private static final Logger logger = LoggerFactory.getLogger(OpenApiRepository.class);

    private static final String DEFAULT_NAME = "openApiSchemaRepository";

    /**
     * List of specifications
     */
    private final List<OpenApiSpecification> openApiSpecifications = synchronizedList(
        new ArrayList<>());

    /**
     * An optional context path, used for each api, without taking into account any
     * {@link OpenApiSpecification} specific context path.
     */
    private String rootContextPath;

    /**
     * Flag to indicate whether the base path of the OpenAPI should be part of the path or not.
     */
    private boolean neglectBasePath = isNeglectBasePathGlobally();

    /**
     * Flag to indicate whether OpenAPIs managed by this repository should perform request validation.
     */
    private boolean requestValidationEnabled = isRequestValidationEnabledGlobally();

    /**
     * Flag to indicate whether OpenAPIs managed by this repository should perform response validation.
     */
    private boolean responseValidationEnabled = isResponseValidationEnabledGlobally();

    private OpenApiValidationPolicy validationPolicy = OpenApiSettings.getOpenApiValidationPolicy();

    public OpenApiRepository() {
        super(DEFAULT_NAME);
    }



    public List<OpenApiSpecification> getOpenApiSpecifications() {
        return openApiSpecifications;
    }

    public String getRootContextPath() {
        return rootContextPath;
    }

    public void setRootContextPath(String rootContextPath) {
        this.rootContextPath = rootContextPath;
    }

    public OpenApiRepository rootContextPath(String rootContextPath) {
        setRootContextPath(rootContextPath);
        return this;
    }

    public boolean isNeglectBasePath() {
        return neglectBasePath;
    }

    public void setNeglectBasePath(boolean neglectBasePath) {
        this.neglectBasePath = neglectBasePath;
    }

    public OpenApiRepository neglectBasePath(boolean neglectBasePath) {
        setNeglectBasePath(neglectBasePath);
        return this;
    }

    public boolean isRequestValidationEnabled() {
        return requestValidationEnabled;
    }

    public void setRequestValidationEnabled(boolean requestValidationEnabled) {
        this.requestValidationEnabled = requestValidationEnabled;
    }

    public OpenApiRepository requestValidationEnabled(boolean requestValidationEnabled) {
        setRequestValidationEnabled(requestValidationEnabled);
        return this;
    }

    public boolean isResponseValidationEnabled() {
        return responseValidationEnabled;
    }

    public void setResponseValidationEnabled(boolean responseValidationEnabled) {
        this.responseValidationEnabled = responseValidationEnabled;
    }

    public OpenApiRepository responseValidationEnabled(boolean responseValidationEnabled) {
        setResponseValidationEnabled(responseValidationEnabled);
        return this;
    }

    public OpenApiValidationPolicy getValidationPolicy() {
        return validationPolicy;
    }

    public void setValidationPolicy(
        OpenApiValidationPolicy validationPolicy) {
        this.validationPolicy = validationPolicy;
    }

    public OpenApiRepository validationPolicy(OpenApiValidationPolicy openApiValidationPolicy) {
        setValidationPolicy(openApiValidationPolicy);
        return this;
    }

    /**
     * Adds an OpenAPI Specification specified by the given resource to the repository. If an alias
     * is determined from the resource name, it is added to the specification.
     *
     * @param openApiResource the resource to add as an OpenAPI specification
     */
    @Override
    public void addRepository(Resource openApiResource) {

        try {
            OpenApiSpecification openApiSpecification = OpenApiSpecification.from(openApiResource,
                validationPolicy);
            openApiSpecification.setApiRequestValidationEnabled(requestValidationEnabled);
            openApiSpecification.setApiResponseValidationEnabled(responseValidationEnabled);
            openApiSpecification.setRootContextPath(rootContextPath);
            openApiSpecification.neglectBasePath(neglectBasePath);
            addRepository(openApiSpecification);
        } catch (Exception e) {
            logger.error(format("Unable to read OpenApiSpecification from location: %s", openApiResource.getURI()));
            throw new CitrusRuntimeException(e);
        }

    }

    /**
     * Adds the given OpenAPI specification to this repository and invokes all registered
     * {@link OpenApiSpecificationProcessor}.
     *
     * @param openApiSpecification the OpenAPI specification to add to the repository
     */
    public void addRepository(OpenApiSpecification openApiSpecification) {
        this.openApiSpecifications.add(openApiSpecification);

        OpenApiSpecificationProcessor.lookup()
            .values()
            .forEach(processor -> processor.process(openApiSpecification));
    }

    public OpenApiRepository locations(List<String> locations) {
        setLocations(locations);
        return this;
    }

    public @Nullable OpenApiSpecification openApi(@NotNull String alias) {

        if (alias.equals(getName())) {
            if (openApiSpecifications.size() == 1) {
                return openApiSpecifications.get(0);
            } else {
                throw new IllegalArgumentException(
                    "The alias matches the repository name, but the repository contains multiple specifications. "
                        + "Matching a specification by repository name is only allowed if there is exactly one specification in the repository."
                );
            }
        }

        return getOpenApiSpecifications().stream()
            .filter(spec -> spec.getAliases().contains(alias))
            .findFirst()
            .orElse(null);
    }
}
