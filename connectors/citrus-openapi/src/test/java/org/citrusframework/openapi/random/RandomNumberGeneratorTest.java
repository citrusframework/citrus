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

package org.citrusframework.openapi.random;

import java.math.BigDecimal;

import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import org.citrusframework.openapi.OpenApiConstants;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class RandomNumberGeneratorTest {

    private RandomNumberGenerator generator;
    private RandomContext mockContext;
    private RandomModelBuilder mockBuilder;
    private OasSchema schema;

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

    @BeforeMethod
    public void setUp() {
        generator = new RandomNumberGenerator();
        mockContext = mock(RandomContext.class);
        mockBuilder = mock(RandomModelBuilder.class);
        schema = new Oas30Schema();

        when(mockContext.getRandomModelBuilder()).thenReturn(mockBuilder);
    }

    @Test
    public void testGenerateDefaultBounds() {
        generator.generateIntoContext(mockContext, schema);
        verify(mockBuilder).appendSimple("citrus:randomNumberGenerator('2', '-1000', '1000', 'false', 'false')");
    }

    @Test
    public void testGenerateWithMinimum() {
        schema.minimum = BigDecimal.valueOf(5);
        generator.generateIntoContext(mockContext, schema);
        // Max is because of guessing a reasonable range
        verify(mockBuilder).appendSimple("citrus:randomNumberGenerator('2', '5', '1005', 'false', 'false')");
    }

    @Test
    public void testGenerateWithMaximum() {
        schema.maximum = BigDecimal.valueOf(15);
        generator.generateIntoContext(mockContext, schema);
        // Min is because of guessing a reasonable range
        verify(mockBuilder).appendSimple("citrus:randomNumberGenerator('2', '-985', '15', 'false', 'false')");
    }

    @Test
    public void testGenerateWithMinimumAndMaximum() {
        schema.minimum = BigDecimal.valueOf(5);
        schema.maximum = BigDecimal.valueOf(15);
        generator.generateIntoContext(mockContext, schema);
        verify(mockBuilder).appendSimple("citrus:randomNumberGenerator('2', '5', '15', 'false', 'false')");
    }

    @Test
    public void testGenerateWithExclusiveMinimum() {
        schema.minimum = BigDecimal.valueOf(5);
        schema.exclusiveMinimum = true;
        generator.generateIntoContext(mockContext, schema);
        // Max is because of guessing a reasonable range
        verify(mockBuilder).appendSimple("citrus:randomNumberGenerator('2', '5', '1005', 'true', 'false')");
    }

    @Test
    public void testGenerateWithExclusiveMaximum() {
        schema.maximum = BigDecimal.valueOf(15);
        schema.exclusiveMaximum = true;
        generator.generateIntoContext(mockContext, schema);
        // Min is because of guessing a reasonable range
        verify(mockBuilder).appendSimple("citrus:randomNumberGenerator('2', '-985', '15', 'false', 'true')");
    }

    @Test
    public void testGenerateWithMultipleOf() {
        schema.multipleOf = BigDecimal.valueOf(5);
        schema.minimum = BigDecimal.valueOf(10);
        schema.maximum = BigDecimal.valueOf(50);
        generator.generateIntoContext(mockContext, schema);
        verify(mockBuilder).appendSimple("citrus:randomNumberGenerator('0', '10', '50', 'false', 'false', '5')");
    }

    @Test
    public void testGenerateWithIntegerType() {
        schema.type = "integer";
        schema.minimum = BigDecimal.valueOf(1);
        schema.maximum = BigDecimal.valueOf(10);
        generator.generateIntoContext(mockContext, schema);
        verify(mockBuilder).appendSimple("citrus:randomNumberGenerator('0', '1', '10', 'false', 'false')");
    }

    @Test
    public void testGenerateWithFloatType() {
        schema.type = "number";
        schema.minimum = BigDecimal.valueOf(1.5);
        schema.maximum = BigDecimal.valueOf(10.5);
        generator.generateIntoContext(mockContext, schema);
        verify(mockBuilder).appendSimple("citrus:randomNumberGenerator('2', '1.5', '10.5', 'false', 'false')");
    }

    @Test
    public void testGenerateWithMultipleOfFloat() {
        schema.type = "number";
        schema.multipleOf = BigDecimal.valueOf(0.5);
        schema.minimum = BigDecimal.valueOf(1.0);
        schema.maximum = BigDecimal.valueOf(5.0);
        generator.generateIntoContext(mockContext, schema);
        verify(mockBuilder).appendSimple("citrus:randomNumberGenerator('1', '1.0', '5.0', 'false', 'false', '0.5')");
    }

    @Test
    public void testCalculateMinRelativeToMaxWithMultipleOf() {
        BigDecimal max = new BigDecimal("1000");
        Number multipleOf = new BigDecimal("10");

        BigDecimal result = RandomNumberGenerator.calculateMinRelativeToMax(max, multipleOf);

        BigDecimal expected = max.subtract(new BigDecimal(multipleOf.toString()).abs().multiply(RandomNumberGenerator.HUNDRED));
        assertEquals(result, expected);
    }

    @Test
    public void testCalculateMinRelativeToMaxWithoutMultipleOf() {
        BigDecimal max = new BigDecimal("1000");

        BigDecimal result = RandomNumberGenerator.calculateMinRelativeToMax(max, null);

        BigDecimal expected = max.subtract(max.multiply(BigDecimal.valueOf(2)).max(RandomNumberGenerator.THOUSAND));
        assertEquals(result, expected);
    }

    @Test
    public void testCalculateMaxRelativeToMinWithMultipleOf() {
        BigDecimal min = new BigDecimal("1000");
        Number multipleOf = new BigDecimal("10");

        BigDecimal result = RandomNumberGenerator.calculateMaxRelativeToMin(min, multipleOf);

        BigDecimal expected = min.add(new BigDecimal(multipleOf.toString()).abs().multiply(RandomNumberGenerator.HUNDRED));
        assertEquals(result, expected);
    }

    @Test
    public void testCalculateMaxRelativeToMinWithoutMultipleOf() {
        BigDecimal min = new BigDecimal("1000");

        BigDecimal result = RandomNumberGenerator.calculateMaxRelativeToMin(min, null);

        BigDecimal expected = min.add(min.multiply(BigDecimal.valueOf(2)).max(RandomNumberGenerator.THOUSAND));
        assertEquals(result, expected);
    }

    @Test
    public void testHandlesWithIntegerType() {
        OasSchema schema = new Oas30Schema();
        schema.type = OpenApiConstants.TYPE_INTEGER;

        assertTrue(generator.handles(schema));
    }

    @Test
    public void testHandlesWithNumberType() {
        OasSchema schema = new Oas30Schema();
        schema.type = OpenApiConstants.TYPE_NUMBER;

        assertTrue(generator.handles(schema));
    }

    @Test
    public void testHandlesWithOtherType() {
        OasSchema schema = new Oas30Schema();
        schema.type = "string";

        assertFalse(generator.handles(schema));
    }

    @Test
    public void testHandlesWithNullType() {
        OasSchema schema = new Oas30Schema();
        schema.type = null;

        assertFalse(generator.handles(schema));
    }

    @Test
    public void testHandlesWithNullSchema() {
        assertFalse(generator.handles(null));
    }

    @Test(dataProvider = "findLeastSignificantDecimalPlace")
    public void findLeastSignificantDecimalPlace(BigDecimal number, int expectedSignificance) {
        assertEquals(generator.findLeastSignificantDecimalPlace(number), expectedSignificance);
    }
}
