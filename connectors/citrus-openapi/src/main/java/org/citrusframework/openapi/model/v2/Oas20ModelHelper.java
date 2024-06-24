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

package org.citrusframework.openapi.model.v2;

import io.apicurio.datamodels.openapi.models.OasHeader;
import io.apicurio.datamodels.openapi.models.OasParameter;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Header;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.apicurio.datamodels.openapi.v2.models.Oas20Parameter;
import io.apicurio.datamodels.openapi.v2.models.Oas20Response;
import io.apicurio.datamodels.openapi.v2.models.Oas20Schema;
import io.apicurio.datamodels.openapi.v2.models.Oas20SchemaDefinition;
import jakarta.annotation.Nullable;
import org.citrusframework.openapi.model.OasAdapter;
import org.citrusframework.openapi.model.OasModelHelper;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class Oas20ModelHelper {

    private Oas20ModelHelper() {
        // utility class
    }

    public static String getHost(Oas20Document openApiDoc) {
        return openApiDoc.host;
    }

    public static List<String> getSchemes(Oas20Document openApiDoc) {
        return openApiDoc.schemes;
    }

    public static String getBasePath(Oas20Document openApiDoc) {
        return Optional.ofNullable(openApiDoc.basePath)
                .map(basePath -> basePath.startsWith("/") ? basePath : "/" + basePath).orElse("/");
    }

    public static Map<String, OasSchema> getSchemaDefinitions(Oas20Document openApiDoc) {
        if (openApiDoc == null
                || openApiDoc.definitions == null) {
            return Collections.emptyMap();
        }

        return openApiDoc.definitions.getDefinitions().stream().collect(Collectors.toMap(Oas20SchemaDefinition::getName, definition -> definition));
    }

    public static Optional<OasSchema> getSchema(Oas20Response response) {
        return Optional.ofNullable(response.schema);
    }

    public static Optional<OasAdapter<OasSchema, String>> getSchema(Oas20Operation oas20Operation, Oas20Response response, List<String> acceptedMediaTypes) {

        acceptedMediaTypes = OasModelHelper.resolveAllTypes(acceptedMediaTypes);
        acceptedMediaTypes = acceptedMediaTypes != null ? acceptedMediaTypes : OasModelHelper.DEFAULT_ACCEPTED_MEDIA_TYPES;

        OasSchema selectedSchema = response.schema;
        String selectedMediaType = null;
        if (oas20Operation.produces != null && !oas20Operation.produces.isEmpty()) {
            selectedMediaType = acceptedMediaTypes.stream()
                .filter(type -> !isFormDataMediaType(type))
                .filter(type -> oas20Operation.produces.contains(type)).findFirst()
                .orElse(null);
        }

        return selectedSchema == null && selectedMediaType == null ? Optional.empty() : Optional.of(new OasAdapter<>(selectedSchema, selectedMediaType));
    }

    public static Optional<OasSchema> getRequestBodySchema(@Nullable Oas20Document ignoredOpenApiDoc, Oas20Operation operation) {
        if (operation.parameters == null) {
            return Optional.empty();
        }

        final List<OasParameter> operationParameters = operation.parameters;

        Optional<OasParameter> body = operationParameters.stream()
                .filter(p -> "body".equals(p.in) && p.schema != null)
                .findFirst();

        return body.map(oasParameter -> (OasSchema) oasParameter.schema);
    }

    public static Optional<String> getRequestContentType(Oas20Operation operation) {
        if (operation.consumes != null) {
            return Optional.of(operation.consumes.get(0));
        }

        return Optional.empty();
    }

    public static Collection<String> getResponseTypes(Oas20Operation operation, @Nullable Oas20Response ignoredResponse) {
        if (operation == null) {
            return Collections.emptyList();
        }
        return operation.produces;
    }

    public static Map<String, OasSchema> getHeaders(Oas20Response response) {
        if (response.headers == null) {
            return Collections.emptyMap();
        }

        return response.headers.getHeaders().stream()
                .collect(Collectors.toMap(OasHeader::getName, Oas20ModelHelper::getHeaderSchema));
    }

    private static boolean isFormDataMediaType(String type) {
        return Arrays.asList("application/x-www-form-urlencoded", "multipart/form-data").contains(type);
    }

    /**
     * If the header already contains a schema (and it is an instance of {@link Oas20Header}), this schema is returned.
     * Otherwise, a new {@link Oas20Header} is created based on the properties of the parameter and returned.
     *
     * @param header the {@link Oas20Header} from which to extract or create the schema
     * @return an {@link Optional} containing the extracted or newly created {@link OasSchema}
     */
    private static OasSchema getHeaderSchema(Oas20Header header) {
        Oas20Schema schema = new Oas20Schema();
        schema.title = header.getName();
        schema.type = header.type;
        schema.format = header.format;
        schema.items = header.items;
        schema.multipleOf = header.multipleOf;

        schema.default_ = header.default_;
        schema.enum_ = header.enum_;

        schema.pattern = header.pattern;
        schema.description = header.description;
        schema.uniqueItems = header.uniqueItems;

        schema.maximum = header.maximum;
        schema.maxItems = header.maxItems;
        schema.maxLength = header.maxLength;
        schema.exclusiveMaximum = header.exclusiveMaximum;

        schema.minimum = header.minimum;
        schema.minItems = header.minItems;
        schema.minLength = header.minLength;
        schema.exclusiveMinimum = header.exclusiveMinimum;
        return schema;
    }

    /**
     * If the parameter already contains a schema (and it is an instance of {@link Oas20Schema}), this schema is returned.
     * Otherwise, a new {@link Oas20Schema} is created based on the properties of the parameter and returned.
     *
     * @param parameter the {@link Oas20Parameter} from which to extract or create the schema
     * @return an {@link Optional} containing the extracted or newly created {@link OasSchema}
     */
    public static Optional<OasSchema> getParameterSchema(Oas20Parameter parameter) {
        if (parameter.schema instanceof Oas20Schema oasSchema) {
            return Optional.of(oasSchema);
        }

        Oas20Schema schema = new Oas20Schema();
        schema.title = parameter.getName();
        schema.type = parameter.type;
        schema.format = parameter.format;
        schema.items = parameter.items;
        schema.multipleOf = parameter.multipleOf;

        schema.default_ = parameter.default_;
        schema.enum_ = parameter.enum_;

        schema.pattern = parameter.pattern;
        schema.description = parameter.description;
        schema.uniqueItems = parameter.uniqueItems;

        schema.maximum = parameter.maximum;
        schema.maxItems = parameter.maxItems;
        schema.maxLength = parameter.maxLength;
        schema.exclusiveMaximum = parameter.exclusiveMaximum;

        schema.minimum = parameter.minimum;
        schema.minItems = parameter.minItems;
        schema.minLength = parameter.minLength;
        schema.exclusiveMinimum = parameter.exclusiveMinimum;

        return Optional.of(schema);
    }
}
