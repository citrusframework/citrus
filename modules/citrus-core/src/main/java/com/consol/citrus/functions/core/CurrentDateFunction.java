package com.consol.citrus.functions.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.functions.Function;

public class CurrentDateFunction implements Function {

    SimpleDateFormat dateFormat;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(CurrentDateFunction.class);

    public String execute(List parameterList) throws TestSuiteException {
        Calendar calendar = Calendar.getInstance();

        String result = "";

        if (parameterList != null && parameterList.size() > 0) {
            dateFormat = new SimpleDateFormat((String)parameterList.get(0));
        } else {
            dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        }

        if (parameterList != null && parameterList.size() > 1) {
            String offsetString = (String)parameterList.get(1);
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
            throw new TestSuiteException(e);
        }

        return result;
    }

    private int getDateValueOffset(String offsetString, char c) {
        int index = 0;
        ArrayList charList = new ArrayList();

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
