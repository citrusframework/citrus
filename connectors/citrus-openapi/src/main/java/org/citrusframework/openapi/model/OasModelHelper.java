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

package org.citrusframework.openapi.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.apicurio.datamodels.openapi.models.OasDocument;
import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasPathItem;
import io.apicurio.datamodels.openapi.models.OasPaths;
import io.apicurio.datamodels.openapi.models.OasResponse;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.apicurio.datamodels.openapi.v2.models.Oas20Response;
import io.apicurio.datamodels.openapi.v3.models.Oas30Document;
import io.apicurio.datamodels.openapi.v3.models.Oas30Operation;
import io.apicurio.datamodels.openapi.v3.models.Oas30Response;
import org.citrusframework.openapi.model.v2.Oas20ModelHelper;
import org.citrusframework.openapi.model.v3.Oas30ModelHelper;

/**
 * @author Christoph Deppisch
 */
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
        return "object".equals(schema.type);
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
     * Determines if given schema has a reference to another schema object.
     * @param schema to check
     * @return true if given schema has a reference.
     */
    public static boolean isReferenceType(OasSchema schema) {
        return schema.$ref != null;
    }

    public static String getHost(OasDocument openApiDoc) {
        return delegate(openApiDoc, Oas20ModelHelper::getHost, Oas30ModelHelper::getHost);
    }

    public static List<String> getSchemes(OasDocument openApiDoc) {
        return delegate(openApiDoc, Oas20ModelHelper::getSchemes, Oas30ModelHelper::getSchemes);
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

    public static Optional<String> getResponseContentType(OasDocument openApiDoc, OasOperation operation) {
        return delegate(openApiDoc, operation, Oas20ModelHelper::getResponseContentType, Oas30ModelHelper::getResponseContentType);
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
        if (response instanceof Oas20Response) {
            return oas20Function.apply((Oas20Response) response);
        } else if (response instanceof Oas30Response) {
            return oas30Function.apply((Oas30Response) response);
        }

        throw new IllegalArgumentException(String.format("Unsupported operation response type: %s", response.getClass()));
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
        if (operation instanceof Oas20Operation) {
            return oas20Function.apply((Oas20Operation) operation);
        } else if (operation instanceof Oas30Operation) {
            return oas30Function.apply((Oas30Operation) operation);
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
}
