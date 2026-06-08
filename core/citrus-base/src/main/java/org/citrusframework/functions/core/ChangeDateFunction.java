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

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.ParameterizedFunction;
import org.citrusframework.yaml.SchemaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.List;

import static org.citrusframework.util.StringUtils.hasText;

/**
 * Function changes given date value by adding/subtracting day/month/year/hour/minute offset values.
 * Class uses a date format to parse date string to an {@link OffsetDateTime}.
 *
 * @since 1.3.1
 */
public class ChangeDateFunction implements ParameterizedFunction<ChangeDateFunction.Parameters> {

    private static final Logger logger = LoggerFactory.getLogger(ChangeDateFunction.class);

    @Override
    public String execute(Parameters params, TestContext context) {
        DateTimeFormatter dateFormat = hasText(params.getDateFormat())
                ? DateTimeFormatter.ofPattern(params.getDateFormat())
                : DateTimeFormatter.ofPattern(DateFunctionHelper.getDefaultDateFormat().toPattern());

        OffsetDateTime date;

        try {
            date = parseDate(params.getValue(), dateFormat);
        } catch (RuntimeException e) {
            throw new CitrusRuntimeException(e);
        }

        if (hasText(params.getOffset())) {
            date = applyDateOffset(date, params.getOffset());
        }

        try {
            return date.format(dateFormat);
        } catch (RuntimeException e) {
            logger.error("Error while formatting dateParameter value ", e);
            throw new CitrusRuntimeException(e);
        }
    }

    @Override
    public Parameters getParameters() {
        return new Parameters();
    }

    private OffsetDateTime parseDate(String value, DateTimeFormatter formatter) {
        TemporalAccessor parsed = formatter.parseBest(value, OffsetDateTime::from, LocalDateTime::from, LocalDate::from);

        if (parsed instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime;
        }

        if (parsed instanceof LocalDateTime localDateTime) {
            ZoneId zone = ZoneId.systemDefault();
            return localDateTime.atZone(zone).toOffsetDateTime();
        }

        LocalDate localDate = (LocalDate) parsed;
        ZoneId zone = ZoneId.systemDefault();
        return localDate.atStartOfDay(zone).toOffsetDateTime();
    }

    private OffsetDateTime applyDateOffset(OffsetDateTime date, String offset) {
        return date
                .plusYears(DateFunctionHelper.getDateValueOffset(offset, 'y'))
                .plusMonths(DateFunctionHelper.getDateValueOffset(offset, 'M'))
                .plusDays(DateFunctionHelper.getDateValueOffset(offset, 'd'))
                .plusHours(DateFunctionHelper.getDateValueOffset(offset, 'h'))
                .plusMinutes(DateFunctionHelper.getDateValueOffset(offset, 'm'))
                .plusSeconds(DateFunctionHelper.getDateValueOffset(offset, 's'));
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
