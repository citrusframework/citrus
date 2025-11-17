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

package org.citrusframework.validation.matcher.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.validation.matcher.ParameterizedValidationMatcher;
import org.citrusframework.yaml.SchemaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validation matcher for verifying a date is within the specified range. The following check is made when performing
 * the validation: <br />
 * from-date >= date-to-validate <= to-date
 *
 * @since 2.5
 */
public class DateRangeValidationMatcher implements ParameterizedValidationMatcher<DateRangeValidationMatcher.Parameters> {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(DateRangeValidationMatcher.class);

    @Override
    public void validate(String fieldName, String value, Parameters params, TestContext context) throws ValidationException {
        logger.debug("Validating date range for date '{}' using control data: from: {} to: {} format: {}", value,
                params.getDateFrom(), params.getDateTo(), params.getDateFormat());

        try {
            String dateFromParam = params.getDateFrom();
            String dateToParam = params.getDateTo();
            String dateFormat = params.getDateFormat();

            Calendar dateFrom = toCalender(dateFromParam, dateFormat);
            Calendar dateTo = toCalender(dateToParam, dateFormat);
            Calendar dateToValidate = toCalender(value, dateFormat);

            if (!checkInRange(dateFrom, dateTo, dateToValidate)) {
                String validationErr = String.format("%s failed for field '%s'. Date '%s' not in range: %s - %s",
                        this.getClass().getSimpleName(),
                        fieldName,
                        value,
                        dateFromParam,
                        dateToParam
                );
                throw new ValidationException(validationErr);
            }
        } catch (Exception e) {
            if (e instanceof ValidationException) {
                throw e;
            } else {
                String validationErr = String.format("%s failed for field '%s'",
                        this.getClass().getSimpleName(),
                        fieldName
                );
                throw new ValidationException(validationErr, e);
            }
        }
    }

    /**
     * Converts the supplied date to its calendar representation. The {@code datePattern} is
     * used for parsing the date.
     *
     * @param date        the date to parse
     * @param datePattern the date format to use when parsing the date
     * @return the calendar representation
     */
    protected Calendar toCalender(String date, String datePattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(dateFormat.parse(date));
        } catch (ParseException e) {
            throw new CitrusRuntimeException(String.format("Error parsing date '%s' using pattern '%s'", date, datePattern), e);
        }
        return cal;
    }

    private boolean checkInRange(Calendar dateFrom, Calendar dateTo, Calendar dateToCheck) {
        return checkGreaterOrEqualTo(dateFrom, dateToCheck)
                && checkGreaterOrEqualTo(dateToCheck, dateTo);
    }

    private boolean checkGreaterOrEqualTo(Calendar referenceDate, Calendar dateToCheck) {
        return referenceDate.compareTo(dateToCheck) <= 0;
    }

    @Override
    public Parameters getParameters() {
        return new Parameters();
    }

    public static class Parameters implements ParameterizedValidationMatcher.ControlParameters {
        private static final String FALLBACK_DATE_PATTERN = "yyyy-MM-dd";

        private String dateFrom;
        private String dateTo;
        private String dateFormat = FALLBACK_DATE_PATTERN;

        @Override
        public void configure(List<String> parameterList, TestContext context) {
            if (!parameterList.isEmpty()) {
                setDateFrom(parameterList.get(0));
            }

            if (parameterList.size() > 1) {
                setDateTo(parameterList.get(1));
            }

            if (parameterList.size() > 2) {
                setDateFormat(parameterList.get(2));
            }
        }

        public String getDateFrom() {
            return dateFrom;
        }

        @SchemaProperty(required = true, description = "The expected data range start value.")
        public void setDateFrom(String dateFrom) {
            this.dateFrom = dateFrom;
        }

        public String getDateTo() {
            return dateTo;
        }

        @SchemaProperty(required = true, description = "The expected data range end value.")
        public void setDateTo(String dateTo) {
            this.dateTo = dateTo;
        }

        public String getDateFormat() {
            return dateFormat;
        }

        @SchemaProperty(description = "The date format string.", defaultValue = FALLBACK_DATE_PATTERN)
        public void setDateFormat(String dateFormat) {
            this.dateFormat = dateFormat;
        }
    }
}

