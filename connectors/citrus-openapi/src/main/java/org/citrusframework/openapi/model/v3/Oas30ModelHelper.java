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

package org.citrusframework.openapi.model.v3;

import io.apicurio.datamodels.core.models.common.Server;
import io.apicurio.datamodels.core.models.common.ServerVariable;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30Header;
import io.apicurio.datamodels.openapi.v3.models.Oas30MediaType;
import io.apicurio.datamodels.openapi.v3.models.Oas30Operation;
import io.apicurio.datamodels.openapi.v3.models.Oas30Parameter;
import io.apicurio.datamodels.openapi.v3.models.Oas30RequestBody;
import io.apicurio.datamodels.openapi.v3.models.Oas30Response;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.openapi.model.OasAdapter;
import org.citrusframework.openapi.model.OasModelHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;
import static org.citrusframework.openapi.model.OasModelHelper.DEFAULT_ACCEPTED_MEDIA_TYPES;
import static org.citrusframework.openapi.model.OasModelHelper.getReferenceName;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

public final class Oas30ModelHelper {

    private static final Logger logger = LoggerFactory.getLogger(Oas30ModelHelper.class);

    public static final String NO_URL_ERROR_MESSAGE = "Unable to determine base path from server URL: %s";

    private Oas30ModelHelper() {
        // utility class
    }

    public static String getHost(Oas30Document openApiDoc) {
        if (openApiDoc.servers == null || openApiDoc.servers.isEmpty()) {
            return "localhost";
        }

        String serverUrl = resolveUrl(openApiDoc.servers.get(0));
        if (serverUrl.startsWith("http")) {
            return URI.create(serverUrl).getHost();
        }

        return "localhost";
    }

    public static List<String> getSchemes(Oas30Document openApiDoc) {
        if (openApiDoc.servers == null || openApiDoc.servers.isEmpty()) {
            return emptyList();
        }

        return openApiDoc.servers.stream()
                .map(Oas30ModelHelper::resolveUrl)
                .map(serverUrl -> {
                    try {
                        return URI.create(serverUrl).toURL().getProtocol();
                    } catch (Exception e) {
                        logger.warn(format(NO_URL_ERROR_MESSAGE, serverUrl), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public static boolean isCompositeSchema(Oas30Schema schema) {
        return schema.anyOf != null || schema.oneOf != null || schema.allOf != null;
    }

    public static String getBasePath(Oas30Document openApiDoc) {
        if (openApiDoc.servers == null || openApiDoc.servers.isEmpty()) {
            return "/";
        }

        Server server = openApiDoc.servers.get(0);
        String basePath;

        String serverUrl = resolveUrl(server);
        if (serverUrl.startsWith("http")) {
            basePath = URI.create(serverUrl).getPath();
        } else {
            basePath = serverUrl;
        }

        return basePath.startsWith("/") ? basePath : "/" + basePath;
    }

    public static Map<String, OasSchema> getSchemaDefinitions(Oas30Document openApiDoc) {
        if (openApiDoc.components == null || openApiDoc.components.schemas == null) {
            return emptyMap();
        }

        return openApiDoc.components.schemas.entrySet()
                .stream()
                .collect(toMap(Map.Entry::getKey, Entry::getValue));
    }

    public static Optional<OasSchema> getSchema(Oas30Response response) {
        Map<String, Oas30MediaType> content = response.content;
        if (content == null) {
            return Optional.empty();
        }

        return content.entrySet()
                .stream()
                .filter(entry -> !isFormDataMediaType(entry.getKey()))
                .filter(entry -> entry.getValue().schema != null)
                .map(entry -> (OasSchema) entry.getValue().schema)
                .findFirst();
    }

    public static Optional<OasAdapter<OasSchema, String>> getSchema(Oas30Operation ignoredOas30Operation, Oas30Response response, List<String> acceptedMediaTypes) {
        acceptedMediaTypes = OasModelHelper.resolveAllTypes(acceptedMediaTypes);
        acceptedMediaTypes = acceptedMediaTypes.isEmpty() ? DEFAULT_ACCEPTED_MEDIA_TYPES : acceptedMediaTypes;

        Map<String, Oas30MediaType> content = response.content;
        if (content == null) {
            return Optional.empty();
        }

        String selectedMediaType = null;
        Oas30Schema selectedSchema = null;
        for (String type : acceptedMediaTypes) {
            if (!isFormDataMediaType(type)) {
                Oas30MediaType oas30MediaType = content.get(type);
                if (oas30MediaType != null) {
                    selectedMediaType = type;
                    selectedSchema = oas30MediaType.schema;
                    break;
                }
            }
        }

        return selectedSchema == null && selectedMediaType == null ? Optional.empty() : Optional.of(new OasAdapter<>(selectedSchema, selectedMediaType));
    }

    public static Optional<OasSchema> getRequestBodySchema(Oas30Document openApiDoc, Oas30Operation operation) {
        if (operation.requestBody == null) {
            return Optional.empty();
        }

        Oas30RequestBody bodyToUse = operation.requestBody;

        if (openApiDoc.components != null
                && openApiDoc.components.requestBodies != null
                && bodyToUse.$ref != null) {
            bodyToUse = openApiDoc.components.requestBodies.get(getReferenceName(bodyToUse.$ref));
        }

        if (bodyToUse.content == null) {
            return Optional.empty();
        }

        return bodyToUse.content.entrySet()
                .stream()
                .filter(entry -> !isFormDataMediaType(entry.getKey()))
                .filter(entry -> entry.getValue().schema != null)
                .findFirst()
                .map(Map.Entry::getValue)
                .map(oas30MediaType -> oas30MediaType.schema);
    }

    public static boolean isOperationRequestBodyRequired(Oas30Document openApiDoc, Oas30Operation operation) {
        return operation.requestBody != null && Boolean.TRUE == operation.requestBody.required;
    }

    public static Optional<String> getRequestContentType(Oas30Operation operation) {
        if (operation.requestBody == null || operation.requestBody.content == null) {
            return Optional.empty();
        }

        return operation.requestBody.content.entrySet()
                .stream()
                .filter(entry -> entry.getValue().schema != null)
                .map(Map.Entry::getKey)
                .findFirst();
    }

    public static Collection<String> getResponseTypes(Oas30Operation operation, Oas30Response response) {
        if (operation == null) {
            return emptySet();
        }

        return response.content != null ? response.content.keySet() : emptyList();
    }

    public static Map<String, OasSchema> getRequiredHeaders(Oas30Document oasDocument, Oas30Response response) {
        if (response.headers == null) {
            return emptyMap();
        }

        return response.headers.entrySet()
                .stream()
                .filter(entry -> Boolean.TRUE.equals(entry.getValue().required))
                .map((entry) -> Map.entry(entry.getKey(), resolveRequiredSchema(oasDocument, entry.getValue())))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String, OasSchema> getHeaders(Oas30Document oasDocument, Oas30Response response) {
        if (response.headers == null) {
            return emptyMap();
        }

        return response.headers.entrySet()
                .stream()
                .map((entry) -> Map.entry(entry.getKey(), resolveRequiredSchema(oasDocument, entry.getValue())))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static OasSchema resolveRequiredSchema(Oas30Document oasDocument, Oas30Header oas30Header) {
        OasSchema oasSchema;

        if (isNull(oas30Header.schema)) {
            oasSchema = getSchemaDefinitions(oasDocument)
                    .get(getReferenceName(oas30Header.getReference()));
        } else {
            oasSchema = oas30Header.schema;
        }

        if (isNull(oasSchema)) {
            throw new CitrusRuntimeException("Failed to resolve schema in OpenAPI specification, tried reference as well!");
        }

        return oasSchema;
    }

    private static boolean isFormDataMediaType(String type) {
        return Arrays.asList(APPLICATION_FORM_URLENCODED_VALUE, MULTIPART_FORM_DATA_VALUE).contains(type);
    }

    /**
     * Resolve given server url and replace variable placeholders if any with default variable values. Open API 3.x
     * supports variables with placeholders in form {variable_name} (e.g. "http://{hostname}:{port}/api/v1").
     *
     * @param server the server holding a URL with maybe variable placeholders.
     * @return the server URL with all placeholders resolved or "/" by default.
     */
    private static String resolveUrl(Server server) {
        String url = Optional.ofNullable(server.url).orElse("/");
        if (server.variables != null) {
            for (Map.Entry<String, ServerVariable> variable : server.variables.entrySet()) {
                String defaultValue = Optional.ofNullable(variable.getValue().default_).orElse("");
                url = url.replaceAll(String.format("\\{%s\\}", variable.getKey()), defaultValue);
            }
        }

        return url;
    }

    public static Optional<OasSchema> getParameterSchema(Oas30Parameter parameter) {
        return Optional.ofNullable((OasSchema) parameter.schema);
    }
}
