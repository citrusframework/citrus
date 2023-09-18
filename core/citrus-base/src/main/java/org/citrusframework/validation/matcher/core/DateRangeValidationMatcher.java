/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.validation.matcher.core;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.validation.matcher.ValidationMatcher;
import org.citrusframework.validation.matcher.ValidationMatcherUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Validation matcher for verifying a date is within the specified range. The following check is made when performing
 * the validation: <br />
 * from-date >= date-to-validate <= to-date
 *
 * @author Martin Maher
 * @since 2.5
 */
public class DateRangeValidationMatcher implements ValidationMatcher {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(DateRangeValidationMatcher.class);

    private static final String FALLBACK_DATE_PATTERN = "yyyy-MM-dd";

    @Override
    public void validate(String fieldName, String value, List<String> params, TestContext context) throws ValidationException {
        logger.debug(String.format(
                "Validating date range for date '%s' using control data: %s",
                value,
                ValidationMatcherUtils.getParameterListAsString(params)));
        try {

            String dateFromParam = params.get(0);
            String dateToParam = params.get(1);
            String datePatternParam = FALLBACK_DATE_PATTERN;
            if (params.size() == 3) {
                datePatternParam = params.get(2);
            }

            Calendar dateFrom = toCalender(dateFromParam, datePatternParam);
            Calendar dateTo = toCalender(dateToParam, datePatternParam);
            Calendar dateToValidate = toCalender(value, datePatternParam);

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
     * Converts the supplied date to it's calendar representation. The {@code datePattern} is
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
}

