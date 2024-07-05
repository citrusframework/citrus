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
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.openapi.model.OasModelHelper;
import org.citrusframework.openapi.util.OpenApiUtils;
import org.citrusframework.openapi.util.RandomModelBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static org.citrusframework.openapi.OpenApiConstants.TYPE_INTEGER;
import static org.citrusframework.util.StringUtils.hasText;
import static org.citrusframework.util.StringUtils.quote;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Generates proper payloads and validation expressions based on Open API specification rules.
 */
public abstract class OpenApiTestDataGenerator {

    public static final BigDecimal THOUSAND = new BigDecimal(1000);
    public static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    public static final BigDecimal MINUS_THOUSAND = new BigDecimal(-1000);

    private OpenApiTestDataGenerator() {
        // Static access only
    }

    private static final Map<String, String> SPECIAL_FORMATS = Map.of(
        "email", "[a-z]{5,15}\\.?[a-z]{5,15}\\@[a-z]{5,15}\\.[a-z]{2}",
        "uri",
        "((http|https)://[a-zA-Z0-9-]+(\\.[a-zA-Z]{2,})+(/[a-zA-Z0-9-]+){1,6})|(file:///[a-zA-Z0-9-]+(/[a-zA-Z0-9-]+){1,6})",
        "hostname",
        "(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])",
        "ipv4",
        "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)",
        "ipv6",
        "(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))");

    /**
     * Creates payload from schema for outbound message.
     */
    public static String createOutboundPayload(OasSchema schema,
        OpenApiSpecification specification) {
        return createOutboundPayload(schema,
            OasModelHelper.getSchemaDefinitions(specification.getOpenApiDoc(null)), specification);
    }

    /**
     * Creates payload from schema for outbound message.
     */
    public static String createOutboundPayload(OasSchema schema, Map<String, OasSchema> definitions,
        OpenApiSpecification specification) {
        return createOutboundPayload(schema, definitions, specification, new HashSet<>());
    }

    /**
     * Creates payload from schema for outbound message.
     */
    private static String createOutboundPayload(OasSchema schema,
        Map<String, OasSchema> definitions,
        OpenApiSpecification specification, Set<OasSchema> visitedRefSchemas) {
        RandomModelBuilder randomModelBuilder = new RandomModelBuilder();
        createOutboundPayloadAsMap(randomModelBuilder, schema, definitions, specification,
            visitedRefSchemas);
        return randomModelBuilder.toString();
    }

    private static void createOutboundPayloadAsMap(RandomModelBuilder randomModelBuilder,
        OasSchema schema,
        Map<String, OasSchema> definitions,
        OpenApiSpecification specification, Set<OasSchema> visitedRefSchemas) {

        if (hasText(schema.$ref) && visitedRefSchemas.contains(schema)) {
            // Avoid recursion
            return;
        }

        if (OasModelHelper.isReferenceType(schema)) {
            OasSchema resolved = definitions.get(OasModelHelper.getReferenceName(schema.$ref));
            createOutboundPayloadAsMap(randomModelBuilder, resolved, definitions, specification,
                visitedRefSchemas);
            return;
        }

        if (OasModelHelper.isCompositeSchema(schema)) {
            createComposedSchema(randomModelBuilder, schema, true, specification,
                visitedRefSchemas);
            return;
        }

        switch (schema.type) {
            case OpenApiConstants.TYPE_OBJECT ->
                createRandomObjectSchemeMap(randomModelBuilder, schema, specification,
                    visitedRefSchemas);
            case OpenApiConstants.TYPE_ARRAY ->
                createRandomArrayValueMap(randomModelBuilder, schema, specification,
                    visitedRefSchemas);
            case OpenApiConstants.TYPE_STRING, TYPE_INTEGER, OpenApiConstants.TYPE_NUMBER, OpenApiConstants.TYPE_BOOLEAN ->
                createRandomValueExpressionMap(randomModelBuilder, schema, true);
            default -> randomModelBuilder.appendSimple("\"\"");
        }
    }

    /**
     * Use test variable with given name if present or create value from schema with random values
     */
    public static String createRandomValueExpression(String name, OasSchema schema,
        Map<String, OasSchema> definitions,
        boolean quotes, OpenApiSpecification specification, TestContext context) {
        if (context.getVariables().containsKey(name)) {
            return CitrusSettings.VARIABLE_PREFIX + name + CitrusSettings.VARIABLE_SUFFIX;
        }

        return createRandomValueExpression(schema, definitions, quotes, specification);
    }

    /**
     * Create payload from schema with random values.
     */
    public static String createRandomValueExpression(OasSchema schema,
        Map<String, OasSchema> definitions, boolean quotes,
        OpenApiSpecification specification) {
        if (OasModelHelper.isReferenceType(schema)) {
            OasSchema resolved = definitions.get(OasModelHelper.getReferenceName(schema.$ref));
            return createRandomValueExpression(resolved, definitions, quotes, specification);
        }

        StringBuilder payload = new StringBuilder();
        if (OasModelHelper.isObjectType(schema) || OasModelHelper.isArrayType(schema)) {
            payload.append(createOutboundPayload(schema, definitions, specification));
        } else if (OpenApiConstants.TYPE_STRING.equals(schema.type)) {
            if (quotes) {
                payload.append("\"");
            }
            if (OpenApiConstants.FORMAT_DATE.equals(schema.format)) {
                payload.append("citrus:currentDate('yyyy-MM-dd')");
            } else if (OpenApiConstants.FORMAT_DATE_TIME.equals(schema.format)) {
                payload.append("citrus:currentDate('yyyy-MM-dd'T'hh:mm:ssZ')");
            } else if (hasText(schema.pattern)) {
                payload.append("citrus:randomValue(").append(schema.pattern).append(")");
            } else if (!isEmpty(schema.enum_)) {
                payload.append("citrus:randomEnumValue(").append(
                    schema.enum_.stream().map(value -> "'" + value + "'")
                        .collect(Collectors.joining(","))).append(")");
            } else if (OpenApiConstants.FORMAT_UUID.equals(schema.format)) {
                payload.append("citrus:randomUUID()");
            } else {
                if (schema.format != null && SPECIAL_FORMATS.containsValue(schema.format)) {
                    payload.append("citrus:randomValue('")
                        .append(SPECIAL_FORMATS.get(schema.format)).append("')");
                } else {
                    int length = 10;
                    if (schema.maxLength != null && schema.maxLength.intValue() > 0) {
                        length = schema.maxLength.intValue();
                    } else if (schema.minLength != null && schema.minLength.intValue() > 0) {
                        length = schema.minLength.intValue();
                    }

                    payload.append("citrus:randomString(").append(length).append(")");
                }
            }

            if (quotes) {
                payload.append("\"");
            }
        } else if (OpenApiUtils.isAnyNumberScheme(schema)) {
            payload.append("citrus:randomNumber(8)");
        } else if (OpenApiConstants.TYPE_BOOLEAN.equals(schema.type)) {
            payload.append("citrus:randomEnumValue('true', 'false')");
        } else if (quotes) {
            payload.append("\"\"");
        }

        return payload.toString();
    }

    public static <T> T createRawRandomValueExpression(OasSchema schema,
        Map<String, OasSchema> definitions, boolean quotes,
        OpenApiSpecification specification, TestContext context) {
        if (OasModelHelper.isReferenceType(schema)) {
            OasSchema resolved = definitions.get(OasModelHelper.getReferenceName(schema.$ref));
            return createRawRandomValueExpression(resolved, definitions, quotes, specification,
                context);
        }

        StringBuilder payload = new StringBuilder();
        if (OpenApiConstants.TYPE_STRING.equals(schema.type) || OasModelHelper.isObjectType(schema)
            || OasModelHelper.isArrayType(schema)) {
            return (T) createRandomValueExpression(schema, definitions, quotes, specification);
        } else if (OpenApiConstants.TYPE_NUMBER.equals(schema.type)) {
            return (T) Double.valueOf(
                context.replaceDynamicContentInString("citrus:randomNumber(8,2)"));
        } else if ("integer".equals(schema.type)) {
            return (T) Double.valueOf(
                context.replaceDynamicContentInString("citrus:randomNumber(8)"));
        } else if ("boolean".equals(schema.type)) {
            return (T) Boolean.valueOf(
                context.replaceDynamicContentInString("citrus:randomEnumValue('true', 'false')"));
        } else if (quotes) {
            payload.append("\"\"");
        }

        return (T) payload.toString();
    }

    /**
     * Checks if given field name is in list of required fields for this schema.
     */
    private static boolean isRequired(OasSchema schema, String field) {
        if (schema.required == null) {
            return true;
        }

        return schema.required.contains(field);
    }

    /**
     * Use test variable with given name (if present) or create random value expression using
     * functions according to schema type and format.
     */
    public static String createRandomValueExpression(String name, OasSchema schema,
        TestContext context) {
        if (context.getVariables().containsKey(name)) {
            return CitrusSettings.VARIABLE_PREFIX + name + CitrusSettings.VARIABLE_SUFFIX;
        }

        RandomModelBuilder randomModelBuilder = new RandomModelBuilder();
        createRandomValueExpressionMap(randomModelBuilder, schema, false);
        return randomModelBuilder.toString();
    }

    public static String createRandomValueExpression(OasSchema schema, boolean quotes) {
        RandomModelBuilder randomModelBuilder = new RandomModelBuilder();
        createRandomValueExpressionMap(randomModelBuilder, schema, quotes);
        return randomModelBuilder.toString();
    }

    /**
     * Create random value expression using functions according to schema type and format.
     */
    private static void createRandomValueExpressionMap(RandomModelBuilder randomModelBuilder,
        OasSchema schema, boolean quotes) {

        switch (schema.type) {
            case OpenApiConstants.TYPE_STRING -> {
                if (OpenApiConstants.FORMAT_DATE.equals(schema.format)) {
                    randomModelBuilder.appendSimple(
                        quote("citrus:currentDate('yyyy-MM-dd')", quotes));
                } else if (OpenApiConstants.FORMAT_DATE_TIME.equals(schema.format)) {
                    randomModelBuilder.appendSimple(
                        quote("citrus:currentDate('yyyy-MM-dd'T'hh:mm:ssZ')", quotes));
                } else if (hasText(schema.pattern)) {
                    randomModelBuilder.appendSimple(
                        quote("citrus:randomValue('" + schema.pattern + "')", quotes));
                } else if (!isEmpty(schema.enum_)) {
                    randomModelBuilder.appendSimple(
                        quote("citrus:randomEnumValue(" + (java.lang.String.join(",", schema.enum_))
                            + ")", quotes));
                } else if (OpenApiConstants.FORMAT_UUID.equals(schema.format)) {
                    randomModelBuilder.appendSimple(quote("citrus:randomUUID()", quotes));
                } else {

                    if (schema.format != null && SPECIAL_FORMATS.containsKey(schema.format)) {
                        randomModelBuilder.appendSimple(quote(
                            "citrus:randomValue('" + SPECIAL_FORMATS.get(schema.format) + "')",
                            quotes));
                    } else {
                        long minLength =
                            schema.minLength != null && schema.minLength.longValue() > 0
                                ? schema.minLength.longValue() : 10L;
                        long maxLength =
                            schema.maxLength != null && schema.maxLength.longValue() > 0
                                ? schema.maxLength.longValue() : 10L;
                        long length = ThreadLocalRandom.current()
                            .nextLong(minLength, maxLength + 1);
                        randomModelBuilder.appendSimple(
                            quote("citrus:randomString(%s)".formatted(length), quotes));
                    }
                }
            }
            case OpenApiConstants.TYPE_NUMBER, TYPE_INTEGER ->
                // No quotes for numbers
                randomModelBuilder.appendSimple(createRandomNumber(schema));
            case OpenApiConstants.TYPE_BOOLEAN ->
                // No quotes for boolean
                randomModelBuilder.appendSimple("citrus:randomEnumValue('true', 'false')");
            default -> randomModelBuilder.appendSimple("");
        }
    }

    private static String createRandomNumber(OasSchema schema) {
        Number multipleOf = schema.multipleOf;

        boolean exclusiveMaximum = TRUE.equals(schema.exclusiveMaximum);
        boolean exclusiveMinimum = TRUE.equals(schema.exclusiveMinimum);

        BigDecimal[] bounds = determineBounds(schema);

        BigDecimal minimum = bounds[0];
        BigDecimal maximum = bounds[1];

        if (multipleOf != null) {
            minimum = exclusiveMinimum ? incrementToExclude(minimum) : minimum;
            maximum = exclusiveMaximum ? decrementToExclude(maximum) : maximum;
            return createMultipleOf(minimum, maximum, new BigDecimal(multipleOf.toString()));
        }

        return format(
            "citrus:randomNumberGenerator('%d', '%s', '%s', '%s', '%s')",
            determineDecimalPlaces(schema, minimum, maximum),
            minimum,
            maximum,
            exclusiveMinimum,
            exclusiveMaximum
        );
    }

    /**
     * Determines the number of decimal places to use based on the given schema and minimum/maximum values.
     * For integer types, it returns 0. For other types, it returns the maximum number of decimal places
     * found between the minimum and maximum values, with a minimum of 2 decimal places.
     */
    private static int determineDecimalPlaces(OasSchema schema, BigDecimal minimum,
        BigDecimal maximum) {
        if (TYPE_INTEGER.equals(schema.type)) {
            return 0;
        } else {
            return
                Math.max(2, Math.max(findLeastSignificantDecimalPlace(minimum),
                    findLeastSignificantDecimalPlace(maximum)));
        }
    }

    /**
     * Determine some reasonable bounds for a random number
     */
    private static BigDecimal[] determineBounds(OasSchema schema) {
        Number maximum = schema.maximum;
        Number minimum = schema.minimum;
        Number multipleOf = schema.multipleOf;

        BigDecimal bdMinimum;
        BigDecimal bdMaximum;
        if (minimum == null && maximum == null) {
            bdMinimum = MINUS_THOUSAND;
            bdMaximum = THOUSAND;
        } else if (minimum == null) {
            // Determine min relative to max
            bdMaximum = new BigDecimal(maximum.toString());

            if (multipleOf != null) {
                bdMinimum = bdMaximum.subtract(new BigDecimal(multipleOf.toString()).abs().multiply(
                    HUNDRED));
            } else {
                bdMinimum = bdMaximum.subtract(bdMaximum.multiply(BigDecimal.valueOf(2)).max(
                    THOUSAND));
            }
        } else if (maximum == null) {
            // Determine max relative to min
            bdMinimum = new BigDecimal(minimum.toString());
            if (multipleOf != null) {
                bdMaximum = bdMinimum.add(new BigDecimal(multipleOf.toString()).abs().multiply(
                    HUNDRED));
            } else {
                bdMaximum = bdMinimum.add(bdMinimum.multiply(BigDecimal.valueOf(2)).max(THOUSAND));
            }
        } else {
            bdMaximum = new BigDecimal(maximum.toString());
            bdMinimum = new BigDecimal(minimum.toString());
        }

        return new BigDecimal[]{bdMinimum, bdMaximum};
    }

    /**
     * Create a random schema value
     *
     * @param schema            the type to create
     * @param visitedRefSchemas the schemas already created during descent, used to avoid recursion
     */
    private static void createRandomValue(RandomModelBuilder randomModelBuilder, OasSchema schema,
        boolean quotes,
        OpenApiSpecification specification, Set<OasSchema> visitedRefSchemas) {
        if (hasText(schema.$ref) && visitedRefSchemas.contains(schema)) {
            // Avoid recursion
            return;
        }

        if (OasModelHelper.isReferenceType(schema)) {
            OasSchema resolved = OasModelHelper.getSchemaDefinitions(
                    specification.getOpenApiDoc(null))
                .get(OasModelHelper.getReferenceName(schema.$ref));
            createRandomValue(randomModelBuilder, resolved, quotes, specification,
                visitedRefSchemas);
            return;
        }

        if (OasModelHelper.isCompositeSchema(schema)) {
            createComposedSchema(randomModelBuilder, schema, quotes, specification,
                visitedRefSchemas);
            return;
        }

        switch (schema.type) {
            case OpenApiConstants.TYPE_OBJECT ->
                createRandomObjectSchemeMap(randomModelBuilder, schema, specification,
                    visitedRefSchemas);
            case OpenApiConstants.TYPE_ARRAY ->
                createRandomArrayValueMap(randomModelBuilder, schema, specification,
                    visitedRefSchemas);
            case OpenApiConstants.TYPE_STRING, TYPE_INTEGER, OpenApiConstants.TYPE_NUMBER, OpenApiConstants.TYPE_BOOLEAN ->
                createRandomValueExpressionMap(randomModelBuilder, schema, quotes);
            default -> {
                if (quotes) {
                    randomModelBuilder.appendSimple("\"\"");
                } else {
                    randomModelBuilder.appendSimple("");
                }
            }
        }
    }

    private static void createRandomObjectSchemeMap(RandomModelBuilder randomModelBuilder,
        OasSchema objectSchema,
        OpenApiSpecification specification, Set<OasSchema> visitedRefSchemas) {

        randomModelBuilder.object(() -> {
            if (objectSchema.properties != null) {
                for (Map.Entry<String, OasSchema> entry : objectSchema.properties.entrySet()) {
                    if (specification.isGenerateOptionalFields() || isRequired(objectSchema,
                        entry.getKey())) {
                        randomModelBuilder.property(entry.getKey(), () ->
                            createRandomValue(randomModelBuilder, entry.getValue(), true,
                                specification,
                                visitedRefSchemas));
                    }
                }
            }
        });
    }

    private static void createComposedSchema(RandomModelBuilder randomModelBuilder,
        OasSchema schema, boolean quotes,
        OpenApiSpecification specification, Set<OasSchema> visitedRefSchemas) {

        if (!isEmpty(schema.allOf)) {
            createAllOff(randomModelBuilder, schema, quotes, specification, visitedRefSchemas);
        } else if (schema instanceof Oas30Schema oas30Schema && !isEmpty(oas30Schema.anyOf)) {
            createAnyOf(randomModelBuilder, oas30Schema, quotes, specification, visitedRefSchemas);
        } else if (schema instanceof Oas30Schema oas30Schema && !isEmpty(oas30Schema.oneOf)) {
            createOneOf(randomModelBuilder, oas30Schema.oneOf, quotes, specification,
                visitedRefSchemas);
        }
    }

    private static void createOneOf(RandomModelBuilder randomModelBuilder, List<OasSchema> schemas,
        boolean quotes,
        OpenApiSpecification specification, Set<OasSchema> visitedRefSchemas) {
        int schemaIndex = ThreadLocalRandom.current().nextInt(schemas.size());
        randomModelBuilder.object(() ->
            createRandomValue(randomModelBuilder, schemas.get(schemaIndex), quotes, specification,
                visitedRefSchemas));
    }

    private static void createAnyOf(RandomModelBuilder randomModelBuilder, Oas30Schema schema,
        boolean quotes,
        OpenApiSpecification specification, Set<OasSchema> visitedRefSchemas) {

        randomModelBuilder.object(() -> {
            boolean anyAdded = false;
            for (OasSchema oneSchema : schema.anyOf) {
                if (ThreadLocalRandom.current().nextBoolean()) {
                    createRandomValue(randomModelBuilder, oneSchema, quotes, specification,
                        visitedRefSchemas);
                    anyAdded = true;
                }
            }

            // Add at least one
            if (!anyAdded) {
                createOneOf(randomModelBuilder, schema.anyOf, quotes, specification,
                    visitedRefSchemas);
            }
        });
    }

    private static Map<String, Object> createAllOff(RandomModelBuilder randomModelBuilder,
        OasSchema schema, boolean quotes,
        OpenApiSpecification specification, Set<OasSchema> visitedRefSchemas) {
        Map<String, Object> allOf = new HashMap<>();

        randomModelBuilder.object(() -> {
            for (OasSchema oneSchema : schema.allOf) {
                createRandomValue(randomModelBuilder, oneSchema, quotes, specification,
                    visitedRefSchemas);
            }
        });

        return allOf;
    }

    private static String createMultipleOf(
        BigDecimal minimum,
        BigDecimal maximum,
        BigDecimal multipleOf
    ) {

        BigDecimal lowestMultiple = lowestMultipleOf(minimum, multipleOf);
        BigDecimal largestMultiple = largestMultipleOf(maximum, multipleOf);

        // Check if there are no valid multiples in the range
        if (lowestMultiple.compareTo(largestMultiple) > 0) {
            return null;
        }

        BigDecimal range = largestMultiple.subtract(lowestMultiple)
            .divide(multipleOf, RoundingMode.DOWN);

        // Don't go for incredible large numbers
        if (range.compareTo(BigDecimal.valueOf(11)) > 0) {
            range = BigDecimal.valueOf(10);
        }

        long factor = 0;
        if (range.compareTo(BigDecimal.ZERO) != 0) {
            factor = ThreadLocalRandom.current().nextLong(1, range.longValue() + 1);
        }
        BigDecimal randomMultiple = lowestMultiple.add(
            multipleOf.multiply(BigDecimal.valueOf(factor)));
        randomMultiple = randomMultiple.setScale(findLeastSignificantDecimalPlace(multipleOf),
            RoundingMode.HALF_UP);

        return randomMultiple.toString();
    }

    /**
     * Create a random array value.
     *
     * @param schema            the type to create
     * @param visitedRefSchemas the schemas already created during descent, used to avoid recursion
     */
    @SuppressWarnings("rawtypes")
    private static void createRandomArrayValueMap(RandomModelBuilder randomModelBuilder,
        OasSchema schema,
        OpenApiSpecification specification, Set<OasSchema> visitedRefSchemas) {
        Object items = schema.items;

        if (items instanceof OasSchema itemsSchema) {
            createRandomArrayValueWithSchemaItem(randomModelBuilder, schema, itemsSchema,
                specification,
                visitedRefSchemas);
        } else {
            throw new UnsupportedOperationException(
                "Random array creation for an array with items having different schema is currently not supported!");
        }
    }

    private static void createRandomArrayValueWithSchemaItem(RandomModelBuilder randomModelBuilder,
        OasSchema schema,
        OasSchema itemsSchema, OpenApiSpecification specification,
        Set<OasSchema> visitedRefSchemas) {
        Number minItems = schema.minItems;
        minItems = minItems != null ? minItems : 1;
        Number maxItems = schema.maxItems;
        maxItems = maxItems != null ? maxItems : 9;

        int nItems = ThreadLocalRandom.current()
            .nextInt(minItems.intValue(), maxItems.intValue() + 1);

        randomModelBuilder.array(() -> {
            for (int i = 0; i < nItems; i++) {
                createRandomValue(randomModelBuilder, itemsSchema, true, specification,
                    visitedRefSchemas);
            }
        });
    }

    static BigDecimal largestMultipleOf(BigDecimal highest, BigDecimal multipleOf) {
        RoundingMode roundingMode =
            highest.compareTo(BigDecimal.ZERO) < 0 ? RoundingMode.UP : RoundingMode.DOWN;
        BigDecimal factor = highest.divide(multipleOf, 0, roundingMode);
        return multipleOf.multiply(factor);
    }

    static BigDecimal lowestMultipleOf(BigDecimal lowest, BigDecimal multipleOf) {
        RoundingMode roundingMode =
            lowest.compareTo(BigDecimal.ZERO) < 0 ? RoundingMode.DOWN : RoundingMode.UP;
        BigDecimal factor = lowest.divide(multipleOf, 0, roundingMode);
        return multipleOf.multiply(factor);
    }

    static BigDecimal incrementToExclude(BigDecimal val) {
        return val.add(determineIncrement(val))
            .setScale(findLeastSignificantDecimalPlace(val), RoundingMode.HALF_DOWN);
    }

    static BigDecimal decrementToExclude(BigDecimal val) {
        return val.subtract(determineIncrement(val))
            .setScale(findLeastSignificantDecimalPlace(val), RoundingMode.HALF_DOWN);
    }

    static BigDecimal determineIncrement(BigDecimal number) {
        return BigDecimal.valueOf(1.0d / (Math.pow(10d, findLeastSignificantDecimalPlace(number))));
    }

    static int findLeastSignificantDecimalPlace(BigDecimal number) {
        number = number.stripTrailingZeros();

        String[] parts = number.toPlainString().split("\\.");

        if (parts.length == 1) {
            return 0;
        }

        return parts[1].length();
    }
}
