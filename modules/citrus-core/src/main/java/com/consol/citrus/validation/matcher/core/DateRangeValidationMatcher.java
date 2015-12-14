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

package com.consol.citrus.validation.matcher.core;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.validation.matcher.ValidationMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static Logger log = LoggerFactory.getLogger(DateRangeValidationMatcher.class);

    static final String FALLBACK_DATE_PATTERN = "yyyy-MM-dd";


    @Override
    public void validate(String fieldName, String value, String control, TestContext context) throws ValidationException {

        String controlExpression = fixControlExpressionPrefixAndSuffix(control);

        log.debug(String.format("Validating date range for value '%s' using control data: '%s'", value, controlExpression));
        try {
            String[] controlData = extractControlData(controlExpression);
            Calendar dateFrom = toCalender(controlData[0], controlData[2]);
            Calendar dateTo = toCalender(controlData[1], controlData[2]);
            Calendar dateToValidate = toCalender(value, controlData[2]);

            if (!checkInRange(dateFrom, dateTo, dateToValidate)) {
                String validationErr = String.format("%s failed for field '%s'. Date '%s' not in range: %s - %s",
                        this.getClass().getSimpleName(),
                        fieldName,
                        value,
                        controlData[0],
                        controlData[1]
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
     * Parses the control data returning the individual control data items. The control data
     * should be supplied in the format: ('date-from','date-to')
     * or ('date-from','date-to','date-format').
     * If no date-format is supplied then the {@link #FALLBACK_DATE_PATTERN} is returned in
     * its place.
     *
     * @param control the control string
     * @return the individual control data items - {date-from, date-to, date-format}
     */
    protected String[] extractControlData(String control) {
        String[] controlData = new String[3];
        Pattern pattern = Pattern.compile("'([^']+)'\\s*,\\s*'([^']+)'(?:\\s*,\\s*'(.+)'\\s*)?");
        Matcher matcher = pattern.matcher(control);
        if (matcher.find()) {
            controlData[0] = StringUtils.trimWhitespace(matcher.group(1));
            controlData[1] = StringUtils.trimWhitespace(matcher.group(2));
            controlData[2] = StringUtils.trimWhitespace(matcher.group(3));
            if (controlData[2] == null) {
                controlData[2] = FALLBACK_DATE_PATTERN;
            }
            log.debug(String.format("Using the following control data items: %s", controlData));
        }

        for (String data : controlData) {
            if (StringUtils.isEmpty(data)) {
                throw new CitrusRuntimeException("Invalid configuration of DateRange validator. Was expecting: 'dateFrom','dateTo' and optionally 'datePattern' but got " + control);
            }
        }
        return controlData;
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

    private static String fixControlExpressionPrefixAndSuffix(String controlExpression) {
        StringBuilder ceBuilder = new StringBuilder(controlExpression);

        if(!controlExpression.startsWith("'")) {
            ceBuilder.insert(0,"'");
        }

        if(!controlExpression.endsWith("'")) {
            ceBuilder.append("'");
        }

        return ceBuilder.toString();
    }
}

