package com.consol.citrus.functions.core;

import java.util.List;

import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.functions.Function;

public class AbsoluteFunction implements Function {

    public String execute(List parameterList) throws TestSuiteException {
        if (parameterList == null || parameterList.isEmpty()) {
            throw new InvalidFunctionUsageException("Function parameters must not be empty");
        }

        String param = (String)parameterList.get(0);
        
        if(param.contains(".")) {
            return Double.valueOf(Math.abs(Double.valueOf(param))).toString();
        } else {
            return Integer.valueOf(Math.abs(Integer.valueOf(param))).toString();
        }
    }

}
