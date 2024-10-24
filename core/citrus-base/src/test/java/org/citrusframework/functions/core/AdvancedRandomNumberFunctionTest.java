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

package org.citrusframework.functions.core;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AdvancedRandomNumberFunctionTest {

    private AdvancedRandomNumberFunction function;
    private TestContext context;

    @BeforeMethod
    public void setUp() {
        function = new AdvancedRandomNumberFunction();
        context = new TestContext();
    }

    @Test
    public void testRandomNumberWithNullParameter() {
        InvalidFunctionUsageException exception = expectThrows(InvalidFunctionUsageException.class,
            () -> function.execute(null, context));
        assertEquals(exception.getMessage(),
            "Function parameters must not be null.");
    }

    @Test
    public void testRandomNumberWithDefaultValues() {
        List<String> params = List.of();
        String result = function.execute(params, context);
        assertNotNull(result);
        assertTrue(result.matches("-?\\d*\\.\\d{2}"));
    }

    @Test
    public void testRandomNumberWithDecimalPlaces() {
        List<String> params = List.of("2");
        String result = function.execute(params, context);
        assertNotNull(result);
        assertTrue(result.matches("-?\\d*\\.\\d{2}"), "result does not match pattern: " + result);
    }

    @Test
    public void testRandomNumberWithinRange() {
        List<String> params = List.of("2", "10.5", "20.5");
        String result = function.execute(params, context);
        assertNotNull(result);
        double randomValue = Double.parseDouble(result);
        assertTrue(randomValue >= 10.5 && randomValue <= 20.5);
    }

    @Test
    public void testRandomNumberIncludesMin() {
        List<String> params = List.of("1", "10.5", "20.5");
        function = new AdvancedRandomNumberFunction() {
            @Override
            BigDecimal createRandomValue(BigDecimal minValue, BigDecimal range, double random) {
                random = 0.0;
                return super.createRandomValue(minValue, range, random);
            }
        };
        String result = function.execute(params, context);
        assertEquals(result, "10.5");
    }

    @Test
    public void testRandomNumberIncludesMax() {
        List<String> params = List.of("1", "10.5", "20.5");
        function = new AdvancedRandomNumberFunction() {
            @Override
            BigDecimal createRandomValue(BigDecimal minValue, BigDecimal range, double random) {
                random = 1.0;
                return super.createRandomValue(minValue, range, random);
            }
        };
        String result = function.execute(params, context);
        assertEquals(result, "20.5");
    }

    @Test
    public void testRandomNumberExcludeMin() {
        List<String> params = List.of("1", "10.5", "20.5", "true", "false");
        function = new AdvancedRandomNumberFunction() {
            @Override
            BigDecimal createRandomValue(BigDecimal minValue, BigDecimal range, double random) {
                random = 0.0;
                return super.createRandomValue(minValue, range, random);
            }
        };
        String result = function.execute(params, context);
        assertNotNull(result);
        double randomValue = Double.parseDouble(result);
        assertTrue(randomValue > 10.5 && randomValue <= 20.5);
    }

    @Test
    public void testRandomNumberExcludeMax() {
        List<String> params = List.of("2", "10.5", "20.5", "false", "true");
        function = new AdvancedRandomNumberFunction() {
            @Override
            BigDecimal createRandomValue(BigDecimal minValue, BigDecimal range, double random) {
                random = 1.0;
                return super.createRandomValue(minValue, range, random);
            }
        };
        String result = function.execute(params, context);
        assertNotNull(result);
        double randomValue = Double.parseDouble(result);
        assertTrue(randomValue >= 10.5 && randomValue < 20.5);
    }

    @Test
    public void testRandomInteger32EdgeCase() {
        List<String> params = List.of("0", "-2147483648", "2147483647", "false", "false");
        String result = function.execute(params, context);
        assertNotNull(result);
        double randomValue = Double.parseDouble(result);
        assertTrue(randomValue >= -Integer.MAX_VALUE && randomValue < Integer.MAX_VALUE);
    }

    @Test
    public void testRandomInteger32MinEqualsMaxEdgeCase() {
        List<String> params = List.of("0", "3", "3", "false", "false");
        for (int i = 0; i < 100; i++) {
            String result = function.execute(params, context);
            assertNotNull(result);
            double randomValue = Double.parseDouble(result);
            assertEquals(randomValue, 3);
        }
    }

    @Test
    public void testRandomDouble32MinEqualsMaxEdgeCase() {
        List<String> params = List.of("2", "3.0", "3.0", "false", "false");
        for (int i = 0; i < 100; i++) {
            String result = function.execute(params, context);
            assertNotNull(result);
            double randomValue = Double.parseDouble(result);
            assertEquals(randomValue, 3);
        }
    }

    @Test
    public void testRandomInteger64EdgeCase() {
        List<String> params = List.of("0", "-9223372036854775808", "9223372036854775807", "false", "false");
        String result = function.execute(params, context);
        assertNotNull(result);
        double randomValue = Double.parseDouble(result);
        assertTrue(randomValue >= -Long.MAX_VALUE && randomValue < Long.MAX_VALUE);
    }

    @Test
    public void testRandomNumberFloatEdgeCase() {
        List<String> params = List.of("0", "-3.4028235E38", "3.4028235E38", "false", "false");
        String result = function.execute(params, context);
        assertNotNull(result);
        double randomValue = Double.parseDouble(result);
        assertTrue(randomValue >= -Float.MAX_VALUE && randomValue < Float.MAX_VALUE);
    }

    @Test
    public void testRandomNumberDoubleEdgeCase() {
        List<String> params = List.of("0", "-1.7976931348623157E308", "1.7976931348623157E308", "false", "false");
        String result = function.execute(params, context);
        assertNotNull(result);
        double randomValue = Double.parseDouble(result);
        assertTrue(randomValue >= -Double.MAX_VALUE && randomValue < Double.MAX_VALUE);
    }

    @Test
    public void testInvalidDecimalPlaces() {
        List<String> params = List.of("-1"); // invalid decimalPlaces
        InvalidFunctionUsageException exception = expectThrows(InvalidFunctionUsageException.class,
            () -> function.execute(params, context));
        assertEquals(exception.getMessage(),
            "Decimal places must be a non-negative integer value.");
    }

    @Test
    public void testInvalidRange() {
        List<String> params = List.of("2", "20.5", "10.5"); // invalid range
        InvalidFunctionUsageException exception = expectThrows(InvalidFunctionUsageException.class,
            () -> function.execute(params, context));
        assertEquals(exception.getMessage(),
            "Min value must be less than max value.");
    }

    @Test
    public void testInvalidDecimalPlacesFormat() {
        List<String> params = List.of("xxx"); // invalid decimalPlaces
        InvalidFunctionUsageException exception = expectThrows(InvalidFunctionUsageException.class,
            () -> function.execute(params, context));
        assertEquals(exception.getMessage(),
            "Invalid parameter at index 1. xxx must be parsable to Integer.");
    }

    @Test
    public void testInvalidMinValueFormat() {
        List<String> params = List.of("1", "xxx"); // invalid min value
        InvalidFunctionUsageException exception = expectThrows(InvalidFunctionUsageException.class,
            () -> function.execute(params, context));
        assertEquals(exception.getMessage(),
            "Invalid parameter at index 2. xxx must be parsable to BigDecimal.");
    }

    @Test
    public void testInvalidMaxValueFormat() {
        List<String> params = List.of("1", "1.1", "xxx"); // invalid max value
        InvalidFunctionUsageException exception = expectThrows(InvalidFunctionUsageException.class,
            () -> function.execute(params, context));
        assertEquals(exception.getMessage(),
            "Invalid parameter at index 3. xxx must be parsable to BigDecimal.");
    }

    @DataProvider(name = "testRandomNumber")
    public static Object[][] testRandomNumber() {
        return new Object[][]{
            {0, 12, null, null, false, false},
            {0, null, 0, 2, true, true},
            {0, null, null, null, false, false},
            {0, null, 0, 100, false, false},
            {0, null, 0, 2, false, false},
            {0, null, -100, 0, false, false},
            {0, null, -2, 0, false, false},
            {0, null, 0, 100, true, true},
            {0, null, -100, 0, true, true},
            {0, null, -2, 0, true, true},
            {0, null, 0, null, false, false},
            {0, null, 0, 0, false, false},
            {0, 11, 0, 12, true, true},

            {0, 13, 0, 100, false, false},
            {0, 14, 0, 14, false, false},
            {0, 15, -100, 0, false, false},
            {0, 16, -16, 0, false, false},
            {0, 17, 0, 100, true, true},
            {0, 18, -100, 0, true, true},
            {0, 19, -20, 0, true, true},
            {0, 20, 0, null, false, false},
            {0, 21, 21, 21, false, false},

            {0, null, 0, 2, true, true},
            {0, null, null, null, false, false},
            {0, null, 0, 100, false, false},
            {0, null, 0, 2, false, false},
            {0, null, -100, 0, false, false},
            {0, null, -2, 0, false, false},
            {0, null, 0, 100, true, true},
            {0, null, -100, 0, true, true},
            {0, null, -2, 0, true, true},
            {0, null, 0, null, false, false},
            {0, null, 0, 0, false, false},
            {0, 11, 0, 12, true, true},
            {0, 12, null, null, false, false},
            {0, 13, 0, 100, false, false},
            {0, 14, 0, 14, false, false},
            {0, 15, -100, 0, false, false},
            {0, 16, -16, 0, false, false},
            {0, 17, 0, 100, true, true},
            {0, 18, -100, 0, true, true},
            {0, 19, -20, 0, true, true},
            {0, 20, 0, null, false, false},
            {0, 21, 21, 21, false, false},

            {3, null, 0, 2, true, true},
            {3, null, null, null, false, false},
            {3, null, 0, 100, false, false},
            {3, null, 0, 2, false, false},
            {3, null, -100, 0, false, false},
            {3, null, -2, 0, false, false},
            {3, null, 0, 100, true, true},
            {3, null, -100, 0, true, true},
            {3, null, -2, 0, true, true},
            {3, null, 0, null, false, false},
            {3, null, 0, 0, false, false},
            {3, 11.123f, 0, 13, true, true},
            {3, 12.123f, null, null, false, false},
            {3, 13.123f, 0, 100, false, false},
            {3, 14.123f, 0, 14, false, false},
            {3, 15.123f, -100, 0, false, false},
            {3, 16.123f, -16, 0, false, false},
            {3, 17.123f, 0, 100, true, true},
            {3, 18.123f, -100, 0, true, true},
            {3, 19.123f, -21, 0, true, true},
            {3, 20.123f, 0, null, false, false},
            {3, 21.123f, 21.122f, 21.124f, false, false},

            {5, null, 0, 2, true, true},
            {5, null, null, null, false, false},
            {5, null, 0, 100, false, false},
            {5, null, 0, 2, false, false},
            {5, null, -100, 0, false, false},
            {5, null, -2, 0, false, false},
            {5, null, 0, 100, true, true},
            {5, null, -100, 0, true, true},
            {5, null, -2, 0, true, true},
            {5, null, 0, null, false, false},
            {5, null, 0, 0, false, false},
            {5, 11.123d, 0, 13, true, true},
            {5, 12.123d, null, null, false, false},
            {5, 13.123d, 0, 100, false, false},
            {5, 14.123d, 0, 14, false, false},
            {5, 15.123d, -100, 0, false, false},
            {5, 16.123d, -16, 0, false, false},
            {5, 17.123d, 0, 100, true, true},
            {5, 18.123d, -100, 0, true, true},
            {5, 19.123d, -21, 0, true, true},
            {5, 20.123d, 0, null, false, false},
            {5, 21.123d, 21.122d, 21.124d, false, false},
        };
    }

    @Test(dataProvider = "testRandomNumber")
    void testRandomNumber(Number decimalPlaces, Number multipleOf, Number minimum, Number maximum,
        boolean exclusiveMinimum, boolean exclusiveMaximum) {

        TestContext testContext = new TestContext();
        AdvancedRandomNumberFunction advancedRandomNumberFunction = new AdvancedRandomNumberFunction();
        try {
            for (int i = 0; i < 1000; i++) {

                BigDecimal value = new BigDecimal(advancedRandomNumberFunction.execute(
                    List.of(toString(decimalPlaces), toString(minimum), toString(maximum),
                        toString(exclusiveMinimum), toString(exclusiveMaximum), toString(multipleOf)), testContext));

                if (multipleOf != null) {
                    BigDecimal remainder = value.remainder(new BigDecimal(multipleOf.toString()));

                    assertEquals(
                        remainder.compareTo(BigDecimal.ZERO), 0,
                        "Expected %s to be a multiple of %s! Remainder is %s".formatted(
                            value, multipleOf,
                            remainder));
                }

                if (maximum != null) {
                    if (exclusiveMaximum) {
                        assertTrue(value.doubleValue() < maximum.doubleValue(),
                            "Expected %s to be lower than %s!".formatted(
                                value, maximum));
                    } else {
                        assertTrue(value.doubleValue() <= maximum.doubleValue(),
                            "Expected %s to be lower or equal than %s!".formatted(
                                value, maximum));
                    }
                }

                if (minimum != null) {
                    if (exclusiveMinimum) {
                        assertTrue(value.doubleValue() > minimum.doubleValue(),
                            "Expected %s to be larger than %s!".formatted(
                                value, minimum));
                    } else {
                        assertTrue(value.doubleValue() >= minimum.doubleValue(),
                            "Expected %s to be larger or equal than %s!".formatted(
                                value, minimum));
                    }
                }
            }
        } catch (Exception e) {
            Assert.fail("Creation of multiple float threw an exception: " + e.getMessage(), e);
        }
    }

    private String toString(Object obj) {
        if (obj == null) {
            return "null";
        }

        return obj.toString();
    }

    private <T extends Throwable> T expectThrows(Class<T> exceptionClass, Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            if (exceptionClass.isInstance(throwable)) {
                return exceptionClass.cast(throwable);
            } else {
                throw new AssertionError("Unexpected exception type", throwable);
            }
        }

        throw new AssertionError("Expected exception not thrown");
    }
}
