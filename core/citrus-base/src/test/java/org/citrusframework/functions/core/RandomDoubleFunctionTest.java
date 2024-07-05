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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RandomDoubleFunctionTest {

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
        assertTrue(result.matches("-?\\d*"));
    }

    @Test
    public void testRandomNumberWithDecimalPlaces() {
        List<String> params = List.of("2");
        String result = function.execute(params, context);
        assertNotNull(result);
        assertTrue(result.matches("-?\\d*\\.\\d{2}"), "result does not match pattern: "+result);
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
            double getRandomValue(double minValue, BigDecimal range, double random) {
                random = 0.0;
                return super.getRandomValue(minValue, range, random);
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
            double getRandomValue(double minValue, BigDecimal range, double random) {
                random = 1.0;
                return super.getRandomValue(minValue, range, random);
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
            double getRandomValue(double minValue, BigDecimal range, double random) {
                random = 0.0;
                return super.getRandomValue(minValue, range, random);
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
            double getRandomValue(double minValue, BigDecimal range, double random) {
                random = 1.0;
                return super.getRandomValue(minValue, range, random);
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
        for (int i =0;i<100;i++) {
            String result = function.execute(params, context);
            assertNotNull(result);
            double randomValue = Double.parseDouble(result);
            assertEquals(randomValue, 3);
        }
    }



    // randomDouble('0','3','3','true','true')
    // randomDouble('0','3','3','true','true')

    @Test
    public void testRandomDouble32MinEqualsMaxEdgeCase() {
        List<String> params = List.of("2", "3.0", "3.0", "false", "false");
        for (int i =0;i<100;i++) {
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
        assertTrue(randomValue >=-Long.MAX_VALUE && randomValue < Long.MAX_VALUE);
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
        InvalidFunctionUsageException exception = expectThrows(InvalidFunctionUsageException.class, () -> function.execute(params, context));
        assertEquals(exception.getMessage(), "Invalid parameter definition. Decimal places must be a non-negative integer value.");
    }

    @Test
    public void testInvalidRange() {
        List<String> params = List.of("2", "20.5", "10.5"); // invalid range
        InvalidFunctionUsageException exception = expectThrows(InvalidFunctionUsageException.class, () -> function.execute(params, context));
        assertEquals(exception.getMessage(), "Invalid parameter definition. Min value must be less than max value.");
    }

    @Test
    public void testInvalidDecimalPlacesFormat() {
        List<String> params = List.of("xxx"); // invalid decimalPlaces
        InvalidFunctionUsageException exception = expectThrows(InvalidFunctionUsageException.class, () -> function.execute(params, context));
        assertEquals(exception.getMessage(), "Invalid parameter at index 1. xxx must be parsable to Integer.");
    }

    @Test
    public void testInvalidMinValueFormat() {
        List<String> params = List.of("1","xxx"); // invalid min value
        InvalidFunctionUsageException exception = expectThrows(InvalidFunctionUsageException.class, () -> function.execute(params, context));
        assertEquals(exception.getMessage(), "Invalid parameter at index 2. xxx must be parsable to Double.");
    }

    @Test
    public void testInvalidMaxValueFormat() {
        List<String> params = List.of("1", "1.1", "xxx"); // invalid max value
        InvalidFunctionUsageException exception = expectThrows(InvalidFunctionUsageException.class,
            () -> function.execute(params, context));
        assertEquals(exception.getMessage(),
            "Invalid parameter at index 3. xxx must be parsable to Double.");
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