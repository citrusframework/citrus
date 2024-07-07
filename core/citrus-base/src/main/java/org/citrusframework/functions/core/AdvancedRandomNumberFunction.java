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

import static java.lang.String.format;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.Function;

/**
 * A function for generating random double values with specified decimal places and range. This
 * function includes options to specify the number of decimal places, minimum and maximum values,
 * and whether to include or exclude the minimum and maximum values.
 * <p>
 * Parameters:
 * <ol>
 *     <li>Decimal places: The number of decimal places in the generated random number (optional, default: 0). Note that definition of 0 results in an integer.</li>
 *     <li>Min value: The minimum value for the generated random number (optional, default: Double.MIN_VALUE).</li>
 *     <li>Max value: The maximum value for the generated random number (optional, default: Double.MAX_VALUE).</li>
 *     <li>Exclude min: Whether to exclude the minimum value (optional, default: false).</li>
 *     <li>Exclude max: Whether to exclude the maximum value (optional, default: false).</li>
 *     <li>Multiple of: The generated number will be a multiple of this value (optional).</li>
 * </ol>
 * <p>
 * This function differs from the {@link RandomNumberFunction} in several key ways:
 * <ul>
 *     <li>It allows to specify several aspects of a number (see above).</li>
 *     <li>The length of the number is restricted to the range and precision of a double, whereas RandomNumberFunction can create arbitrarily long integer values.</li>
 * </ul>
 */
public class AdvancedRandomNumberFunction implements Function {

    public static final BigDecimal DEFAULT_MAX_VALUE = new BigDecimal(1000000);
    public static final BigDecimal DEFAULT_MIN_VALUE = DEFAULT_MAX_VALUE.negate();

    public String execute(List<String> parameterList, TestContext context) {
        if (parameterList == null) {
            throw new InvalidFunctionUsageException("Function parameters must not be null.");
        }

        int decimalPlaces = getParameter(parameterList, 0, Integer.class, Integer::parseInt, 2);
        if (decimalPlaces < 0) {
            throw new InvalidFunctionUsageException(
                "Decimal places must be a non-negative integer value.");
        }

        BigDecimal minValue = getParameter(parameterList, 1, BigDecimal.class, BigDecimal::new,
            DEFAULT_MIN_VALUE);
        BigDecimal maxValue = getParameter(parameterList, 2, BigDecimal.class, BigDecimal::new,
            DEFAULT_MAX_VALUE);
        if (minValue.compareTo(maxValue) > 0) {
            throw new InvalidFunctionUsageException("Min value must be less than max value.");
        }

        boolean excludeMin = getParameter(parameterList, 3, Boolean.class, Boolean::parseBoolean,
            false);
        boolean excludeMax = getParameter(parameterList, 4, Boolean.class, Boolean::parseBoolean,
            false);
        BigDecimal multiple = getParameter(parameterList, 5, BigDecimal.class, BigDecimal::new,
            null);

        return getRandomNumber(decimalPlaces, minValue, maxValue, excludeMin, excludeMax, multiple);
    }

    private <T> T getParameter(List<String> params, int index, Class<T> type,
        java.util.function.Function<String, T> parser, T defaultValue) {
        if (index < params.size()) {
            String param = params.get(index);
            return "null".equals(param) ? defaultValue
                : parseParameter(index + 1, param, type, parser);
        }
        return defaultValue;
    }

    private <T> T parseParameter(int index, String text, Class<T> type,
        java.util.function.Function<String, T> parseFunction) {
        T value;
        try {

            value = parseFunction.apply(text);
            if (value == null) {
                throw new CitrusRuntimeException(
                    "Text '%s' could not be parsed to '%s'. Resulting value is null".formatted(text,
                        type.getSimpleName()));
            }
            return value;
        } catch (Exception e) {
            throw new InvalidFunctionUsageException(
                format("Invalid parameter at index %d. %s must be parsable to %s.", index, text,
                    type.getSimpleName()));
        }
    }

    /**
     * Static number generator method.
     */
    private String getRandomNumber(int decimalPlaces, BigDecimal minValue, BigDecimal maxValue,
        boolean excludeMin, boolean excludeMax, BigDecimal multiple) {

        minValue = excludeMin ? incrementToExclude(minValue) : minValue;
        maxValue = excludeMax ? decrementToExclude(maxValue) : maxValue;

        BigDecimal range = maxValue.subtract(minValue);

        BigDecimal randomValue;
        if (multiple != null) {
            randomValue = createMultipleOf(minValue, maxValue, multiple);
        } else {
            randomValue = createRandomValue(minValue, range,
                ThreadLocalRandom.current().nextDouble());
            randomValue = randomValue.setScale(decimalPlaces, RoundingMode.HALF_UP);
        }

        if (randomValue == null) {
            // May only happen if multiple is out of range of min/max
            return format("%s", Double.POSITIVE_INFINITY);
        }

        return decimalPlaces == 0 ?
            format("%s", randomValue.longValue()) :
            format(format("%%.%sf", decimalPlaces), randomValue.doubleValue());
    }

    // Pass in random for testing
    BigDecimal createRandomValue(BigDecimal minValue, BigDecimal range, double random) {
        BigDecimal offset = range.multiply(BigDecimal.valueOf(random));
        BigDecimal value = minValue.add(offset);
        return value.compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) > 0 ? BigDecimal.valueOf(
            Double.MAX_VALUE) : value;
    }

    private BigDecimal largestMultipleOf(BigDecimal highest, BigDecimal multipleOf) {
        RoundingMode roundingMode =
            highest.compareTo(BigDecimal.ZERO) < 0 ? RoundingMode.UP : RoundingMode.DOWN;
        BigDecimal factor = highest.divide(multipleOf, 0, roundingMode);
        return multipleOf.multiply(factor);
    }

    private BigDecimal lowestMultipleOf(BigDecimal lowest, BigDecimal multipleOf) {
        RoundingMode roundingMode =
            lowest.compareTo(java.math.BigDecimal.ZERO) < 0 ? RoundingMode.DOWN : RoundingMode.UP;
        BigDecimal factor = lowest.divide(multipleOf, 0, roundingMode);
        return multipleOf.multiply(factor);
    }

    private BigDecimal incrementToExclude(BigDecimal val) {
        return val.add(determineIncrement(val))
            .setScale(findLeastSignificantDecimalPlace(val), RoundingMode.HALF_DOWN);
    }

    private BigDecimal decrementToExclude(BigDecimal val) {
        return val.subtract(determineIncrement(val))
            .setScale(findLeastSignificantDecimalPlace(val), RoundingMode.HALF_DOWN);
    }

    private BigDecimal determineIncrement(BigDecimal number) {
        return java.math.BigDecimal.valueOf(
            1.0d / (Math.pow(10d, findLeastSignificantDecimalPlace(number))));
    }

    private int findLeastSignificantDecimalPlace(BigDecimal number) {
        number = number.stripTrailingZeros();

        String[] parts = number.toPlainString().split("\\.");

        if (parts.length == 1) {
            return 0;
        }

        return parts[1].length();
    }

    private BigDecimal createMultipleOf(
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

        return randomMultiple;
    }

}