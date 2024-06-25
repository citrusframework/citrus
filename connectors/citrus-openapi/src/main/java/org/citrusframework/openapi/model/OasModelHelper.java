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

package org.citrusframework.openapi.model;

import io.apicurio.datamodels.combined.visitors.CombinedVisitorAdapter;
import io.apicurio.datamodels.openapi.models.OasDocument;
import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasParameter;
import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.models.OasPaths;
import io.apicurio.datamodels.openapi.models.OasResponse;
import io.apicurio.datamodels.openapi.models.OasResponses;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.apicurio.datamodels.openapi.v2.models.Oas20Parameter;
import io.apicurio.datamodels.openapi.v2.models.Oas20Response;
import io.apicurio.datamodels.openapi.v2.models.Oas20Schema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30Operation;
import io.apicurio.datamodels.openapi.v3.models.Oas30Parameter;
import io.apicurio.datamodels.openapi.v3.models.Oas30Response;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import jakarta.annotation.Nullable;
import org.citrusframework.openapi.model.v2.Oas20ModelHelper;
import org.citrusframework.openapi.model.v3.Oas30ModelHelper;
import org.citrusframework.util.StringUtils;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Collections.singletonList;

public final class OasModelHelper {

    public static final String DEFAULT_ = "default_";

    /**
     * List of preferred media types in the order of priority,
     * used when no specific 'Accept' header is provided to determine the default response type.
     */
    public static final List<String> DEFAULT_ACCEPTED_MEDIA_TYPES = List.of(MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE);

    private OasModelHelper() {
        // utility class
    }

    /**
     * Determines if given schema is of type object.
     * @param schema to check
     * @return true if given schema is an object.
     */
    public static boolean isObjectType(@Nullable OasSchema schema) {
        return schema != null && "object".equals(schema.type);
    }

    /**
     * Determines if given schema is of type array.
     * @param schema to check
     * @return true if given schema is an array.
     */
    public static boolean isArrayType(@Nullable OasSchema schema) {
        return schema != null && "array".equals(schema.type);
    }

    /**
     * Determines if given schema is of type object array .
     * @param schema to check
     * @return true if given schema is an object array.
     */
    public static boolean isObjectArrayType(@Nullable OasSchema schema) {

        if (schema == null || !"array".equals(schema.type)) {
            return false;
        }

        Object items = schema.items;
        if (items instanceof  OasSchema oasSchema) {
            return isObjectType(oasSchema);
        } else if (items instanceof  List<?> list) {
            return list.stream().allMatch(item -> item instanceof OasSchema oasSchema && isObjectType(oasSchema));
        }

        return false;
    }

    /**
     * Determines if given schema has a reference to another schema object.
     * @param schema to check
     * @return true if given schema has a reference.
     */
    public static boolean isReferenceType(@Nullable OasSchema schema) {
        return schema != null && schema.$ref != null;
    }

    public static boolean isCompositeSchema(OasSchema schema) {
        return delegate(schema, Oas20ModelHelper::isCompositeSchema, Oas30ModelHelper::isCompositeSchema);
    }

    public static String getHost(OasDocument openApiDoc) {
        return delegate(openApiDoc, Oas20ModelHelper::getHost, Oas30ModelHelper::getHost);
    }

    public static List<String> getSchemes(OasDocument openApiDoc) {
        return delegate(openApiDoc, Oas20ModelHelper::getSchemes, Oas30ModelHelper::getSchemes);
    }

    public static OasSchema resolveSchema(OasDocument oasDocument, OasSchema schema) {
        if (isReferenceType(schema)) {
            return getSchemaDefinitions(oasDocument).get(getReferenceName(schema.$ref));
        }

        return schema;
    }

    public static String getBasePath(OasDocument openApiDoc) {
        return delegate(openApiDoc, Oas20ModelHelper::getBasePath, Oas30ModelHelper::getBasePath);
    }

    public static Map<String, OasSchema> getSchemaDefinitions(OasDocument openApiDoc) {
        return delegate(openApiDoc, Oas20ModelHelper::getSchemaDefinitions, Oas30ModelHelper::getSchemaDefinitions);
    }

    /**
     * Iterate through list of generic path items and collect path items of given type.
     * @param paths given path items.
     * @return typed list of path items.
     */
    public static List<OasPathItem> getPathItems(OasPaths paths) {
        if (paths == null) {
            return Collections.emptyList();
        }

        return paths.getItems();
    }

    /**
     * Construct map of all specified operations for given path item. Only non-null operations are added to the
     * map where the key is the http method name.
     * @param pathItem path holding operations.
     * @return map of operations on the given path where Http method name is the key.
     */
    public static Map<String, OasOperation> getOperationMap(OasPathItem pathItem) {
        Map<String, OasOperation> operations = new LinkedHashMap<>();

        if (pathItem.get != null) {
            operations.put("get", pathItem.get);
        }

        if (pathItem.put != null) {
            operations.put("put", pathItem.put);
        }

        if (pathItem.post != null) {
            operations.put("post", pathItem.post);
        }

        if (pathItem.delete != null) {
            operations.put("delete", pathItem.delete);
        }

        if (pathItem.options != null) {
            operations.put("options", pathItem.options);
        }

        if (pathItem.head != null) {
            operations.put("head", pathItem.head);
        }

        if (pathItem.patch != null) {
            operations.put("patch", pathItem.patch);
        }

        return operations;
    }

    /**
     * Get pure name from reference path. Usually reference definitions start with '#/definitions/' for OpenAPI 2.x and
     * '#/components/schemas/' for OpenAPI 3.x and this method removes the basic reference path part and just returns the
     * reference object name.
     * @param reference path expression.
     * @return the name of the reference object.
     */
    public static String getReferenceName(String reference) {
        if (reference != null) {
            return reference.replaceAll("^.*/", "");
        }

        return null;
    }

    public static Optional<OasSchema> getSchema(OasResponse response) {
        return delegate(response, Oas20ModelHelper::getSchema, Oas30ModelHelper::getSchema);
    }

    public static Optional<OasAdapter<OasSchema, String>> getSchema(OasOperation oasOperation, OasResponse response, List<String> acceptedMediaTypes) {
        if (oasOperation instanceof  Oas20Operation oas20Operation && response instanceof Oas20Response oas20Response) {
            return Oas20ModelHelper.getSchema(oas20Operation, oas20Response, acceptedMediaTypes);
        } else if (oasOperation instanceof Oas30Operation oas30Operation && response instanceof Oas30Response oas30Response) {
            return Oas30ModelHelper.getSchema(oas30Operation, oas30Response, acceptedMediaTypes);
        }
        throw new IllegalArgumentException(String.format("Unsupported operation response type: %s", response.getClass()));
    }

    public static Optional<OasSchema> getParameterSchema(OasParameter parameter) {
        return delegate(parameter, Oas20ModelHelper::getParameterSchema, Oas30ModelHelper::getParameterSchema);
    }

    public static Map<String, OasSchema> getRequiredHeaders(OasResponse response) {
        return delegate(response, Oas20ModelHelper::getHeaders, Oas30ModelHelper::getRequiredHeaders);
    }

    public static Map<String, OasSchema> getHeaders(OasResponse response) {
        return delegate(response, Oas20ModelHelper::getHeaders, Oas30ModelHelper::getHeaders);
    }

    public static Optional<String> getRequestContentType(OasOperation operation) {
        return delegate(operation, Oas20ModelHelper::getRequestContentType, Oas30ModelHelper::getRequestContentType);
    }

    public static Optional<OasSchema> getRequestBodySchema(OasDocument openApiDoc, OasOperation operation) {
        return delegate(openApiDoc, operation, Oas20ModelHelper::getRequestBodySchema, Oas30ModelHelper::getRequestBodySchema);
    }

    public static Collection<String> getResponseTypes(OasOperation operation, OasResponse response) {
        return delegate(operation, response, Oas20ModelHelper::getResponseTypes, Oas30ModelHelper::getResponseTypes);
    }

    /**
     * Determines the appropriate random response from an OpenAPI Specification operation based on the given status code.
     * If a status code is specified, return the response for the specified status code. May be empty.
     * <p>
     * If no exact match is found:
     * <ul>
     *     <li>Fallback 1: Returns the 'default_' response if it exists.</li>
     *     <li>Fallback 2: Returns the first response object related to a 2xx status code that contains an acceptable schema for random message generation.</li>
     *     <li>Fallback 3: Returns the first response object related to a 2xx status code even without a schema. This is for operations that simply do not return anything else than a status code.</li>
     *     <li>Fallback 4: Returns the first response in the list of responses, no matter which schema.</li>
     * </ul>
     *
     * Note that for Fallback 3 and 4, it is very likely, that there is no schema specified. It is expected, that an empty response is a viable response in these cases.
     *
     * @param openApiDoc The OpenAPI document containing the API specifications.
     * @param operation The OAS operation for which to determine the response.
     * @param statusCode The specific status code to match against responses, or {@code null} to search for any acceptable response.
     * @param accept The mediatype accepted by the request
     * @return An {@link Optional} containing the resolved {@link OasResponse} if found, or {@link Optional#empty()} otherwise.
     */
    public static Optional<OasResponse> getResponseForRandomGeneration(OasDocument openApiDoc, OasOperation operation, @Nullable String statusCode, @Nullable String accept) {

        if (operation.responses == null || operation.responses.getResponses().isEmpty()) {
            return Optional.empty();
        }

        // Resolve all references
        Map<String, OasResponse> responseMap = OasModelHelper.resolveResponses(openApiDoc,
            operation.responses);

        // For a given status code, do not fall back
        if (statusCode != null) {
            return Optional.ofNullable(responseMap.get(statusCode));
        }

        // Only accept responses that provide a schema for which we can actually provide a random message
        Predicate<OasResponse> acceptedSchemas = resp -> getSchema(operation, resp, accept != null ? singletonList(accept) : DEFAULT_ACCEPTED_MEDIA_TYPES).isPresent();

        // Fallback 1: Pick the default if it exists
        Optional<OasResponse> response = Optional.ofNullable(responseMap.get(DEFAULT_));

        if (response.isEmpty()) {
            // Fallback 2: Pick the response object related to the first 2xx, providing an accepted schema
            response = responseMap.values().stream()
                .filter(r -> r.getStatusCode() != null && r.getStatusCode().startsWith("2"))
                .map(OasResponse.class::cast)
                .filter(acceptedSchemas)
                .findFirst();
        }

        if (response.isEmpty()) {
            // Fallback 3: Pick the response object related to the first 2xx (even without schema)
            response = responseMap.values().stream()
                .filter(r -> r.getStatusCode() != null && r.getStatusCode().startsWith("2"))
                .map(OasResponse.class::cast)
                .findFirst();
        }

        if (response.isEmpty()) {
            // Fallback 4: Pick the first response no matter which schema
            response = operation.responses.getResponses().stream()
                .map(resp -> responseMap.get(resp.getStatusCode()))
                .filter(Objects::nonNull)
                    .findFirst();
        }

        return response;
    }

    /**
     * Delegate method to version specific model helpers for Open API v2 or v3.
     * @param openApiDoc the open api document either v2 or v3
     * @param oas20Function function to apply in case of v2
     * @param oas30Function function to apply in case of v3
     * @param <T> generic return value
     * @return
     */
    private static <T> T delegate(OasDocument openApiDoc, Function<Oas20Document, T> oas20Function, Function<Oas30Document, T> oas30Function) {
        if (isOas20(openApiDoc)) {
            return oas20Function.apply((Oas20Document) openApiDoc);
        } else if (isOas30(openApiDoc)) {
            return oas30Function.apply((Oas30Document) openApiDoc);
        }

        throw new IllegalArgumentException(String.format("Unsupported Open API document type: %s", openApiDoc.getClass()));
    }

    /**
     * Delegate method to version specific model helpers for Open API v2 or v3.
     * @param response
     * @param oas20Function function to apply in case of v2
     * @param oas30Function function to apply in case of v3
     * @param <T> generic return value
     * @return
     */
    private static <T> T delegate(OasResponse response, Function<Oas20Response, T> oas20Function, Function<Oas30Response, T> oas30Function) {
        if (response instanceof Oas20Response oas20Response) {
            return oas20Function.apply(oas20Response);
        } else if (response instanceof Oas30Response oas30Response) {
            return oas30Function.apply(oas30Response);
        }

        throw new IllegalArgumentException(String.format("Unsupported operation response type: %s", response.getClass()));
    }

    /**
     * Delegate method to version specific model helpers for Open API v2 or v3.
     * @param response
     * @param oas20Function function to apply in case of v2
     * @param oas30Function function to apply in case of v3
     * @param <T> generic return value
     * @return
     */
    private static <T> T delegate(OasOperation operation, OasResponse response, BiFunction<Oas20Operation, Oas20Response, T> oas20Function, BiFunction<Oas30Operation, Oas30Response, T> oas30Function) {
        if (operation instanceof Oas20Operation oas20Operation && response instanceof  Oas20Response oas20Response) {
            return oas20Function.apply(oas20Operation, oas20Response);
        } else if (operation instanceof Oas30Operation oas30Operation && response instanceof  Oas30Response oas30Response) {
            return oas30Function.apply(oas30Operation, oas30Response);
        }

        throw new IllegalArgumentException(String.format("Unsupported operation response type: %s", response.getClass()));
    }

    /**
     * Delegate method to version specific model helpers for Open API v2 or v3.
     * @param parameter
     * @param oas20Function function to apply in case of v2
     * @param oas30Function function to apply in case of v3
     * @param <T> generic return value
     * @return
     */
    private static <T> T delegate(OasParameter parameter, Function<Oas20Parameter, T> oas20Function, Function<Oas30Parameter, T> oas30Function) {
        if (parameter instanceof Oas20Parameter oas20Parameter) {
            return oas20Function.apply(oas20Parameter);
        } else if (parameter instanceof Oas30Parameter oas30Parameter) {
            return oas30Function.apply(oas30Parameter);
        }

        throw new IllegalArgumentException(String.format("Unsupported operation parameter type: %s", parameter.getClass()));
    }

    /**
     * Delegate method to version specific model helpers for Open API v2 or v3.
     * @param schema
     * @param oas20Function function to apply in case of v2
     * @param oas30Function function to apply in case of v3
     * @param <T> generic return value
     * @return
     */
    private static <T> T delegate(OasSchema schema, Function<Oas20Schema, T> oas20Function, Function<Oas30Schema, T> oas30Function) {
        if (schema instanceof Oas20Schema oas20Schema) {
            return oas20Function.apply(oas20Schema);
        } else if (schema instanceof Oas30Schema oas30Schema) {
            return oas30Function.apply(oas30Schema);
        }

        throw new IllegalArgumentException(String.format("Unsupported operation parameter type: %s", schema.getClass()));
    }

    /**
     * Delegate method to version specific model helpers for Open API v2 or v3.
     * @param operation
     * @param oas20Function function to apply in case of v2
     * @param oas30Function function to apply in case of v3
     * @param <T> generic return value
     * @return
     */
    private static <T> T delegate(OasOperation operation, Function<Oas20Operation, T> oas20Function, Function<Oas30Operation, T> oas30Function) {
        if (operation instanceof Oas20Operation oas20Operation) {
            return oas20Function.apply(oas20Operation);
        } else if (operation instanceof Oas30Operation oas30Operation) {
            return oas30Function.apply(oas30Operation);
        }

        throw new IllegalArgumentException(String.format("Unsupported operation type: %s", operation.getClass()));
    }

    /**
     * Delegate method to version specific model helpers for Open API v2 or v3.
     *
     * @param openApiDoc
     * @param operation
     * @param oas20Function function to apply in case of v2
     * @param oas30Function function to apply in case of v3
     * @param <T> generic return value
     * @return
     */
    private static <T> T delegate(OasDocument openApiDoc, OasOperation operation, BiFunction<Oas20Document, Oas20Operation, T> oas20Function, BiFunction<Oas30Document, Oas30Operation, T> oas30Function) {
        if (isOas20(openApiDoc)) {
            return oas20Function.apply((Oas20Document) openApiDoc, (Oas20Operation) operation);
        } else if (isOas30(openApiDoc)) {
            return oas30Function.apply((Oas30Document) openApiDoc, (Oas30Operation) operation);
        }

        throw new IllegalArgumentException(String.format("Unsupported Open API document type: %s", openApiDoc.getClass()));
    }

    private static boolean isOas30(OasDocument openApiDoc) {
        return OpenApiVersion.fromDocumentType(openApiDoc).equals(OpenApiVersion.V3);
    }

    private static boolean isOas20(OasDocument openApiDoc) {
        return OpenApiVersion.fromDocumentType(openApiDoc).equals(OpenApiVersion.V2);
    }

    /**
     * Resolves all responses in the given {@link OasResponses} instance.
     *
     * <p>
     * This method iterates over the responses contained in the {@link OasResponses} object. If a response has a reference
     * (indicated by a non-null {@code $ref} field), it resolves the reference and adds the resolved response to the result list.
     * Non-referenced responses are added to the result list as-is. The resulting map includes the default response under
     * the key {@link OasModelHelper#DEFAULT_}, if it exists.
     * </p>
     *
     * @param responses the {@link OasResponses} instance containing the responses to be resolved.
     * @return a {@link List} of {@link OasResponse} instances, where all references have been resolved.
     */
    private static Map<String, OasResponse> resolveResponses(OasDocument openApiDoc, OasResponses responses) {

        Function<String, OasResponse> responseResolver = getResponseResolver(
            openApiDoc);

        Map<String, OasResponse> responseMap = new HashMap<>();
        for (OasResponse response : responses.getResponses()) {
            if (response.$ref != null) {
                OasResponse resolved = responseResolver.apply(getReferenceName(response.$ref));
                if (resolved != null) {
                    // Note that we need to get the statusCode from the ref, as the referenced does not know about it.
                    responseMap.put(response.getStatusCode(), resolved);
                }
            } else {
                responseMap.put(response.getStatusCode(), response);
            }
        }

        if (responses.default_ != null) {
            if (responses.default_.$ref != null) {
                OasResponse resolved = responseResolver.apply(responses.default_.$ref);
                if (resolved != null) {
                    responseMap.put(DEFAULT_, resolved);
                }
            } else {
                responseMap.put(DEFAULT_, responses.default_);
            }
        }

        return responseMap;
    }

    private static Function<String, OasResponse> getResponseResolver(
        OasDocument openApiDoc) {
        return delegate(openApiDoc,
            (Function<Oas20Document, Function<String, OasResponse>>) doc -> (responseRef -> doc.responses.getResponse(OasModelHelper.getReferenceName(responseRef))),
            (Function<Oas30Document, Function<String, OasResponse>>) doc -> (responseRef -> doc.components.responses.get(OasModelHelper.getReferenceName(responseRef))));
    }

    /**
     * Traverses the OAS document and applies the given visitor to each OAS operation found.
     * This method uses the provided {@link OasOperationVisitor} to process each operation within the paths of the OAS document.
     *
     * @param oasDocument the OAS document to traverse
     * @param visitor the visitor to apply to each OAS operation
     */
    public static void visitOasOperations(OasDocument oasDocument, OasOperationVisitor visitor) {
        if (oasDocument == null || visitor == null) {
            return;
        }

        oasDocument.paths.accept(new CombinedVisitorAdapter() {

            @Override
            public void visitPaths(OasPaths oasPaths) {
                oasPaths.getPathItems().forEach(oasPathItem -> oasPathItem.accept(this));
            }

            @Override
            public void visitPathItem(OasPathItem oasPathItem) {
                String path = oasPathItem.getPath();

                if (StringUtils.isEmpty(path)) {
                    return;
                }

                getOperationMap(oasPathItem).values()
                    .forEach(oasOperation -> visitor.visit(oasPathItem, oasOperation));

            }
        });
    }

    /**
     * Resolves and normalizes a list of accepted media types. If the input list is null,
     * returns null. Otherwise, splits each media type string by comma, trims whitespace,
     * and collects them into a list of normalized types.
     *
     * @param acceptedMediaTypes List of accepted media types, may be null.
     * @return Normalized list of media types, or null if input is null.
     */
    public static List<String> resolveAllTypes(@Nullable List<String> acceptedMediaTypes) {
        if (acceptedMediaTypes == null) {
            return acceptedMediaTypes;
        }

        return acceptedMediaTypes.stream()
            .flatMap(types -> Arrays.stream(types.split(","))).map(String::trim).toList();
    }

}
