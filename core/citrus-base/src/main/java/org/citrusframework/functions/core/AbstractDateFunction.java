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

import org.citrusframework.functions.Function;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Abstract date value handling function provides base date value manipulation helpers.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public abstract class AbstractDateFunction implements Function {

    /**
     * Adds/removes date value offset by parsing offset string for
     * year/month/day/hour/minute/second offsets.
     *
     * @param calendar
     * @param offsetString
     */
    protected void applyDateOffset(Calendar calendar, String offsetString) {
        calendar.add(Calendar.YEAR, getDateValueOffset(offsetString, 'y'));
        calendar.add(Calendar.MONTH, getDateValueOffset(offsetString, 'M'));
        calendar.add(Calendar.DAY_OF_YEAR, getDateValueOffset(offsetString, 'd'));
        calendar.add(Calendar.HOUR, getDateValueOffset(offsetString, 'h'));
        calendar.add(Calendar.MINUTE, getDateValueOffset(offsetString, 'm'));
        calendar.add(Calendar.SECOND, getDateValueOffset(offsetString, 's'));
    }

    /**
     * Parse offset string and add or subtract date offset value.
     *
     * @param offsetString
     * @param c
     * @return
     */
    protected int getDateValueOffset(String offsetString, char c) {
        ArrayList<Character> charList = new ArrayList<Character>();

        int index = offsetString.indexOf(c);
        if (index != -1) {
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
                        return Integer.valueOf("-" + offsetValue.toString());
                    } else {
                        return Integer.valueOf(offsetValue.toString());
                    }
                }
            }
        }

        return 0;
    }

    /**
     * Provides default date format.
     * @return
     */
    protected SimpleDateFormat getDefaultDateFormat() {
        return new SimpleDateFormat("dd.MM.yyyy");
    }

}
