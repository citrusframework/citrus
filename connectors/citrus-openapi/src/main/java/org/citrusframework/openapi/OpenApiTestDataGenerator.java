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

package org.citrusframework.openapi;

import java.util.Map;
import java.util.stream.Collectors;

import io.apicurio.datamodels.openapi.models.OasSchema;
import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.openapi.model.OasModelHelper;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Generates proper payloads and validation expressions based on Open API specification rules. Creates outbound payloads
 * with generated random test data according to specification and creates inbound payloads with proper validation expressions to
 * enforce the specification rules.
 *
 * @author Christoph Deppisch
 */
public class OpenApiTestDataGenerator {

    /**
     * Creates payload from schema for outbound message.
     * @param schema
     * @param definitions
     * @return
     */
    public static String createOutboundPayload(OasSchema schema, Map<String, OasSchema> definitions,
                                               OpenApiSpecification specification) {
        if (OasModelHelper.isReferenceType(schema)) {
            OasSchema resolved = definitions.get(OasModelHelper.getReferenceName(schema.$ref));
            return createOutboundPayload(resolved, definitions, specification);
        }

        StringBuilder payload = new StringBuilder();
        if (OasModelHelper.isObjectType(schema)) {
            payload.append("{");

            if (schema.properties != null) {
                for (Map.Entry<String, OasSchema> entry : schema.properties.entrySet()) {
                    if (specification.isGenerateOptionalFields() || isRequired(schema, entry.getKey())) {
                        payload.append("\"")
                                .append(entry.getKey())
                                .append("\": ")
                                .append(createRandomValueExpression(entry.getValue(), definitions, true, specification))
                                .append(",");
                    }
                }
            }

            if (payload.toString().endsWith(",")) {
                payload.replace(payload.length() - 1, payload.length(), "");
            }

            payload.append("}");
        } else if (OasModelHelper.isArrayType(schema)) {
            payload.append("[");
            payload.append(createRandomValueExpression((OasSchema) schema.items, definitions, true, specification));
            payload.append("]");
        } else {
            payload.append(createRandomValueExpression(schema, definitions, true, specification));
        }

        return payload.toString();
    }

    /**
     * Use test variable with given name if present or create value from schema with random values
     * @param schema
     * @param definitions
     * @param quotes
     * @return
     */
    public static String createRandomValueExpression(String name, OasSchema schema, Map<String, OasSchema> definitions,
                                                     boolean quotes, OpenApiSpecification specification, TestContext context) {
        if (context.getVariables().containsKey(name)) {
            return CitrusSettings.VARIABLE_PREFIX + name + CitrusSettings.VARIABLE_SUFFIX;
        }

        return createRandomValueExpression(schema, definitions, quotes, specification);
    }

    /**
     * Create payload from schema with random values.
     * @param schema
     * @param definitions
     * @param quotes
     * @return
     */
    public static String createRandomValueExpression(OasSchema schema, Map<String, OasSchema> definitions, boolean quotes,
                                                     OpenApiSpecification specification) {
        if (OasModelHelper.isReferenceType(schema)) {
            OasSchema resolved = definitions.get(OasModelHelper.getReferenceName(schema.$ref));
            return createRandomValueExpression(resolved, definitions, quotes, specification);
        }

        StringBuilder payload = new StringBuilder();
        if (OasModelHelper.isObjectType(schema) || OasModelHelper.isArrayType(schema)) {
            payload.append(createOutboundPayload(schema, definitions, specification));
        } else if ("string".equals(schema.type)) {
            if (quotes) {
                payload.append("\"");
            }

            if (schema.format != null && schema.format.equals("date")) {
                payload.append("citrus:currentDate()");
            } else if (schema.format != null && schema.format.equals("date-time")) {
                payload.append("citrus:currentDate('yyyy-MM-dd'T'hh:mm:ss')");
            } else if (StringUtils.hasText(schema.pattern)) {
                payload.append("citrus:randomValue(").append(schema.pattern).append(")");
            } else if (!CollectionUtils.isEmpty(schema.enum_)) {
                payload.append("citrus:randomEnumValue(").append(schema.enum_.stream().map(value -> "'" + value + "'").collect(Collectors.joining(","))).append(")");
            } else if (schema.format != null && schema.format.equals("uuid")) {
                payload.append("citrus:randomUUID()");
            } else {
                payload.append("citrus:randomString(").append(schema.maxLength != null && schema.maxLength.intValue() > 0 ? schema.maxLength : (schema.minLength != null && schema.minLength.intValue() > 0 ? schema.minLength : 10)).append(")");
            }

            if (quotes) {
                payload.append("\"");
            }
        } else if ("integer".equals(schema.type) || "number".equals(schema.type)) {
            payload.append("citrus:randomNumber(8)");
        } else if ("boolean".equals(schema.type)) {
            payload.append("citrus:randomEnumValue('true', 'false')");
        } else if (quotes) {
            payload.append("\"\"");
        }

        return payload.toString();
    }

    /**
     * Creates control payload from schema for validation.
     * @param schema
     * @param definitions
     * @return
     */
    public static String createInboundPayload(OasSchema schema, Map<String, OasSchema> definitions,
                                              OpenApiSpecification specification) {
        if (OasModelHelper.isReferenceType(schema)) {
            OasSchema resolved = definitions.get(OasModelHelper.getReferenceName(schema.$ref));
            return createInboundPayload(resolved, definitions, specification);
        }

        StringBuilder payload = new StringBuilder();
        if (OasModelHelper.isObjectType(schema)) {
            payload.append("{");

            if (schema.properties != null) {
                for (Map.Entry<String, OasSchema> entry : schema.properties.entrySet()) {
                    if (specification.isValidateOptionalFields() || isRequired(schema, entry.getKey())) {
                        payload.append("\"")
                                .append(entry.getKey())
                                .append("\": ")
                                .append(createValidationExpression(entry.getValue(), definitions, true, specification))
                                .append(",");
                    }
                }
            }

            if (payload.toString().endsWith(",")) {
                payload.replace(payload.length() - 1, payload.length(), "");
            }

            payload.append("}");
        } else if (OasModelHelper.isArrayType(schema)) {
            payload.append("[");
            payload.append(createValidationExpression((OasSchema) schema.items, definitions, true, specification));
            payload.append("]");
        } else {
            payload.append(createValidationExpression(schema, definitions, false, specification));
        }

        return payload.toString();
    }

    /**
     * Checks if given field name is in list of required fields for this schema.
     * @param schema
     * @param field
     * @return
     */
    private static boolean isRequired(OasSchema schema, String field) {
        if (schema.required == null) {
            return true;
        }

        return schema.required.contains(field);
    }

    /**
     * Use test variable with given name if present or create validation expression using functions according to schema type and format.
     * @param name
     * @param schema
     * @param definitions
     * @param quotes
     * @param context
     * @return
     */
    public static String createValidationExpression(String name, OasSchema schema, Map<String, OasSchema> definitions,
                                                    boolean quotes, OpenApiSpecification specification,
                                                    TestContext context) {
        if (context.getVariables().containsKey(name)) {
            return CitrusSettings.VARIABLE_PREFIX + name + CitrusSettings.VARIABLE_SUFFIX;
        }

        return createValidationExpression(schema, definitions, quotes, specification);
    }

    /**
     * Create validation expression using functions according to schema type and format.
     * @param schema
     * @param definitions
     * @param quotes
     * @return
     */
    public static String createValidationExpression(OasSchema schema, Map<String, OasSchema> definitions, boolean quotes,
                                                    OpenApiSpecification specification) {
        if (OasModelHelper.isReferenceType(schema)) {
            OasSchema resolved = definitions.get(OasModelHelper.getReferenceName(schema.$ref));
            return createValidationExpression(resolved, definitions, quotes, specification);
        }

        StringBuilder payload = new StringBuilder();
        if (OasModelHelper.isObjectType(schema)) {
            payload.append("{");

            if (schema.properties != null) {
                for (Map.Entry<String, OasSchema> entry : schema.properties.entrySet()) {
                    if (specification.isValidateOptionalFields() || isRequired(schema, entry.getKey())) {
                        payload.append("\"")
                                .append(entry.getKey())
                                .append("\": ")
                                .append(createValidationExpression(entry.getValue(), definitions, quotes, specification))
                                .append(",");
                    }
                }
            }

            if (payload.toString().endsWith(",")) {
                payload.replace(payload.length() - 1, payload.length(), "");
            }

            payload.append("}");
        } else {
            if (quotes) {
                payload.append("\"");
            }

            payload.append(createValidationExpression(schema));

            if (quotes) {
                payload.append("\"");
            }
        }

        return payload.toString();
    }

    /**
     * Create validation expression using functions according to schema type and format.
     * @param schema
     * @return
     */
    private static String createValidationExpression(OasSchema schema) {
        switch (schema.type) {
            case "string":
                if (schema.format != null && schema.format.equals("date")) {
                    return "@matchesDatePattern('yyyy-MM-dd')@";
                } else if (schema.format != null && schema.format.equals("date-time")) {
                    return "@matchesDatePattern('yyyy-MM-dd'T'hh:mm:ss')@";
                } else if (StringUtils.hasText(schema.pattern)) {
                    return String.format("@matches(%s)@", schema.pattern);
                } else if (!CollectionUtils.isEmpty(schema.enum_)) {
                    return String.format("@matches(%s)@", String.join("|", schema.enum_));
                } else {
                    return "@notEmpty()@";
                }
            case "number":
            case "integer":
                return "@isNumber()@";
            case "boolean":
                return "@matches(true|false)@";
            default:
                return "@ignore@";
        }
    }

    /**
     * Use test variable with given name (if present) or create random value expression using functions according to
     * schema type and format.
     * @param name
     * @param schema
     * @param context
     * @return
     */
    public static String createRandomValueExpression(String name, OasSchema schema, TestContext context) {
        if (context.getVariables().containsKey(name)) {
            return CitrusSettings.VARIABLE_PREFIX + name + CitrusSettings.VARIABLE_SUFFIX;
        }

        return createRandomValueExpression(schema);
    }

    /**
     * Create random value expression using functions according to schema type and format.
     * @param schema
     * @return
     */
    public static String createRandomValueExpression(OasSchema schema) {
        switch (schema.type) {
            case "string":
                if (schema.format != null && schema.format.equals("date")) {
                    return "\"citrus:currentDate('yyyy-MM-dd')\"";
                } else if (schema.format != null && schema.format.equals("date-time")) {
                    return "\"citrus:currentDate('yyyy-MM-dd'T'hh:mm:ss')\"";
                } else if (StringUtils.hasText(schema.pattern)) {
                    return "\"citrus:randomValue(" + schema.pattern + ")\"";
                } else if (!CollectionUtils.isEmpty(schema.enum_)) {
                    return "\"citrus:randomEnumValue(" + (String.join(",", schema.enum_)) + ")\"";
                } else if (schema.format != null && schema.format.equals("uuid")){
                    return "citrus:randomUUID()";
                } else {
                    return "citrus:randomString(10)";
                }
            case "number":
            case "integer":
                return "citrus:randomNumber(8)";
            case "boolean":
                return "citrus:randomEnumValue('true', 'false')";
            default:
                return "";
        }
    }

}
