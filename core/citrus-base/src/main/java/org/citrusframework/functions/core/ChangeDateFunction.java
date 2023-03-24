/**
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

package org.citrusframework.functions.core;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Function changes given date value by adding/subtracting day/month/year/hour/minute
 * offset values. Class uses special date format to parse date string to Calendar instance.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class ChangeDateFunction extends AbstractDateFunction {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(ChangeDateFunction.class);

    /**
     * @see org.citrusframework.functions.Function#execute(java.util.List, org.citrusframework.context.TestContext)
     * @throws CitrusRuntimeException
     */
    public String execute(List<String> parameterList, TestContext context) {
        if (CollectionUtils.isEmpty(parameterList)) {
            throw new InvalidFunctionUsageException("Function parameters must not be empty");
        }

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dateFormat;
        String result = "";

        if (parameterList.size() > 2) {
            dateFormat = new SimpleDateFormat(parameterList.get(2));
        } else {
            dateFormat = getDefaultDateFormat();
        }

        try {
            calendar.setTime(dateFormat.parse(parameterList.get(0)));
        } catch (ParseException e) {
            throw new CitrusRuntimeException(e);
        }

        if (parameterList.size() > 1) {
            applyDateOffset(calendar, parameterList.get(1));
        }

        try {
            result = dateFormat.format(calendar.getTime());
        } catch (RuntimeException e) {
            log.error("Error while formatting dateParameter value ", e);
            throw new CitrusRuntimeException(e);
        }

        return result;
    }

}
