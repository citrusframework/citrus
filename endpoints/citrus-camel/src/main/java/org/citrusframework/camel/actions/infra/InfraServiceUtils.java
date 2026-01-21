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
import java.util.List;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import org.apache.camel.catalog.CamelCatalog;

/**
 * Utility class provides access to Camel infra services metadata.
 */
public final class InfraServiceUtils {

    private static final ObjectMapper jsonMapper = JsonMapper.builder()
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
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

            metadata = jsonMapper.readValue(json, new TypeReference<List<InfraService>>() {
            });
        }

        return metadata;
    }
}
