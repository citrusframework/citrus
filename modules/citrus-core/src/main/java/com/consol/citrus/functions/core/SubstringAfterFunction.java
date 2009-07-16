package com.consol.citrus.functions.core;

import java.util.List;

import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.functions.Function;

public class SubstringAfterFunction implements Function {

    public String execute(List parameterList) throws TestSuiteException {
        if (parameterList == null || parameterList.size() < 2) {
            throw new InvalidFunctionUsageException("Function parameters not set correctly");
        }

        String resultString = (String)parameterList.get(0);

        if (parameterList.size()>1) {
            String searchString = (String)parameterList.get(1);
            resultString = resultString.substring(resultString.indexOf(searchString)+1);
        }

        return resultString;
    }
}
