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
import io.apicurio.datamodels.openapi.models.OasResponse;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30MediaType;
import io.apicurio.datamodels.openapi.v3.models.Oas30Operation;
import io.apicurio.datamodels.openapi.v3.models.Oas30Parameter;
import io.apicurio.datamodels.openapi.v3.models.Oas30RequestBody;
import io.apicurio.datamodels.openapi.v3.models.Oas30Response;
import jakarta.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.citrusframework.openapi.model.OasModelHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

public final class Oas30ModelHelper {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(Oas30ModelHelper.class);
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
            try {
                return new URL(serverUrl).getHost();
            } catch (MalformedURLException e) {
                throw new IllegalStateException(String.format(NO_URL_ERROR_MESSAGE, serverUrl));
            }
        }

        return "localhost";
    }

    public static List<String> getSchemes(Oas30Document openApiDoc) {
        if (openApiDoc.servers == null || openApiDoc.servers.isEmpty()) {
            return Collections.emptyList();
        }

        return openApiDoc.servers.stream()
                .map(Oas30ModelHelper::resolveUrl)
                .map(serverUrl -> {
                    try {
                        return new URL(serverUrl).getProtocol();
                    } catch (MalformedURLException e) {
                        LOG.warn(String.format(NO_URL_ERROR_MESSAGE, serverUrl));
                        return null;
                    }
                })
        .filter(Objects::nonNull)
        .toList();
    }

    public static String getBasePath(Oas30Document openApiDoc) {
        if (openApiDoc.servers == null || openApiDoc.servers.isEmpty()) {
            return "/";
        }

        Server server = openApiDoc.servers.get(0);
        String basePath;

        String serverUrl = resolveUrl(server);
        if (serverUrl.startsWith("http")) {
            try {
                basePath = new URL(serverUrl).getPath();
            } catch (MalformedURLException e) {
                throw new IllegalStateException(String.format(NO_URL_ERROR_MESSAGE, serverUrl));
            }
        } else {
            basePath = serverUrl;
        }

        return basePath.startsWith("/") ? basePath : "/" + basePath;
    }

    public static Map<String, OasSchema> getSchemaDefinitions(Oas30Document openApiDoc) {
        if (openApiDoc.components == null || openApiDoc.components.schemas == null) {
            return Collections.emptyMap();
        }

        return openApiDoc.components.schemas.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Entry::getValue));
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

    public static Optional<OasSchema> getRequestBodySchema(Oas30Document openApiDoc, Oas30Operation operation) {
        if (operation.requestBody == null) {
            return Optional.empty();
        }

        Oas30RequestBody bodyToUse = operation.requestBody;

        if (openApiDoc.components != null
                && openApiDoc.components.requestBodies != null
                && bodyToUse.$ref != null) {
            bodyToUse = openApiDoc.components.requestBodies.get(OasModelHelper.getReferenceName(bodyToUse.$ref));
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

    public static Collection<String> getResponseTypes(@Nullable  Oas30Operation operation, Oas30Response response) {
        if (operation == null) {
            return Collections.emptySet();
        }
        return response.content != null ? response.content.keySet() : Collections.emptyList();
    }

    /**
     * Returns the response content for random response generation. Note that this implementation currently only returns {@link MediaType#APPLICATION_JSON_VALUE},
     * if this type exists. Otherwise, it will return an empty Optional. The reason for this is, that we cannot safely guess the type other than for JSON.
     *
     * @param openApiDoc
     * @param operation
     * @return
     */
    public static Optional<String> getResponseContentTypeForRandomGeneration(Oas30Document openApiDoc, Oas30Operation operation) {
        Optional<OasResponse> responseForRandomGeneration = getResponseForRandomGeneration(
            openApiDoc, operation);
        return responseForRandomGeneration.map(
                Oas30Response.class::cast).flatMap(res -> res.content.entrySet()
                .stream()
            .filter(entry -> MediaType.APPLICATION_JSON_VALUE.equals(entry.getKey()))
                .filter(entry -> entry.getValue().schema != null)
                .map(Map.Entry::getKey)
                .findFirst());
    }

    public static Optional<OasResponse> getResponseForRandomGeneration(Oas30Document openApiDoc, Oas30Operation operation) {
        if (operation.responses == null) {
            return Optional.empty();
        }

        List<OasResponse> responses = OasModelHelper.resolveResponses(operation.responses,
            responseRef -> openApiDoc.components.responses.get(OasModelHelper.getReferenceName(responseRef)));

        // Pick the response object related to the first 2xx return code found
        Optional<OasResponse> response = responses.stream()
            .filter(Oas30Response.class::isInstance)
            .filter(r -> r.getStatusCode() != null && r.getStatusCode().startsWith("2"))
            .map(OasResponse.class::cast)
            .filter(res -> OasModelHelper.getSchema(res).isPresent())
            .findFirst();

        // No 2xx response given so pick the first one no matter what status code
        if (response.isEmpty()) {
            // TODO: This behavior differs from OAS2 and is very likely a bug because it may result in returning error messages.
            // According to the specification, there MUST be at least one response, which SHOULD be a successful response.
            // If the response is NOT A SUCCESSFUL one, we encounter an error case, which is likely not the intended behavior.
            // The specification likely does not intend to define operations that always fail. On the other hand, it is not
            // against the spec to NOT document an OK response that is empty.
            // For testing purposes, note that the difference between OAS2 and OAS3 is evident in the Petstore API.
            // The Petstore API specifies successful response codes for OAS3 but lacks these definitions for OAS2.
            // Therefore, while tests pass for OAS3, they fail for OAS2.
            // I would suggest to return an empty response in case we fail to resolve a good response, as in Oas2.
            // In case of absence of a response an OK response will be sent as default.
            response = responses.stream()
                .filter(Oas30Response.class::isInstance)
                .map(OasResponse.class::cast)
                .filter(res -> OasModelHelper.getSchema(res).isPresent())
                .findFirst();
        }

        return response;
    }

    public static Map<String, OasSchema> getRequiredHeaders(Oas30Response response) {
        if (response.headers == null) {
            return Collections.emptyMap();
        }

        return response.headers.entrySet()
                .stream()
                .filter(entry -> Boolean.TRUE.equals(entry.getValue().required))
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().schema));
    }

    public static Map<String, OasSchema> getHeaders(Oas30Response response) {
        if (response.headers == null) {
            return Collections.emptyMap();
        }

        return response.headers.entrySet()
                                .stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().schema));
    }

    private static boolean isFormDataMediaType(String type) {
        return Arrays.asList("application/x-www-form-urlencoded", "multipart/form-data").contains(type);
    }

    /**
     * Resolve given server url and replace variable placeholders if any with default variable values. Open API 3.x
     * supports variables with placeholders in form {variable_name} (e.g. "http://{hostname}:{port}/api/v1").
     * @param server the server holding a URL with maybe variable placeholders.
     * @return the server URL with all placeholders resolved or "/" by default.
     */
    private static String resolveUrl(Server server) {
        String url = Optional.ofNullable(server.url).orElse("/");
        if (server.variables != null) {
            for (Map.Entry<String, ServerVariable> variable: server.variables.entrySet()) {
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
