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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.ParameterizedFunction;
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Function changes given date value by adding/subtracting day/month/year/hour/minute
 * offset values. Class uses special date format to parse date string to Calendar instance.
 *
 * @since 1.3.1
 */
public class ChangeDateFunction implements ParameterizedFunction<ChangeDateFunction.Parameters> {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(ChangeDateFunction.class);

    private final CalendarProvider calendarProvider = new CalendarProvider();

    @Override
    public String execute(Parameters params, TestContext context) {
        Calendar calendar = calendarProvider.getInstance();

        SimpleDateFormat dateFormat;
        String result;

        if (StringUtils.hasText(params.getDateFormat())) {
            dateFormat = new SimpleDateFormat(params.getDateFormat());
        } else {
            dateFormat = DateFunctionHelper.getDefaultDateFormat();
        }

        try {
            calendar.setTime(dateFormat.parse(params.getValue()));
        } catch (ParseException e) {
            throw new CitrusRuntimeException(e);
        }

        if (StringUtils.hasText(params.getOffset())) {
            DateFunctionHelper.applyDateOffset(calendar, params.getOffset());
        }

        try {
            result = dateFormat.format(calendar.getTime());
        } catch (RuntimeException e) {
            logger.error("Error while formatting dateParameter value ", e);
            throw new CitrusRuntimeException(e);
        }

        return result;
    }

    @Override
    public Parameters getParameters() {
        return new Parameters();
    }

    static class CalendarProvider {

        private CalendarProvider () {
            // This class allows mocking in unit tests
        }

        Calendar getInstance() {
            return Calendar.getInstance();
        }
    }

    public static class Parameters implements FunctionParameters {
        private String value;
        private String offset;
        private String dateFormat;

        @Override
        public void configure(List<String> parameterList, TestContext context) {
            if (parameterList == null || parameterList.isEmpty()) {
                throw new InvalidFunctionUsageException("Function parameters must not be empty");
            }

            setValue(context.resolveDynamicValue(parameterList.get(0)));

            if (parameterList.size() > 1) {
                setOffset(parameterList.get(1));
            }

            if (parameterList.size() > 2) {
                setDateFormat(parameterList.get(2));
            }
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty(required = true, description = "The value to evaluate.")
        public void setValue(String value) {
            this.value = value;
        }

        public String getOffset() {
            return offset;
        }

        @SchemaProperty(description = "The date offset.")
        public void setOffset(String offset) {
            this.offset = offset;
        }

        public String getDateFormat() {
            return dateFormat;
        }

        @SchemaProperty(description = "The date format string.")
        public void setDateFormat(String dateFormat) {
            this.dateFormat = dateFormat;
        }
    }
}
