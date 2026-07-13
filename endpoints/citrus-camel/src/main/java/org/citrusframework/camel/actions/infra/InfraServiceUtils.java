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

package org.citrusframework.camel.actions.infra;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.camel.catalog.CamelCatalog;
import org.apache.camel.catalog.DefaultCamelCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class provides access to Camel infra services metadata.
 */
public final class InfraServiceUtils {

    private static final Logger logger = LoggerFactory.getLogger(InfraServiceUtils.class);

    private static final ObjectMapper jsonMapper = JsonMapper.builder()
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    private InfraServiceUtils() {
        //prevent instantiation of utility class
    }

    /**
     * Reads available infra services metadata from Camel catalog.
     */
    public static List<InfraService> getInfraServiceMetadata(CamelCatalog catalog) throws IOException {
        List<InfraService> metadata;

        try (InputStream is
                     = catalog.loadResource("test-infra", "metadata.json")) {
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            metadata = jsonMapper.readValue(json, new TypeReference<>() {});
        }

        return metadata;
    }

    /**
     * Search for infra service in given Camel catalog metadata that matches the given service name and implementation combination.
     * The implementation alias is optional.
     */
    public static Optional<InfraService> resolveInfraService(CamelCatalog catalog, String serviceName, String implementation) throws IOException {
        return resolveInfraService(getInfraServiceMetadata(catalog), serviceName, implementation);
    }

    /**
     * Find infra service in the given list of services that matches the given service name and implementation combination.
     * The implementation alias is optional. If not present the method returns the infra service that matches the service name only.
     */
    public static Optional<InfraService> resolveInfraService(List<InfraService> services, String serviceName, String implementation) {
        return services
                .stream()
                .filter(service -> {
                    if (implementation != null && !implementation.isEmpty()
                            && service.aliasImplementation() != null) {
                        return service.alias().contains(serviceName)
                                && service.aliasImplementation().contains(implementation);
                    } else if (implementation == null) {
                        return service.alias().contains(serviceName)
                                && (service.aliasImplementation() == null || service.aliasImplementation().isEmpty());
                    }

                    return false;
                })
                .findFirst();
    }

    /**
     * Provide a list of available Camel infra services names.
     * When an infra service has multiple aliases and multiple implementation aliases the list of names contains a combination of these values using
     * "service_alias.implementation_alias".
     */
    public static Set<String> getInfraServiceNames() {
        try {
            return InfraServiceUtils.getInfraServiceMetadata(new DefaultCamelCatalog())
                    .stream()
                    .map(is -> {
                        if (is.aliasImplementation() != null && !is.aliasImplementation().isEmpty()) {
                            return is.aliasImplementation().stream().map(impl -> is.alias().get(0) + "." + impl).collect(Collectors.toList());
                        } else {
                            return is.alias();
                        }
                    })
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            logger.warn("Failed to load Infra service names from Camel catalog meta data", e);
            return Collections.emptySet();
        }
    }
}
