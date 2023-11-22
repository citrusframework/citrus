/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.openapi.model.v2;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.apicurio.datamodels.openapi.models.OasHeader;
import io.apicurio.datamodels.openapi.models.OasParameter;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Header;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.apicurio.datamodels.openapi.v2.models.Oas20Response;
import io.apicurio.datamodels.openapi.v2.models.Oas20Schema;
import io.apicurio.datamodels.openapi.v2.models.Oas20SchemaDefinition;

/**
 * @author Christoph Deppisch
 */
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

    public static Optional<OasSchema> getRequestBodySchema(Oas20Document openApiDoc, Oas20Operation operation) {
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

    public static Optional<String> getResponseContentType(Oas20Document openApiDoc, Oas20Operation operation) {
        if (operation.produces != null) {
            return Optional.of(operation.produces.get(0));
        }

        return Optional.empty();
    }

    public static Map<String, OasSchema> getHeaders(Oas20Response response) {
        if (response.headers == null) {
            return Collections.emptyMap();
        }

        return response.headers.getHeaders().stream()
                .collect(Collectors.toMap(OasHeader::getName, Oas20ModelHelper::getHeaderSchema));
    }

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
}
