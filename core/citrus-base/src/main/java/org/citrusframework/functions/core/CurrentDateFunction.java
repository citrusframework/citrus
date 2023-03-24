/*
 * Copyright 2006-2010 the original author or authors.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Function returning the actual date as formatted string value. User specifies format string
 * as argument. Function also supports additional date offset in order to manipulate result date value. 
 * 
 * @author Christoph Deppisch
 */
public class CurrentDateFunction extends AbstractDateFunction {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CurrentDateFunction.class);

    /**
     * @see org.citrusframework.functions.Function#execute(java.util.List, org.citrusframework.context.TestContext)
     * @throws CitrusRuntimeException
     */
    public String execute(List<String> parameterList, TestContext context) {
        Calendar calendar = Calendar.getInstance();
        
        SimpleDateFormat dateFormat;
        String result = "";
        
        if (!CollectionUtils.isEmpty(parameterList)) {
            dateFormat = new SimpleDateFormat(parameterList.get(0));
        } else {
            dateFormat = getDefaultDateFormat();
        }

        if (parameterList != null && parameterList.size() > 1) {
            applyDateOffset(calendar, parameterList.get(1));
        }

        try {
            result = dateFormat.format(calendar.getTime());
        } catch (RuntimeException e) {
            log.error("Error while formatting date value ", e);
            throw new CitrusRuntimeException(e);
        }

        return result;
    }
}
