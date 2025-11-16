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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.functions.ParameterizedFunction;
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Function returning the actual date as formatted string value. User specifies format string
 * as argument. Function also supports additional date offset in order to manipulate result date value.
 *
 */
public class CurrentDateFunction implements ParameterizedFunction<CurrentDateFunction.Parameters> {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CurrentDateFunction.class);

    @Override
    public String execute(Parameters params, TestContext context) {
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dateFormat;
        String result;
        if (StringUtils.hasText(params.getDateFormat())) {
            dateFormat = new SimpleDateFormat(params.getDateFormat());
        } else {
            dateFormat = DateFunctionHelper.getDefaultDateFormat();
        }

        if (StringUtils.hasText(params.getOffset())) {
            DateFunctionHelper.applyDateOffset(calendar, params.getOffset());
        }

        if (StringUtils.hasText(params.getTimeZone())) {
            dateFormat.setTimeZone(TimeZone.getTimeZone(params.getTimeZone()));
        }

        try {
            result = dateFormat.format(calendar.getTime());
        } catch (RuntimeException e) {
            logger.error("Error while formatting date value ", e);
            throw new CitrusRuntimeException(e);
        }

        return result;
    }

    @Override
    public Parameters getParameters() {
        return new Parameters();
    }

    public static class Parameters implements FunctionParameters {
        private String dateFormat;
        private String offset;
        private String timeZone;

        @Override
        public void configure(List<String> parameterList, TestContext context) {
            if (!parameterList.isEmpty()) {
                setDateFormat(parameterList.get(0));
            }

            if (parameterList.size() > 1) {
                setOffset(parameterList.get(1));
            }

            if (parameterList.size() > 2) {
                setTimeZone(parameterList.get(2));
            }
        }

        public String getDateFormat() {
            return dateFormat;
        }

        @SchemaProperty(description = "The date format string.")
        public void setDateFormat(String dateFormat) {
            this.dateFormat = dateFormat;
        }

        public String getOffset() {
            return offset;
        }

        @SchemaProperty(description = "The date offset.")
        public void setOffset(String offset) {
            this.offset = offset;
        }

        public String getTimeZone() {
            return timeZone;
        }

        @SchemaProperty(description = "The time zone.")
        public void setTimeZone(String timeZone) {
            this.timeZone = timeZone;
        }
    }
}
