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

package com.consol.citrus.functions.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.functions.Function;

/**
 * Function returning the actual date as formatted string value. User specifies format string
 * as argument. Function also supports additional date offset in order to manipulate result date value. 
 * 
 * @author Christoph Deppisch
 */
public class CurrentDateFunction implements Function {

    /** Date formatter */
    private SimpleDateFormat dateFormat;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(CurrentDateFunction.class);

    /**
     * @see com.consol.citrus.functions.Function#execute(java.util.List)
     * @throws CitrusRuntimeException
     */
    public String execute(List<String> parameterList) {
        Calendar calendar = Calendar.getInstance();

        String result = "";

        if (parameterList != null && parameterList.size() > 0) {
            dateFormat = new SimpleDateFormat(parameterList.get(0));
        } else {
            dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        }

        if (parameterList != null && parameterList.size() > 1) {
            String offsetString = parameterList.get(1);
            calendar.add(Calendar.YEAR, getDateValueOffset(offsetString, 'y'));
            calendar.add(Calendar.MONTH, getDateValueOffset(offsetString, 'M'));
            calendar.add(Calendar.DAY_OF_YEAR, getDateValueOffset(offsetString, 'd'));
            calendar.add(Calendar.HOUR, getDateValueOffset(offsetString, 'h'));
            calendar.add(Calendar.MINUTE, getDateValueOffset(offsetString, 'm'));
            calendar.add(Calendar.SECOND, getDateValueOffset(offsetString, 's'));
        }

        try {
            result = dateFormat.format(calendar.getTime());
        } catch (RuntimeException e) {
            log.error("Error while formatting date value ", e);
            throw new CitrusRuntimeException(e);
        }

        return result;
    }

    /**
     * Parse offset string and add or subtract date offset value.
     * 
     * @param offsetString
     * @param c
     * @return
     */
    private int getDateValueOffset(String offsetString, char c) {
        int index = 0;
        ArrayList<Character> charList = new ArrayList<Character>();

        if ((index = offsetString.indexOf(c)) != -1) {
            for (int i = index-1; i >= 0; i--) {
                if (Character.isDigit(offsetString.charAt(i))) {
                    charList.add(0, offsetString.charAt(i));
                } else {

                    StringBuffer offsetValue = new StringBuffer();
                    offsetValue.append("0");
                    for (int j = 0; j < charList.size(); j++) {
                        offsetValue.append(charList.get(j));
                    }

                    if (offsetString.charAt(i) == '-') {
                        return new Integer("-" + offsetValue.toString()).intValue();
                    } else {
                        return new Integer(offsetValue.toString()).intValue();
                    }
                }
            }
        }

        return 0;
    }
}
