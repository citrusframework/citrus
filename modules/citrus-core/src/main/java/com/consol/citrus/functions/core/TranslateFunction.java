package com.consol.citrus.functions.core;

import java.util.List;

import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.functions.Function;

public class TranslateFunction implements Function {

    public String execute(List parameterList) throws TestSuiteException {
        if (parameterList == null || parameterList.size() < 3) {
            throw new InvalidFunctionUsageException("Function parameters not set correctly");
        }

        String resultString = (String)parameterList.get(0);

        String regex = null;
        String replacement = null;

        if (parameterList.size()>1) {
            regex = (String)parameterList.get(1);
        }

        if (parameterList.size()>2) {
            replacement = (String)parameterList.get(2);
        }

        if(regex != null && replacement != null) {
            resultString = resultString.replaceAll(regex, replacement);
        }

        return resultString;
    }
}
