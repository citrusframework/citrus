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

import static org.citrusframework.openapi.OpenApiConstants.FORMAT_DOUBLE;
import static org.citrusframework.openapi.OpenApiConstants.FORMAT_FLOAT;
import static org.citrusframework.openapi.OpenApiConstants.FORMAT_INT32;
import static org.citrusframework.openapi.OpenApiConstants.FORMAT_INT64;
import static org.citrusframework.openapi.OpenApiConstants.FORMAT_UUID;
import static org.citrusframework.openapi.OpenApiConstants.TYPE_ARRAY;
import static org.citrusframework.openapi.OpenApiConstants.TYPE_INTEGER;
import static org.citrusframework.openapi.OpenApiConstants.TYPE_NUMBER;
import static org.citrusframework.openapi.OpenApiConstants.TYPE_STRING;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.atlassian.oai.validator.report.ValidationReport;
import com.atlassian.oai.validator.report.ValidationReport.Message;
import com.atlassian.oai.validator.schema.SchemaValidator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import io.swagger.v3.oas.models.media.Schema;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.citrusframework.context.TestContext;
import org.citrusframework.functions.DefaultFunctionRegistry;
import org.citrusframework.openapi.model.OasModelHelper;
import org.citrusframework.spi.Resources;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class OpenApiTestDataGeneratorTest {

    private static final TestContext testContext = new TestContext();

    private static OpenApiSpecification openApiSpecification;

    private static SchemaValidator schemaValidator;

    @BeforeClass
    public static void beforeClass() {
        testContext.setFunctionRegistry(new DefaultFunctionRegistry());

        openApiSpecification = OpenApiSpecification.from(
            Resources.fromClasspath("org/citrusframework/openapi/ping/ping-api.yaml"));
        schemaValidator = openApiSpecification.getSwaggerOpenApiValidationContext()
            .getSchemaValidator();
    }

    @DataProvider(name = "findLeastSignificantDecimalPlace")
    public static Object[][] findLeastSignificantDecimalPlace() {
        return new Object[][]{
            {new BigDecimal("1234.5678"), 4},
            {new BigDecimal("123.567"), 3},
            {new BigDecimal("123.56"), 2},
            {new BigDecimal("123.5"), 1},
            {new BigDecimal("123.0"), 0},
            {new BigDecimal("123"), 0}
        };
    }

    @Test(dataProvider = "findLeastSignificantDecimalPlace")
    void findLeastSignificantDecimalPlace(BigDecimal number, int expectedSignificance) {
        assertEquals(OpenApiTestDataGenerator.findLeastSignificantDecimalPlace(number),
            expectedSignificance);
    }

    @DataProvider(name = "incrementToExclude")
    public static Object[][] incrementToExclude() {
        return new Object[][]{
            {new BigDecimal("1234.678"), new BigDecimal("1234.679")},
            {new BigDecimal("1234.78"), new BigDecimal("1234.79")},
            {new BigDecimal("1234.8"), new BigDecimal("1234.9")},
            {new BigDecimal("1234.0"), new BigDecimal("1235")},
            {new BigDecimal("1234"), new BigDecimal("1235")},
        };
    }

    @Test(dataProvider = "incrementToExclude")
    void incrementToExclude(BigDecimal value, BigDecimal expectedValue) {
        assertEquals(OpenApiTestDataGenerator.incrementToExclude(value), expectedValue);
    }

    @DataProvider(name = "decrementToExclude")
    public static Object[][] decrementToExclude() {
        return new Object[][]{
            {new BigDecimal("1234.678"), new BigDecimal("1234.677")},
            {new BigDecimal("1234.78"), new BigDecimal("1234.77")},
            {new BigDecimal("1234.8"), new BigDecimal("1234.7")},
            {new BigDecimal("1234.0"), new BigDecimal("1233")},
            {new BigDecimal("1234"), new BigDecimal("1233")},
        };
    }

    @Test(dataProvider = "decrementToExclude")
    void decrementToExclude(BigDecimal value, BigDecimal expectedValue) {
        assertEquals(OpenApiTestDataGenerator.decrementToExclude(value), expectedValue);
    }

    @Test
    void testUuidFormat() {
        Oas30Schema stringSchema = new Oas30Schema();
        stringSchema.type = TYPE_STRING;
        stringSchema.format = FORMAT_UUID;

        String uuidRandomValue = OpenApiTestDataGenerator.createRandomValueExpression(stringSchema,
            false);
        String finalUuidRandomValue = testContext.replaceDynamicContentInString(uuidRandomValue);
        Pattern uuidPattern = Pattern.compile(
            "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

        assertTrue(uuidPattern.matcher(finalUuidRandomValue).matches());
    }

    @DataProvider(name = "testRandomNumber")
    public static Object[][] testRandomNumber() {
        return new Object[][]{
            {TYPE_INTEGER, FORMAT_INT32, null, 0, 2, true, true},
            {TYPE_INTEGER, FORMAT_INT32, null, null, null, false, false},
            {TYPE_INTEGER, FORMAT_INT32, null, 0, 100, false, false},
            {TYPE_INTEGER, FORMAT_INT32, null, 0, 2, false, false},
            {TYPE_INTEGER, FORMAT_INT32, null, -100, 0, false, false},
            {TYPE_INTEGER, FORMAT_INT32, null, -2, 0, false, false},
            {TYPE_INTEGER, FORMAT_INT32, null, 0, 100, true, true},
            {TYPE_INTEGER, FORMAT_INT32, null, -100, 0, true, true},
            {TYPE_INTEGER, FORMAT_INT32, null, -2, 0, true, true},
            {TYPE_INTEGER, FORMAT_INT32, null, 0, null, false, false},
            {TYPE_INTEGER, FORMAT_INT32, null, 0, 0, false, false},
            {TYPE_INTEGER, FORMAT_INT32, 11, 0, 12, true, true},
            {TYPE_INTEGER, FORMAT_INT32, 12, null, null, false, false},
            {TYPE_INTEGER, FORMAT_INT32, 13, 0, 100, false, false},
            {TYPE_INTEGER, FORMAT_INT32, 14, 0, 14, false, false},
            {TYPE_INTEGER, FORMAT_INT32, 15, -100, 0, false, false},
            {TYPE_INTEGER, FORMAT_INT32, 16, -16, 0, false, false},
            {TYPE_INTEGER, FORMAT_INT32, 17, 0, 100, true, true},
            {TYPE_INTEGER, FORMAT_INT32, 18, -100, 0, true, true},
            {TYPE_INTEGER, FORMAT_INT32, 19, -20, 0, true, true},
            {TYPE_INTEGER, FORMAT_INT32, 20, 0, null, false, false},
            {TYPE_INTEGER, FORMAT_INT32, 21, 21, 21, false, false},

            {TYPE_INTEGER, FORMAT_INT64, null, 0, 2, true, true},
            {TYPE_INTEGER, FORMAT_INT64, null, null, null, false, false},
            {TYPE_INTEGER, FORMAT_INT64, null, 0, 100, false, false},
            {TYPE_INTEGER, FORMAT_INT64, null, 0, 2, false, false},
            {TYPE_INTEGER, FORMAT_INT64, null, -100, 0, false, false},
            {TYPE_INTEGER, FORMAT_INT64, null, -2, 0, false, false},
            {TYPE_INTEGER, FORMAT_INT64, null, 0, 100, true, true},
            {TYPE_INTEGER, FORMAT_INT64, null, -100, 0, true, true},
            {TYPE_INTEGER, FORMAT_INT64, null, -2, 0, true, true},
            {TYPE_INTEGER, FORMAT_INT64, null, 0, null, false, false},
            {TYPE_INTEGER, FORMAT_INT64, null, 0, 0, false, false},
            {TYPE_INTEGER, FORMAT_INT64, 11, 0, 12, true, true},
            {TYPE_INTEGER, FORMAT_INT64, 12, null, null, false, false},
            {TYPE_INTEGER, FORMAT_INT64, 13, 0, 100, false, false},
            {TYPE_INTEGER, FORMAT_INT64, 14, 0, 14, false, false},
            {TYPE_INTEGER, FORMAT_INT64, 15, -100, 0, false, false},
            {TYPE_INTEGER, FORMAT_INT64, 16, -16, 0, false, false},
            {TYPE_INTEGER, FORMAT_INT64, 17, 0, 100, true, true},
            {TYPE_INTEGER, FORMAT_INT64, 18, -100, 0, true, true},
            {TYPE_INTEGER, FORMAT_INT64, 19, -20, 0, true, true},
            {TYPE_INTEGER, FORMAT_INT64, 20, 0, null, false, false},
            {TYPE_INTEGER, FORMAT_INT64, 21, 21, 21, false, false},

            {TYPE_NUMBER, FORMAT_FLOAT, null, 0, 2, true, true},
            {TYPE_NUMBER, FORMAT_FLOAT, null, null, null, false, false},
            {TYPE_NUMBER, FORMAT_FLOAT, null, 0, 100, false, false},
            {TYPE_NUMBER, FORMAT_FLOAT, null, 0, 2, false, false},
            {TYPE_NUMBER, FORMAT_FLOAT, null, -100, 0, false, false},
            {TYPE_NUMBER, FORMAT_FLOAT, null, -2, 0, false, false},
            {TYPE_NUMBER, FORMAT_FLOAT, null, 0, 100, true, true},
            {TYPE_NUMBER, FORMAT_FLOAT, null, -100, 0, true, true},
            {TYPE_NUMBER, FORMAT_FLOAT, null, -2, 0, true, true},
            {TYPE_NUMBER, FORMAT_FLOAT, null, 0, null, false, false},
            {TYPE_NUMBER, FORMAT_FLOAT, null, 0, 0, false, false},
            {TYPE_NUMBER, FORMAT_FLOAT, 11.123f, 0, 13, true, true},
            {TYPE_NUMBER, FORMAT_FLOAT, 12.123f, null, null, false, false},
            {TYPE_NUMBER, FORMAT_FLOAT, 13.123f, 0, 100, false, false},
            {TYPE_NUMBER, FORMAT_FLOAT, 14.123f, 0, 14, false, false},
            {TYPE_NUMBER, FORMAT_FLOAT, 15.123f, -100, 0, false, false},
            {TYPE_NUMBER, FORMAT_FLOAT, 16.123f, -16, 0, false, false},
            {TYPE_NUMBER, FORMAT_FLOAT, 17.123f, 0, 100, true, true},
            {TYPE_NUMBER, FORMAT_FLOAT, 18.123f, -100, 0, true, true},
            {TYPE_NUMBER, FORMAT_FLOAT, 19.123f, -21, 0, true, true},
            {TYPE_NUMBER, FORMAT_FLOAT, 20.123f, 0, null, false, false},
            {TYPE_NUMBER, FORMAT_FLOAT, 21.123f, 21.122f, 21.124f, false, false},

            {TYPE_NUMBER, FORMAT_DOUBLE, null, 0, 2, true, true},
            {TYPE_NUMBER, FORMAT_DOUBLE, null, null, null, false, false},
            {TYPE_NUMBER, FORMAT_DOUBLE, null, 0, 100, false, false},
            {TYPE_NUMBER, FORMAT_DOUBLE, null, 0, 2, false, false},
            {TYPE_NUMBER, FORMAT_DOUBLE, null, -100, 0, false, false},
            {TYPE_NUMBER, FORMAT_DOUBLE, null, -2, 0, false, false},
            {TYPE_NUMBER, FORMAT_DOUBLE, null, 0, 100, true, true},
            {TYPE_NUMBER, FORMAT_DOUBLE, null, -100, 0, true, true},
            {TYPE_NUMBER, FORMAT_DOUBLE, null, -2, 0, true, true},
            {TYPE_NUMBER, FORMAT_DOUBLE, null, 0, null, false, false},
            {TYPE_NUMBER, FORMAT_DOUBLE, null, 0, 0, false, false},
            {TYPE_NUMBER, FORMAT_DOUBLE, 11.123d, 0, 13, true, true},
            {TYPE_NUMBER, FORMAT_DOUBLE, 12.123d, null, null, false, false},
            {TYPE_NUMBER, FORMAT_DOUBLE, 13.123d, 0, 100, false, false},
            {TYPE_NUMBER, FORMAT_DOUBLE, 14.123d, 0, 14, false, false},
            {TYPE_NUMBER, FORMAT_DOUBLE, 15.123d, -100, 0, false, false},
            {TYPE_NUMBER, FORMAT_DOUBLE, 16.123d, -16, 0, false, false},
            {TYPE_NUMBER, FORMAT_DOUBLE, 17.123d, 0, 100, true, true},
            {TYPE_NUMBER, FORMAT_DOUBLE, 18.123d, -100, 0, true, true},
            {TYPE_NUMBER, FORMAT_DOUBLE, 19.123d, -21, 0, true, true},
            {TYPE_NUMBER, FORMAT_DOUBLE, 20.123d, 0, null, false, false},
            {TYPE_NUMBER, FORMAT_DOUBLE, 21.123d, 21.122d, 21.124d, false, false},
        };
    }

    @Test(dataProvider = "testRandomNumber")
    void testRandomNumber(String type, String format, Number multipleOf, Number minimum,
        Number maximum, boolean exclusiveMinimum, boolean exclusiveMaximum) {
        Oas30Schema testSchema = new Oas30Schema();
        testSchema.type = type;
        testSchema.format = format;
        testSchema.multipleOf = multipleOf;
        testSchema.minimum = minimum;
        testSchema.maximum = maximum;
        testSchema.exclusiveMinimum = exclusiveMinimum;
        testSchema.exclusiveMaximum = exclusiveMaximum;

        try {
            for (int i = 0; i < 1000; i++) {
                String randomValue = OpenApiTestDataGenerator.createOutboundPayload(
                    testSchema, openApiSpecification);
                String finalRandomValue = testContext.resolveDynamicValue(randomValue);
                BigDecimal value = new BigDecimal(finalRandomValue);

                if (multipleOf != null) {
                    BigDecimal remainder = value.remainder(new BigDecimal(multipleOf.toString()));

                    assertEquals(
                        remainder.compareTo(BigDecimal.ZERO), 0,
                        "Expected %s to be a multiple of %s! Remainder is %s".formatted(
                            finalRandomValue, multipleOf,
                            remainder));
                }

                if (maximum != null) {
                    if (exclusiveMaximum) {
                        assertTrue(value.doubleValue() < testSchema.maximum.doubleValue(),
                            "Expected %s to be lower than %s!".formatted(
                                finalRandomValue, maximum));
                    } else {
                        assertTrue(value.doubleValue() <= testSchema.maximum.doubleValue(),
                            "Expected %s to be lower or equal than %s!".formatted(
                                finalRandomValue, maximum));
                    }
                }

                if (minimum != null) {
                    if (exclusiveMinimum) {
                        assertTrue(value.doubleValue() > testSchema.minimum.doubleValue(),
                            "Expected %s to be larger than %s!".formatted(
                                finalRandomValue, minimum));
                    } else {
                        assertTrue(value.doubleValue() >= testSchema.minimum.doubleValue(),
                            "Expected %s to be larger or equal than %s!".formatted(
                                finalRandomValue, minimum));
                    }
                }
            }
        } catch (Exception e) {
            Assert.fail("Creation of multiple float threw an exception: " + e.getMessage(), e);
        }
    }

    @Test
    void testPattern() {
        Oas30Schema stringSchema = new Oas30Schema();
        stringSchema.type = TYPE_STRING;

        String exp = "[0-3]([a-c]|[e-g]{1,2})";
        stringSchema.pattern = exp;

        String randomValue = OpenApiTestDataGenerator.createRandomValueExpression(stringSchema,
            false);
        String finalRandomValue = testContext.replaceDynamicContentInString(randomValue);
        assertTrue(finalRandomValue.matches(exp),
            "Value '%s' does not match expression '%s'".formatted(finalRandomValue, exp));
    }

    @DataProvider(name = "testPingApiSchemas")
    public static Object[][] testPingApiSchemas() {
        return new Object[][]{

            // Composites currently do not work properly - validation fails
            //{"AnyOfType"},
            //{"AllOfType"},
            //{"PingRespType"},

            {"OneOfType"},
            {"StringsType"},
            {"DatesType"},
            {"NumbersType"},
            {"MultipleOfType"},
            {"PingReqType"},
            {"Detail1"},
            {"Detail2"},
            {"BooleanType"},
            {"EnumType"},
            {"NestedType"},
            {"SimpleArrayType"},
            {"ComplexArrayType"},
            {"ArrayOfArraysType"},
            {"NullableType"},
            {"DefaultValueType"},
        };
    }


    @Test(dataProvider = "testPingApiSchemas")
    void testPingApiSchemas(String schemaType) throws IOException {

        OasSchema schema = OasModelHelper.getSchemaDefinitions(
            openApiSpecification.getOpenApiDoc(null)).get(schemaType);

        Schema<?> swaggerValidationSchema = openApiSpecification.getSwaggerOpenApiValidationContext()
            .getSwaggerOpenApi().getComponents().getSchemas().get(schemaType);

        assertNotNull(schema);

        for (int i=0;i<100;i++) {

            String randomValue = OpenApiTestDataGenerator.createOutboundPayload(schema,
                openApiSpecification);
            assertNotNull(randomValue);

            String finalJsonAsText = testContext.replaceDynamicContentInString(randomValue);

            try {
                JsonNode valueNode = new ObjectMapper().readTree(
                    testContext.replaceDynamicContentInString(finalJsonAsText));
                ValidationReport validationReport = schemaValidator.validate(() -> valueNode,
                    swaggerValidationSchema,
                    "response.body");

                String message = """
                Json is invalid according to schema.
                Message: %s
                Report: %s
                """.formatted(finalJsonAsText, validationReport.getMessages().stream().map(
                    Message::getMessage).collect(Collectors.joining("\n")));
                assertFalse(validationReport.hasErrors(), message);
            } catch (JsonParseException e) {
                Assert.fail("Unable to read generated schema to json: "+finalJsonAsText);
            }
        }
    }

    @Test
    void testArray() {
        Oas30Schema arraySchema = new Oas30Schema();
        arraySchema.type = TYPE_ARRAY;

        Oas30Schema stringSchema = new Oas30Schema();
        stringSchema.type = TYPE_STRING;
        stringSchema.minLength = 5;
        stringSchema.maxLength = 15;

        arraySchema.items = stringSchema;

        for (int i = 0; i < 10; i++) {
            String randomValue = OpenApiTestDataGenerator.createOutboundPayload(arraySchema,
                openApiSpecification);
            int nElements = StringUtils.countMatches(randomValue, "citrus:randomString");
            assertTrue(nElements > 0);
        }
    }

    @Test
    void testArrayMinItems() {
        Oas30Schema arraySchema = new Oas30Schema();
        arraySchema.type = TYPE_ARRAY;
        arraySchema.minItems = 5;

        Oas30Schema stringSchema = new Oas30Schema();
        stringSchema.type = TYPE_STRING;
        stringSchema.minLength = 5;
        stringSchema.maxLength = 15;

        arraySchema.items = stringSchema;

        for (int i = 0; i < 10; i++) {
            String randomValue = OpenApiTestDataGenerator.createOutboundPayload(arraySchema,
                openApiSpecification);
            int nElements = StringUtils.countMatches(randomValue, "citrus:randomString(15)");
            assertTrue(nElements <= 5);
        }
    }

    @Test
    void testArrayMaxItems() {
        Oas30Schema arraySchema = new Oas30Schema();
        arraySchema.type = TYPE_ARRAY;
        arraySchema.minItems = 2;
        arraySchema.maxItems = 5;

        Oas30Schema stringSchema = new Oas30Schema();
        stringSchema.type = TYPE_STRING;
        stringSchema.minLength = 10;
        stringSchema.maxLength = 15;

        arraySchema.items = stringSchema;

        Pattern pattern = Pattern.compile("citrus:randomString\\(1[0-5]\\)");
        for (int i = 0; i < 100; i++) {
            String randomArrayValue = OpenApiTestDataGenerator.createOutboundPayload(arraySchema,
                openApiSpecification);

            Matcher matcher = pattern.matcher(randomArrayValue);
            int matches = 0;
            while (matcher.find()) {
                matches++;
            }

            assertTrue(2 <= matches && matches <= 5,
                "Expected random array string with number of elements between 2 and 4 but found %s: %s".formatted(
                    matches, randomArrayValue));
        }
    }

    @Test
    public void testLowestMultipleOf() {
        assertEquals(OpenApiTestDataGenerator.lowestMultipleOf(BigDecimal.valueOf(-1000),
            BigDecimal.valueOf(10)), BigDecimal.valueOf(-1000));
        assertEquals(OpenApiTestDataGenerator.lowestMultipleOf(BigDecimal.valueOf(-1000),
            BigDecimal.valueOf(-10)), BigDecimal.valueOf(-1000));
        assertEquals(OpenApiTestDataGenerator.lowestMultipleOf(BigDecimal.valueOf(-1000),
            BigDecimal.valueOf(11)), BigDecimal.valueOf(-990));
        assertEquals(OpenApiTestDataGenerator.lowestMultipleOf(BigDecimal.valueOf(-1000),
            BigDecimal.valueOf(-11)), BigDecimal.valueOf(-990));
        assertEquals(OpenApiTestDataGenerator.lowestMultipleOf(BigDecimal.valueOf(-1000),
            BigDecimal.valueOf(11.1234)), BigDecimal.valueOf(-989.9826));
        assertEquals(OpenApiTestDataGenerator.lowestMultipleOf(BigDecimal.valueOf(-1000),
            BigDecimal.valueOf(-11.1234)), BigDecimal.valueOf(-989.9826));

        assertEquals(OpenApiTestDataGenerator.lowestMultipleOf(BigDecimal.valueOf(1000),
            BigDecimal.valueOf(10)), BigDecimal.valueOf(1000));
        assertEquals(OpenApiTestDataGenerator.lowestMultipleOf(BigDecimal.valueOf(1000),
            BigDecimal.valueOf(-10)), BigDecimal.valueOf(1000));
        assertEquals(OpenApiTestDataGenerator.lowestMultipleOf(BigDecimal.valueOf(1000),
            BigDecimal.valueOf(11)), BigDecimal.valueOf(1001));
        assertEquals(OpenApiTestDataGenerator.lowestMultipleOf(BigDecimal.valueOf(1000),
            BigDecimal.valueOf(-11)), BigDecimal.valueOf(1001));
        assertEquals(OpenApiTestDataGenerator.lowestMultipleOf(BigDecimal.valueOf(1000),
            BigDecimal.valueOf(11.1234)), new BigDecimal("1001.1060"));
        assertEquals(OpenApiTestDataGenerator.lowestMultipleOf(BigDecimal.valueOf(1000),
            BigDecimal.valueOf(-11.1234)), new BigDecimal("1001.1060"));
    }

    @Test
    public void testLargestMultipleOf() {
        assertEquals(OpenApiTestDataGenerator.largestMultipleOf(BigDecimal.valueOf(-1000),
            BigDecimal.valueOf(10)), BigDecimal.valueOf(-1000));
        assertEquals(OpenApiTestDataGenerator.largestMultipleOf(BigDecimal.valueOf(-1000),
            BigDecimal.valueOf(-10)), BigDecimal.valueOf(-1000));
        assertEquals(OpenApiTestDataGenerator.largestMultipleOf(BigDecimal.valueOf(-1000),
            BigDecimal.valueOf(11)), BigDecimal.valueOf(-1001));
        assertEquals(OpenApiTestDataGenerator.largestMultipleOf(BigDecimal.valueOf(-1000),
            BigDecimal.valueOf(-11)), BigDecimal.valueOf(-1001));
        assertEquals(OpenApiTestDataGenerator.largestMultipleOf(BigDecimal.valueOf(-1000),
            BigDecimal.valueOf(11.1234)), new BigDecimal("-1001.1060"));
        assertEquals(OpenApiTestDataGenerator.largestMultipleOf(BigDecimal.valueOf(-1000),
            BigDecimal.valueOf(-11.1234)), new BigDecimal("-1001.1060"));

        assertEquals(OpenApiTestDataGenerator.largestMultipleOf(BigDecimal.valueOf(1000),
            BigDecimal.valueOf(10)), BigDecimal.valueOf(1000));
        assertEquals(OpenApiTestDataGenerator.largestMultipleOf(BigDecimal.valueOf(1000),
            BigDecimal.valueOf(-10)), BigDecimal.valueOf(1000));
        assertEquals(OpenApiTestDataGenerator.largestMultipleOf(BigDecimal.valueOf(1000),
            BigDecimal.valueOf(11)), BigDecimal.valueOf(990));
        assertEquals(OpenApiTestDataGenerator.largestMultipleOf(BigDecimal.valueOf(1000),
            BigDecimal.valueOf(-11)), BigDecimal.valueOf(990));
        assertEquals(OpenApiTestDataGenerator.largestMultipleOf(BigDecimal.valueOf(1000),
            BigDecimal.valueOf(11.1234)), new BigDecimal("989.9826"));
        assertEquals(OpenApiTestDataGenerator.largestMultipleOf(BigDecimal.valueOf(1000),
            BigDecimal.valueOf(-11.1234)), new BigDecimal("989.9826"));
    }
}
