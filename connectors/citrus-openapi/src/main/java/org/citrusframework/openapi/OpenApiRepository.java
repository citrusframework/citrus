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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.synchronizedList;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.citrusframework.openapi.validation.OpenApiValidationPolicy;
import org.citrusframework.repository.BaseRepository;
import org.citrusframework.spi.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private boolean requestValidationEnabled = true;

    private boolean responseValidationEnabled = true;

    private OpenApiValidationPolicy openApiValidationPolicy = OpenApiSettings.getOpenApiValidationPolicy();

    public OpenApiRepository() {
        super(DEFAULT_NAME);
    }

    /**
     * @param openApiResource the OpenAPI resource from which to determine the alias
     * @return an {@code Optional} containing the resource alias if it can be resolved, otherwise an
     * empty {@code Optional}
     */
    // Package protection for testing
    static Optional<String> determineResourceAlias(Resource openApiResource) {
        String resourceAlias = null;

        try {
            File file = openApiResource.getFile();
            if (file != null) {
                resourceAlias = file.getName();
                int index = resourceAlias.lastIndexOf(".");
                if (index != -1 && index != resourceAlias.length() - 1) {
                    resourceAlias = resourceAlias.substring(0, index);
                }
                return Optional.of(resourceAlias);
            }
        } catch (Exception e) {
            // Ignore and try with url
        }

        try {
            URL url = openApiResource.getURL();
            if (url != null) {
                String urlString = URLDecoder.decode(url.getPath(), UTF_8).replace("\\", "/");
                int index = urlString.lastIndexOf("/");
                resourceAlias = urlString;
                if (index != -1 && index != urlString.length() - 1) {
                    resourceAlias = resourceAlias.substring(index + 1);
                }
                index = resourceAlias.lastIndexOf(".");
                if (index != -1 && index != resourceAlias.length() - 1) {
                    resourceAlias = resourceAlias.substring(0, index);
                }

            }
        } catch (MalformedURLException e) {
            logger.error("Unable to determine resource alias from resource!", e);
        }

        return Optional.ofNullable(resourceAlias);
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

    public boolean isRequestValidationEnabled() {
        return requestValidationEnabled;
    }

    public void setRequestValidationEnabled(boolean requestValidationEnabled) {
        this.requestValidationEnabled = requestValidationEnabled;
    }

    public boolean isResponseValidationEnabled() {
        return responseValidationEnabled;
    }

    public void setResponseValidationEnabled(boolean responseValidationEnabled) {
        this.responseValidationEnabled = responseValidationEnabled;
    }

    public OpenApiValidationPolicy getOpenApiValidationPolicy() {
        return openApiValidationPolicy;
    }

    public void setOpenApiValidationPolicy(
        OpenApiValidationPolicy openApiValidationPolicy) {
        this.openApiValidationPolicy = openApiValidationPolicy;
    }


    /**
     * Adds an OpenAPI Specification specified by the given resource to the repository. If an alias
     * is determined from the resource name, it is added to the specification.
     *
     * @param openApiResource the resource to add as an OpenAPI specification
     */
    @Override
    public void addRepository(Resource openApiResource) {
        OpenApiSpecification openApiSpecification = OpenApiSpecification.from(openApiResource,
            openApiValidationPolicy);
        determineResourceAlias(openApiResource).ifPresent(openApiSpecification::addAlias);
        openApiSpecification.setApiRequestValidationEnabled(requestValidationEnabled);
        openApiSpecification.setApiResponseValidationEnabled(responseValidationEnabled);
        openApiSpecification.setRootContextPath(rootContextPath);

        addRepository(openApiSpecification);
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
