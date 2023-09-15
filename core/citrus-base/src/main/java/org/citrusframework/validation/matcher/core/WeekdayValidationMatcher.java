/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.validation.matcher.core;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.validation.matcher.ControlExpressionParser;
import org.citrusframework.validation.matcher.ValidationMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.PatternSyntaxException;

/**
 * Special validation matcher implementation checks that a given date matches an
 * expected weekday.
 * <p/>
 * Control weekday value is one of these strings: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
 * <p/>
 * In addition to that user can specify the date format to parse:
 * MONDAY(YYYY-MM-DD)
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class WeekdayValidationMatcher implements ValidationMatcher, ControlExpressionParser {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(WeekdayValidationMatcher.class);

    @Override
    public void validate(String fieldName, String value, List<String> controlParameters, TestContext context) throws ValidationException {
        SimpleDateFormat dateFormat;
        String weekday = controlParameters.get(0);
        String formatString = "dd.MM.yyyy";

        if (controlParameters.size() == 2) {
            // override the default date format
            formatString = controlParameters.get(1);
        }

        try {
            dateFormat = new SimpleDateFormat(formatString);
        } catch (PatternSyntaxException e) {
            throw new ValidationException(this.getClass().getSimpleName() + " failed for field '" + fieldName + "' " +
                    ". Found invalid date format", e);
        }

        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateFormat.parse(value));

            if (cal.get(Calendar.DAY_OF_WEEK) == Weekday.valueOf(weekday).getConstantValue()) {
                logger.info("Weekday validation matcher successful - All values OK");
            } else {
                throw new ValidationException(this.getClass().getSimpleName() + " failed for field '" + fieldName + "'" +
                        ". Received invalid week day '" + value + "', expected date to be a '" + weekday + "'");
            }
        } catch (ParseException e) {
            throw new ValidationException(this.getClass().getSimpleName() + " failed for field '" + fieldName + "'" +
                    ". Received invalid date format for value '" + value + "', expected date format is '" + formatString + "'", e);
        }
    }

    @Override
    public List<String> extractControlValues(String controlExpression, Character delimiter) {
        List<String> parameters = new ArrayList<>();

        if (controlExpression.contains("(")) {
            parameters.add(controlExpression.substring(0, controlExpression.indexOf("('")));
            parameters.add(controlExpression.substring(controlExpression.indexOf("('") + 2, controlExpression.length() - 2));
        } else {
            parameters.add(controlExpression);
        }
        return parameters;
    }

    /**
     * Weekday enumeration links names to Java util Calendar constants.
     */
    private enum Weekday {
        MONDAY(Calendar.MONDAY),
        TUESDAY(Calendar.TUESDAY),
        WEDNESDAY(Calendar.WEDNESDAY),
        THURSDAY(Calendar.THURSDAY),
        FRIDAY(Calendar.FRIDAY),
        SATURDAY(Calendar.SATURDAY),
        SUNDAY(Calendar.SUNDAY);

        private int constantValue;

        Weekday(int constant) {
            this.constantValue = constant;
        }

        public int getConstantValue() {
            return this.constantValue;
        }
    }
}

