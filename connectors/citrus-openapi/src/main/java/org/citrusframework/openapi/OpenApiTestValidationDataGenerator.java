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

package org.citrusframework.openapi;

import io.apicurio.datamodels.openapi.models.OasSchema;
import jakarta.annotation.Nullable;
import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.openapi.model.OasModelHelper;
import org.citrusframework.openapi.util.OpenApiUtils;

import java.util.Map;

import static org.citrusframework.util.StringUtils.hasText;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Generates proper payloads and validation expressions based on Open API specification rules.
 * Creates outbound payloads with generated random test data according to specification and creates
 * inbound payloads with proper validation expressions to enforce the specification rules.
 */
public final class OpenApiTestValidationDataGenerator {

    private OpenApiTestValidationDataGenerator() {
        // Static access only
    }

    /**
     * Creates control payload from schema for validation.
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
                    if (specification.isValidateOptionalFields() || isRequired(schema,
                            entry.getKey())) {
                        payload.append("\"")
                                .append(entry.getKey())
                                .append("\": ")
                                .append(createValidationExpression(entry.getValue(), definitions, true,
                                        specification))
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
            payload.append(createValidationExpression((OasSchema) schema.items, definitions, true,
                    specification));
            payload.append("]");
        } else {
            payload.append(createValidationExpression(schema, definitions, false, specification));
        }

        return payload.toString();
    }

    /**
     * Checks if given field name is in list of required fields for this schema.
     */
    private static boolean isRequired(OasSchema schema, String field) {
        return OpenApiUtils.isRequired(schema, field);
    }

    /**
     * Use test variable with given name if present or create validation expression using functions
     * according to schema type and format.
     */
    public static String createValidationExpression(String name, OasSchema schema,
                                                    Map<String, OasSchema> definitions,
                                                    boolean quotes, OpenApiSpecification specification,
                                                    TestContext context) {
        if (context.getVariables().containsKey(name)) {
            return CitrusSettings.VARIABLE_PREFIX + name + CitrusSettings.VARIABLE_SUFFIX;
        }

        return createValidationExpression(schema, definitions, quotes, specification);
    }

    /**
     * Create validation expression using functions according to schema type and format.
     */
    public static String createValidationExpression(OasSchema schema,
                                                    Map<String, OasSchema> definitions, boolean quotes,
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
                    if (specification.isValidateOptionalFields() || isRequired(schema,
                            entry.getKey())) {
                        payload.append("\"")
                                .append(entry.getKey())
                                .append("\": ")
                                .append(
                                        createValidationExpression(entry.getValue(), definitions, quotes,
                                                specification))
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
     */
    private static String createValidationExpression(OasSchema schema) {

        if (OasModelHelper.isCompositeSchema(schema)) {
            /*
             * Currently these schemas are not supported by validation expressions. They are supported
             * by {@link org.citrusframework.openapi.validation.OpenApiValidator} though.
             */
            return "@ignore@";
        }

        switch (schema.type) {
            case OpenApiConstants.TYPE_STRING:
                if (schema.format != null && schema.format.equals("date")) {
                    return "@matchesDatePattern('yyyy-MM-dd')@";
                } else if (schema.format != null && schema.format.equals("date-time")) {
                    return "@matchesDatePattern('yyyy-MM-dd'T'hh:mm:ssZ')@";
                } else if (hasText(schema.pattern)) {
                    return String.format("@matches(%s)@", schema.pattern);
                } else if (!isEmpty(schema.enum_)) {
                    return String.format("@matches(%s)@",
                            String.join("|", schema.enum_));
                } else {
                    return "@notEmpty()@";
                }
            case OpenApiConstants.TYPE_NUMBER, OpenApiConstants.TYPE_INTEGER:
                return "@isNumber()@";
            case OpenApiConstants.TYPE_BOOLEAN:
                return "@matches(true|false)@";
            default:
                return "@ignore@";
        }
    }

    /**
     * Create validation expression using regex according to schema type and format.
     */
    public static String createValidationRegex(String name, @Nullable OasSchema oasSchema) {
        if (OasModelHelper.isReferenceType(oasSchema) || OasModelHelper.isObjectType(oasSchema)) {
            throw new CitrusRuntimeException(String.format(
                    "Unable to create a validation regex for an reference of object schema '%s'!",
                    name));
        }

        return createValidationRegex(oasSchema);
    }

    public static String createValidationRegex(@Nullable OasSchema schema) {

        if (schema == null) {
            return "";
        }

        switch (schema.type) {
            case OpenApiConstants.TYPE_STRING:
                if (schema.format != null && schema.format.equals("date")) {
                    return "\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])";
                } else if (schema.format != null && schema.format.equals("date-time")) {
                    return "\\d{4}-\\d{2}-\\d{2}T[01]\\d:[0-5]\\d:[0-5]\\dZ";
                } else if (hasText(schema.pattern)) {
                    return schema.pattern;
                } else if (!isEmpty(schema.enum_)) {
                    return "(" + String.join("|", schema.enum_) + ")";
                } else if (schema.format != null && schema.format.equals("uuid")) {
                    return "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
                } else {
                    return ".*";
                }
            case OpenApiConstants.TYPE_NUMBER:
                return "[0-9]+\\.?[0-9]*";
            case OpenApiConstants.TYPE_INTEGER:
                return "[0-9]+";
            case OpenApiConstants.TYPE_BOOLEAN:
                return "(true|false)";
            default:
                return "";
        }
    }
}
