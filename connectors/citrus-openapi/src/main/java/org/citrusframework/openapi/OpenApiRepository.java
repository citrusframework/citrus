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

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.openapi.validation.OpenApiValidationPolicy;
import org.citrusframework.repository.BaseRepository;
import org.citrusframework.spi.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.synchronizedList;
import static java.util.Objects.nonNull;

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
    private List<OpenApiSpecification> openApiSpecifications = synchronizedList(
            new ArrayList<>()
    );

    /**
     * An optional context path, used for each api, without taking into account any
     * {@link OpenApiSpecification} specific context path.
     */
    private String rootContextPath;

    /**
     * Flag to indicate whether the base path of the OpenAPI should be part of the path or not.
     */
    private boolean neglectBasePath = OpenApiSettings.isNeglectBasePathEnabled();

    /**
     * Flag to indicate whether OpenAPIs managed by this repository should perform request validation.
     */
    private boolean requestValidationEnabled = OpenApiSettings.isRequestValidationEnabled();

    /**
     * Flag to indicate whether OpenAPIs managed by this repository should perform response validation.
     */
    private boolean responseValidationEnabled = OpenApiSettings.isResponseValidationEnabled();

    private OpenApiValidationPolicy validationPolicy = OpenApiSettings.getOpenApiValidationPolicy();

    public OpenApiRepository() {
        super(DEFAULT_NAME);
    }

    public List<OpenApiSpecification> getOpenApiSpecifications() {
        return openApiSpecifications;
    }

    /**
     * This method replaces all currently saved {@link OpenApiSpecification}s, replacing them with the new {@link List<OpenApiSpecification>}.
     * It does make sure that all new specifications are being processed by registered {@link OpenApiSpecificationProcessor}s.
     * <p>
     * It can sometimes be necessary to "rebuild" the whole list of cached/persisted specifications.
     * A real-world use-case, for example, is the synchronization of OpenAPI specifications across multiple instances of the citrus simulator (HA setup).
     * This cannot be achieved in a "transactional" way by just exposing the underlying {@link List<OpenApiSpecification>} itself.
     * The best solution is to flash/replace the {@link OpenApiRepository#openApiSpecifications}.
     *
     * @param openApiSpecifications the replacement for all currently known {@link OpenApiSpecification}s
     * @return the same instance of this {@link OpenApiRepository}, but with motified {@link OpenApiRepository#openApiSpecifications}
     */
    public OpenApiRepository withOpenApiSpecifications(List<OpenApiSpecification> openApiSpecifications) {
        this.openApiSpecifications = synchronizedList(openApiSpecifications);

        var openApiSpecificationProcessors = OpenApiSpecificationProcessor.lookup()
                .values();
        openApiSpecifications.forEach(
                openApiSpecification -> processOpenApiSpecification(openApiSpecificationProcessors, openApiSpecification)
        );

        return this;
    }

    private void processOpenApiSpecification(Collection<OpenApiSpecificationProcessor> openApiSpecificationProcessors, OpenApiSpecification openApiSpecification) {
        openApiSpecificationProcessors.forEach(processor -> processor.process(openApiSpecification));
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

    public void setValidationPolicy(OpenApiValidationPolicy validationPolicy) {
        this.validationPolicy = validationPolicy;
    }

    public OpenApiRepository validationPolicy(OpenApiValidationPolicy openApiValidationPolicy) {
        setValidationPolicy(openApiValidationPolicy);
        return this;
    }

    /**
     * Adds the {@link OpenApiSpecification} defined by the given {@link Resource} to this repository <i>if it does not exist yet</i>.
     * Existence of an equal specification is being detected by comparing the values of {@link OpenApiSpecification#getUid()}, which is essentially a comparison of the content-hashes.
     * <p>
     * If an alias is determined from the resource name, it will be added to the specification.
     * <p>
     * Additionally invokes all registered {@link OpenApiSpecificationProcessor}s for newly registered specifications.
     *
     * @param openApiResource the resource to add as an OpenAPI specification
     */
    @Override
    public void addRepository(Resource openApiResource) {
        try {
            OpenApiSpecification openApiSpecification = OpenApiSpecification.from(openApiResource, validationPolicy);
            openApiSpecification.setApiRequestValidationEnabled(requestValidationEnabled);
            openApiSpecification.setApiResponseValidationEnabled(responseValidationEnabled);
            openApiSpecification.setRootContextPath(rootContextPath);
            openApiSpecification.neglectBasePath(neglectBasePath);

            addRepository(openApiSpecification);
        } catch (Exception e) {
            logger.error("Unable to read OpenApiSpecification from location: {}", openApiResource.getURI());
            throw new CitrusRuntimeException(e);
        }

    }

    /**
     * Adds the given {@link OpenApiSpecification} to this repository <i>if it does not exist yet</i>.
     * Existence of an equal specification is being detected by comparing the values of {@link OpenApiSpecification#getUid()}, which is essentially a comparison of the content-hashes.
     * <p>
     * Additionally invokes all registered {@link OpenApiSpecificationProcessor}s for newly registered specifications.
     *
     * @param openApiSpecification the {@link OpenApiSpecification} to add to the repository
     */
    public void addRepository(OpenApiSpecification openApiSpecification) {
        if (contains(openApiSpecification)) {
            logger.trace("Repository already exists for openApiSpecification: {}", openApiSpecification.getUid());
            return;
        }

        this.openApiSpecifications.add(openApiSpecification);

        processOpenApiSpecification(
                OpenApiSpecificationProcessor.lookup()
                        .values(),
                openApiSpecification
        );
    }

    /**
     * Detects whether this repository already contains the given {@link OpenApiSpecification}, comparing its {@link OpenApiSpecification#getUid()}.
     *
     * @param openApiSpecification the {@link OpenApiSpecification} to check
     * @return true if this OpenAPI specification is already contained within the repository
     */
    public boolean contains(OpenApiSpecification openApiSpecification) {
        return openApiSpecifications.stream()
                .filter(existingOpenApi -> nonNull(existingOpenApi.getUid()))
                .anyMatch(existingOpenApi -> existingOpenApi.getUid().equals(openApiSpecification.getUid()));
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
