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

import java.math.BigDecimal;
import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

public class AdvancedRandomNumberFunctionTest {

    private AdvancedRandomNumberFunction function;
    private AdvancedRandomNumberFunction functionWithRandomOne;
    private AdvancedRandomNumberFunction functionWithRandomZero;
    private TestContext context;

    @BeforeMethod
    public void setUp() {
        function = new AdvancedRandomNumberFunction();

        functionWithRandomOne = new AdvancedRandomNumberFunction() {
            @Override
            BigDecimal createRandomValue(BigDecimal minValue, BigDecimal range, double random) {
                random = 1.0;
                return super.createRandomValue(minValue, range, random);
            }
        };

        functionWithRandomZero = new AdvancedRandomNumberFunction() {
            @Override
            BigDecimal createRandomValue(BigDecimal minValue, BigDecimal range, double random) {
                random = 0.0;
                return super.createRandomValue(minValue, range, random);
            }
        };
        context = new TestContext();
    }

    @Test(invocationCount = 100)
    public void testRandomNumberWithNullParameter() {
        assertThatThrownBy(() -> function.execute(null, context))
            .isInstanceOf(InvalidFunctionUsageException.class)
            .hasMessage("Function parameters must not be null.");
    }

    @Test(invocationCount = 100)
    public void testRandomNumberWithDefaultValues() {
        List<String> params = List.of();
        String result = function.execute(params, context);
        assertThat(result).isNotNull().matches("-?\\d*\\.\\d{2}");
    }

    @Test(invocationCount = 100)
    public void testRandomNumberWithDecimalPlaces() {
        List<String> params = List.of("2");
        String result = function.execute(params, context);
        assertThat(result).isNotNull().matches("-?\\d*\\.\\d{2}");
    }

    @Test(invocationCount = 100)
    public void testRandomNumberWithinRange() {
        List<String> params = List.of("2", "10.5", "20.5");
        String result = function.execute(params, context);
        assertThat(result).isNotNull();
        assertThat(new BigDecimal(result)).isBetween(BigDecimal.valueOf(10.5),
            BigDecimal.valueOf(20.5));
    }

    @Test
    public void testRandomNumberIncludesMin() {
        List<String> params = List.of("1", "10.5", "20.5");
        String result = functionWithRandomZero.execute(params, context);
        assertThat(result).isEqualTo("10.5");
    }

    @Test
    public void testRandomNumberIncludesMax() {
        List<String> params = List.of("1", "10.5", "20.5");
        String result = functionWithRandomOne.execute(params, context);
        assertThat(result).isEqualTo("20.5");
    }

    @Test
    public void testRandomNumberExcludeMin() {
        List<String> params = List.of("1", "10.5", "20.5", "true", "false");
        String result = functionWithRandomZero.execute(params, context);
        assertThat(result).isNotNull();
        assertThat(new BigDecimal(result)).isGreaterThan(BigDecimal.valueOf(10.5))
            .isLessThanOrEqualTo(BigDecimal.valueOf(20.5));

        result = functionWithRandomOne.execute(params, context);
        assertThat(result).isNotNull();
        assertThat(new BigDecimal(result)).isGreaterThan(BigDecimal.valueOf(10.5))
            .isLessThanOrEqualTo(BigDecimal.valueOf(20.5));
    }

    @Test
    public void testRandomNumberExcludeMax() {
        List<String> params = List.of("2", "10.5", "20.5", "false", "true");
        String result = functionWithRandomZero.execute(params, context);
        assertThat(result).isNotNull();
        assertThat(new BigDecimal(result)).isGreaterThanOrEqualTo(BigDecimal.valueOf(10.5))
            .isLessThan(BigDecimal.valueOf(20.5));

        result = functionWithRandomOne.execute(params, context);
        assertThat(result).isNotNull();
        assertThat(new BigDecimal(result)).isGreaterThanOrEqualTo(BigDecimal.valueOf(10.5))
            .isLessThan(BigDecimal.valueOf(20.5));
    }

    @Test(invocationCount = 100)
    public void testRandomInteger32EdgeCase() {
        List<String> params = List.of("0", "-2147483648", "2147483647", "false", "false");

        String result = functionWithRandomZero.execute(params, context);
        assertThat(result).isNotNull();
        assertThat(new BigDecimal(result)).isGreaterThanOrEqualTo(
                BigDecimal.valueOf(Integer.MIN_VALUE))
            .isLessThanOrEqualTo(BigDecimal.valueOf(Integer.MAX_VALUE));

        result = functionWithRandomOne.execute(params, context);
        assertThat(result).isNotNull();
        assertThat(new BigDecimal(result)).isGreaterThanOrEqualTo(
                BigDecimal.valueOf(Integer.MIN_VALUE))
            .isLessThanOrEqualTo(BigDecimal.valueOf(Integer.MAX_VALUE));
    }

    @Test(invocationCount = 100)
    public void testRandomInteger32EdgeCaseExcluded() {
        List<String> params = List.of("0", "-2147483648", "2147483647", "true", "true");

        String result = functionWithRandomZero.execute(params, context);
        assertThat(result).isNotNull();
        assertThat(new BigDecimal(result)).isStrictlyBetween(BigDecimal.valueOf(Integer.MIN_VALUE),
            BigDecimal.valueOf(Integer.MAX_VALUE));

        result = functionWithRandomOne.execute(params, context);
        assertThat(result).isNotNull();
        assertThat(new BigDecimal(result)).isStrictlyBetween(BigDecimal.valueOf(Integer.MIN_VALUE),
            BigDecimal.valueOf(Integer.MAX_VALUE));
    }

    @Test(invocationCount = 100)
    public void testRandomInteger32MinEqualsMaxEdgeCase() {
        List<String> params = List.of("0", "3", "3", "false", "false");
        String result = function.execute(params, context);
        assertThat(result).isNotNull();
        assertThat(new BigDecimal(result)).isCloseTo(BigDecimal.valueOf(3.0),
            within(BigDecimal.valueOf(1e-10)));
    }

    @Test(invocationCount = 100)
    public void testRandomDouble32MinEqualsMaxEdgeCase() {
        List<String> params = List.of("2", "3.1", "3.1", "false", "false");
        String result = function.execute(params, context);
        assertThat(result).isNotNull();
        assertThat(new BigDecimal(result)).isCloseTo(BigDecimal.valueOf(3.1),
            within(BigDecimal.valueOf(1e-10)));
    }

    @Test
    public void testRandomInteger64EdgeCase() {
        List<String> params = List.of("0", "-9223372036854775808", "9223372036854775807", "false",
            "false");
        String result = functionWithRandomZero.execute(params, context);
        assertThat(result).isNotNull();
        assertThat(new BigDecimal(result)).isBetween(BigDecimal.valueOf(Long.MIN_VALUE),
            BigDecimal.valueOf(Long.MAX_VALUE));

        result = functionWithRandomOne.execute(params, context);
        assertThat(result).isNotNull();
        assertThat(new BigDecimal(result)).isBetween(BigDecimal.valueOf(Long.MIN_VALUE),
            BigDecimal.valueOf(Long.MAX_VALUE));
    }

    @Test
    public void testRandomInteger64EdgeCaseExcluded() {
        List<String> params = List.of("0", "-9223372036854775808", "9223372036854775807", "true",
            "true");
        String result = functionWithRandomZero.execute(params, context);
        assertThat(result).isNotNull();
        assertThat(new BigDecimal(result)).isStrictlyBetween(new BigDecimal(Long.MIN_VALUE),
            new BigDecimal(Long.MAX_VALUE));

        result = functionWithRandomOne.execute(params, context);
        assertThat(result).isNotNull();
        assertThat(new BigDecimal(result)).isStrictlyBetween(new BigDecimal(Long.MIN_VALUE),
            new BigDecimal(Long.MAX_VALUE));
    }

    @Test
    public void testRandomNumberFloatEdgeCase() {
        List<String> params = List.of("0", "-3.4028235E38", "3.4028235E38", "false", "false");
        String result = functionWithRandomZero.execute(params, context);
        assertThat(result).isNotNull();
        assertThat(new BigDecimal(result)).isCloseTo(new BigDecimal("-3.4028235E38"),
            within(BigDecimal.valueOf(0.1)));

        result = functionWithRandomOne.execute(params, context);
        assertThat(result).isNotNull();
        assertThat(new BigDecimal(result)).isCloseTo(new BigDecimal("3.4028235E38"),
            within(BigDecimal.valueOf(0.1)));
    }

    @Test
    public void testRandomNumberFloatEdgeCaseExcluded() {
        List<String> params = List.of("0", "-3.4028235E38", "3.4028235E38", "true", "true");
        String result = functionWithRandomZero.execute(params, context);
        assertThat(result).isNotNull();
        assertThat(new BigDecimal(result)).isGreaterThan(new BigDecimal("-3.4028235E38"));

        result = functionWithRandomOne.execute(params, context);
        assertThat(result).isNotNull();
        assertThat(new BigDecimal(result)).isLessThan(new BigDecimal("3.4028235E38"));
    }

    @Test
    public void testRandomNumberDoubleEdgeCase() {
        List<String> params = List.of("0", "-1.7976931348623157E308", "1.7976931348623157E308",
            "false", "false");
        String result = functionWithRandomZero.execute(params, context);
        assertThat(result).isNotNull();
        assertThat(new BigDecimal(result)).isCloseTo(new BigDecimal("-1.7976931348623157E308"),
            within(BigDecimal.valueOf(0.1)));

        result = functionWithRandomOne.execute(params, context);
        assertThat(result).isNotNull();
        assertThat(new BigDecimal(result)).isCloseTo(new BigDecimal("1.7976931348623157E308"),
            within(BigDecimal.valueOf(0.1)));
    }

    @Test
    public void testRandomNumberDoubleEdgeCaseExcluded() {
        List<String> params = List.of("0", "-1.7976931348623157E308", "1.7976931348623157E308",
            "true", "true");
        String result = functionWithRandomZero.execute(params, context);
        assertThat(result).isNotNull();
        assertThat(new BigDecimal(result)).isGreaterThan(new BigDecimal("-1.7976931348623157E308"));

        result = functionWithRandomOne.execute(params, context);
        assertThat(result).isNotNull();
        assertThat(new BigDecimal(result)).isLessThan(new BigDecimal("1.7976931348623157E308"));
    }

    @Test(invocationCount = 100)
    public void testInvalidDecimalPlaces() {
        List<String> params = List.of("-1");
        assertThatThrownBy(() -> function.execute(params, context))
            .isInstanceOf(InvalidFunctionUsageException.class)
            .hasMessage("Decimal places must be a non-negative integer value.");
    }

    @Test(invocationCount = 100)
    public void testInvalidRange() {
        List<String> params = List.of("2", "20.5", "10.5");
        assertThatThrownBy(() -> function.execute(params, context))
            .isInstanceOf(InvalidFunctionUsageException.class)
            .hasMessage("Min value must be less than max value.");
    }

    @Test(invocationCount = 100)
    public void testInvalidDecimalPlacesFormat() {
        List<String> params = List.of("xxx");
        assertThatThrownBy(() -> function.execute(params, context))
            .isInstanceOf(InvalidFunctionUsageException.class)
            .hasMessage("Invalid parameter at index 1. xxx must be parsable to Integer.");
    }

    @Test(invocationCount = 100)
    public void testInvalidMinValueFormat() {
        List<String> params = List.of("1", "xxx");
        assertThatThrownBy(() -> function.execute(params, context))
            .isInstanceOf(InvalidFunctionUsageException.class)
            .hasMessage("Invalid parameter at index 2. xxx must be parsable to BigDecimal.");
    }

    @Test(invocationCount = 100)
    public void testInvalidMaxValueFormat() {
        List<String> params = List.of("1", "1.1", "xxx"); // invalid max value
        assertThatThrownBy(() -> function.execute(params, context)).isInstanceOf(
                InvalidFunctionUsageException.class)
            .hasMessage("Invalid parameter at index 3. xxx must be parsable to BigDecimal.");
    }

    @Test(invocationCount = 100)
    public void testSpecificFormat() {
        List<String> params = List.of("3", "1234567", "1234568", "false", "false", "null", "#,###.000" ); // invalid max value
        String result = function.execute(params, context);
        assertThat(result).matches("1,\\d{3},\\d{3}\\.\\d{3}");

        params = List.of("3", "1234567", "1234568", "false", "false", "null", "#.000" ); // invalid max value
        result = function.execute(params, context);
        assertThat(result).matches("1\\d{6}\\.\\d{3}");

        params = List.of("3", "1234567", "1234568", "false", "false", "null", "#.###E0" ); // invalid max value
        result = function.execute(params, context);
        assertThat(result).isEqualTo("1.235E6");
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

    @Test(dataProvider = "testRandomNumber", invocationCount = 100)
    void testRandomNumber(Number decimalPlaces, Number multipleOf, Number minimum, Number maximum,
        boolean exclusiveMinimum, boolean exclusiveMaximum) {

        TestContext testContext = new TestContext();
        AdvancedRandomNumberFunction advancedRandomNumberFunction = new AdvancedRandomNumberFunction();
        BigDecimal value = new BigDecimal(advancedRandomNumberFunction.execute(
            List.of(toString(decimalPlaces), toString(minimum), toString(maximum),
                toString(exclusiveMinimum), toString(exclusiveMaximum), toString(multipleOf)),
            testContext));

        if (multipleOf != null) {
            BigDecimal remainder = value.remainder(new BigDecimal(multipleOf.toString()));
            assertThat(remainder).isEqualByComparingTo(BigDecimal.ZERO);
        }

        if (maximum != null) {
            if (exclusiveMaximum) {
                assertThat(value.doubleValue()).isLessThan(maximum.doubleValue());
            } else {
                assertThat(value.doubleValue())
                    .isLessThanOrEqualTo(maximum.doubleValue());
            }
        }

        if (minimum != null) {
            if (exclusiveMinimum) {
                assertThat(value.doubleValue()).isGreaterThan(minimum.doubleValue());
            } else {
                assertThat(value.doubleValue())
                    .isGreaterThanOrEqualTo(minimum.doubleValue());
            }
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
