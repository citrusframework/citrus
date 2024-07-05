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
import java.util.Random;
import org.citrusframework.context.TestContext;
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
 *     <li>Exclude man: Whether to exclude the maximum value (optional, default: false).</li>
 * </ol>
 * <p>
 *  This function differs from the {@link RandomNumberFunction} in several key ways:
 *  <ul>
 *     <li>It allows to specify several aspects of a number (see above).</li>
 *     <li>The length of the number is restricted to the range and precision of a double, whereas RandomNumberFunction can create arbitrarily long integer values.</li>
 *  </ul>
 */
public class AdvancedRandomNumberFunction implements Function {

    /**
     * Basic seed generating random number
     */
    private static final Random generator = new Random(System.currentTimeMillis());

    public String execute(List<String> parameterList, TestContext context) {
        int decimalPlaces = 0;
        double minValue = -1000000;
        double maxValue = 1000000;
        boolean excludeMin = false;
        boolean excludeMax = false;

        if (parameterList == null) {
            throw new InvalidFunctionUsageException("Function parameters must not be null.");
        }

        if (!parameterList.isEmpty()) {
            decimalPlaces = parseParameter(1, parameterList.get(0), Integer.class,
                Integer::parseInt);
            if (decimalPlaces < 0) {
                throw new InvalidFunctionUsageException(
                    "Invalid parameter definition. Decimal places must be a non-negative integer value.");
            }
        }

        if (parameterList.size() > 1) {
            minValue = parseParameter(2, parameterList.get(1), Double.class, Double::parseDouble);
        }

        if (parameterList.size() > 2) {
            maxValue = parseParameter(3, parameterList.get(2), Double.class, Double::parseDouble);
            if (minValue > maxValue) {
                throw new InvalidFunctionUsageException(
                    "Invalid parameter definition. Min value must be less than max value.");
            }
        }

        if (parameterList.size() > 3) {
            excludeMin = parseParameter(4, parameterList.get(3), Boolean.class,
                Boolean::parseBoolean);
        }

        if (parameterList.size() > 4) {
            excludeMax = parseParameter(5, parameterList.get(4), Boolean.class,
                Boolean::parseBoolean);
        }

        return getRandomNumber(decimalPlaces, minValue, maxValue, excludeMin, excludeMax);
    }

    private <T> T parseParameter(int index, String text, Class<T> type,
        java.util.function.Function<String, T> parseFunction) {
        try {
            return parseFunction.apply(text);
        } catch (Exception e) {
            throw new InvalidFunctionUsageException(
                format("Invalid parameter at index %d. %s must be parsable to %s.", index, text,
                    type.getSimpleName()));
        }
    }

    /**
     * Static number generator method.
     */
    private String getRandomNumber(int decimalPlaces, double minValue, double maxValue,
        boolean excludeMin, boolean excludeMax) {
        double adjustment = Math.pow(10, -decimalPlaces);

        if (excludeMin) {
            minValue += adjustment;
        }

        if (excludeMax) {
            maxValue -= adjustment;
        }

        BigDecimal range = BigDecimal.valueOf(maxValue).subtract(BigDecimal.valueOf(minValue));

        double randomValue = getRandomValue(minValue, range, generator.nextDouble());
        BigDecimal bd = new BigDecimal(Double.toString(randomValue));
        bd = bd.setScale(2, RoundingMode.HALF_UP);

        return decimalPlaces == 0 ?
            format("%s", bd.longValue()) :
            format(format("%%.%sf", decimalPlaces), bd.doubleValue());
    }

    double getRandomValue(double minValue, BigDecimal range, double random) {
        BigDecimal offset = range.multiply(BigDecimal.valueOf(random));
        BigDecimal value =  BigDecimal.valueOf(minValue).add(offset);
        return value.compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) > 0 ? Double.MAX_VALUE : value.doubleValue();
    }

}