/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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
