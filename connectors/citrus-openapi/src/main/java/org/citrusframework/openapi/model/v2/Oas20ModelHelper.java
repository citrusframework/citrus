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
import io.apicurio.datamodels.openapi.v2.models.Oas20Items;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.apicurio.datamodels.openapi.v2.models.Oas20Parameter;
import io.apicurio.datamodels.openapi.v2.models.Oas20Response;
import io.apicurio.datamodels.openapi.v2.models.Oas20Schema;
import io.apicurio.datamodels.openapi.v2.models.Oas20Schema.Oas20AllOfSchema;
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

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

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

    public static boolean isCompositeSchema(Oas20Schema schema) {
        // Note that oneOf and anyOf is not supported by Oas20.
        return schema instanceof Oas20AllOfSchema;
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
        return Arrays.asList(APPLICATION_FORM_URLENCODED_VALUE, MULTIPART_FORM_DATA_VALUE).contains(type);
    }

    /**
     * If the header already contains a schema (and it is an instance of {@link Oas20Header}), this schema is returned.
     * Otherwise, a new {@link Oas20Header} is created based on the properties of the parameter and returned.
     *
     * @param header the {@link Oas20Header} from which to extract or create the schema
     * @return an {@link Optional} containing the extracted or newly created {@link OasSchema}
     */
    private static OasSchema getHeaderSchema(Oas20Header header) {
        return createOas20Schema(header.getName(), header.type, header.format, header.items, header.multipleOf, header.default_, header.enum_, header.pattern, header.description, header.uniqueItems, header.maximum, header.maxItems, header.maxLength, header.exclusiveMaximum, header.minimum, header.minItems, header.minLength, header.exclusiveMinimum);
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

        Oas20Schema schema = createOas20Schema(parameter.getName(), parameter.type, parameter.format, parameter.items, parameter.multipleOf, parameter.default_, parameter.enum_, parameter.pattern, parameter.description, parameter.uniqueItems, parameter.maximum, parameter.maxItems, parameter.maxLength, parameter.exclusiveMaximum, parameter.minimum, parameter.minItems, parameter.minLength, parameter.exclusiveMinimum);
        return Optional.of(schema);
    }

    private static Oas20Schema createOas20Schema(String name, String type, String format, Oas20Items items, Number multipleOf, Object aDefault, List<String> anEnum, String pattern, String description, Boolean uniqueItems, Number maximum, Number maxItems, Number maxLength, Boolean exclusiveMaximum, Number minimum, Number minItems, Number minLength, Boolean exclusiveMinimum) {
        Oas20Schema schema = new Oas20Schema();

        schema.title = name;
        schema.type = type;
        schema.format = format;
        schema.items = items;
        schema.multipleOf = multipleOf;

        schema.default_ = aDefault;
        schema.enum_ = anEnum;

        schema.pattern = pattern;
        schema.description = description;
        schema.uniqueItems = uniqueItems;

        schema.maximum = maximum;
        schema.maxItems = maxItems;
        schema.maxLength = maxLength;
        schema.exclusiveMaximum = exclusiveMaximum;

        schema.minimum = minimum;
        schema.minItems = minItems;
        schema.minLength = minLength;
        schema.exclusiveMinimum = exclusiveMinimum;

        return schema;
    }
}
