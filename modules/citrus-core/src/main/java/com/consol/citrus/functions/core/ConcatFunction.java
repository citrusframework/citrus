package com.consol.citrus.functions.core;

import java.util.List;

import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.functions.Function;

public class ConcatFunction implements Function {

    public String execute(List parameterList) throws TestSuiteException {
        if (parameterList == null || parameterList.isEmpty()) {
            throw new InvalidFunctionUsageException("Function parameters must not be empty");
        }

        StringBuffer resultString = new StringBuffer();

        for (int i = 0; i < parameterList.size(); i++) {
            resultString.append(parameterList.get(i));
        }

        return resultString.toString();
    }

}
