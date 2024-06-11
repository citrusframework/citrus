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
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30Operation;
import io.apicurio.datamodels.openapi.v3.models.Oas30Parameter;
import io.apicurio.datamodels.openapi.v3.models.Oas30Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.citrusframework.openapi.model.v2.Oas20ModelHelper;
import org.citrusframework.openapi.model.v3.Oas30ModelHelper;

public final class OasModelHelper {

    private OasModelHelper() {
        // utility class
    }

    /**
     * Determines if given schema is of type object.
     * @param schema to check
     * @return true if given schema is an object.
     */
    public static boolean isObjectType(OasSchema schema) {
        return schema != null && "object".equals(schema.type);
    }

    /**
     * Determines if given schema is of type array.
     * @param schema to check
     * @return true if given schema is an array.
     */
    public static boolean isArrayType(OasSchema schema) {
        return "array".equals(schema.type);
    }

    /**
     * Determines if given schema is of type object array .
     * @param schema to check
     * @return true if given schema is an object array.
     */
    public static boolean isObjectArrayType(OasSchema schema) {
        if (schema == null ||  !"array".equals(schema.type)) {
            return false;
        }
        Object items = schema.items;
        if (items instanceof OasSchema oasSchema) {
            return isObjectType(oasSchema);
        } else if (items instanceof List<?> list) {
            return list.stream().allMatch(item -> item instanceof OasSchema oasSchema && isObjectType(oasSchema));
        }

        return false;
    }

    /**
     * Determines if given schema has a reference to another schema object.
     * @param schema to check
     * @return true if given schema has a reference.
     */
    public static boolean isReferenceType(OasSchema schema) {
        return schema != null && schema.$ref != null;
    }

    public static String getHost(OasDocument openApiDoc) {
        return delegate(openApiDoc, Oas20ModelHelper::getHost, Oas30ModelHelper::getHost);
    }

    public static List<String> getSchemes(OasDocument openApiDoc) {
        return delegate(openApiDoc, Oas20ModelHelper::getSchemes, Oas30ModelHelper::getSchemes);
    }

    public static OasSchema resolveSchema(OasDocument oasDocument, OasSchema schema) {
        if (isReferenceType(schema)) {
            return getSchemaDefinitions(oasDocument).get(schema.$ref);
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
     * Determines the appropriate response from an OAS (OpenAPI Specification) operation.
     * The method looks for the response status code within the range 200 to 299 and returns
     * the corresponding response if one is found. The first response in the list of responses,
     * that satisfies the constraint will be returned. (TODO: see comment in Oas30ModelHelper) If none of the responses has a 2xx status code,
     * the first response in the list will be returned.
     *
     */
    public static Optional<OasResponse> getResponseForRandomGeneration(OasDocument openApiDoc, OasOperation operation) {
        return delegate(openApiDoc, operation, Oas20ModelHelper::getResponseForRandomGeneration, Oas30ModelHelper::getResponseForRandomGeneration);
    }

    /**
     * Returns the response type used for random response generation. See specific helper implementations for detail.
     */
    public static Optional<String> getResponseContentTypeForRandomGeneration(OasDocument openApiDoc, OasOperation operation) {
        return delegate(openApiDoc, operation, Oas20ModelHelper::getResponseContentTypeForRandomGeneration, Oas30ModelHelper::getResponseContentTypeForRandomGeneration);
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
     * Resolves all responses in the given {@link OasResponses} instance using the provided {@code responseResolver} function.
     *
     * <p>This method iterates over the responses contained in the {@link OasResponses} object. If a response has a reference
     * (indicated by a non-null {@code $ref} field), the reference is resolved using the {@code responseResolver} function. Other responses
     * will be added to the result list as is.</p>
     *
     * @param responses        the {@link OasResponses} instance containing the responses to be resolved.
     * @param responseResolver a {@link Function} that takes a reference string and returns the corresponding {@link OasResponse}.
     * @return a {@link List} of {@link OasResponse} instances, where all references have been resolved.
     */
    public static List<OasResponse> resolveResponses(OasResponses responses, Function<String, OasResponse> responseResolver) {

        List<OasResponse> responseList = new ArrayList<>();
        for (OasResponse response : responses.getResponses()) {
            if (response.$ref != null) {
                OasResponse resolved = responseResolver.apply(getReferenceName(response.$ref));
                if (resolved != null) {
                    responseList.add(resolved);
                }
            } else {
                responseList.add(response);
            }
        }

        return responseList;
    }
}
