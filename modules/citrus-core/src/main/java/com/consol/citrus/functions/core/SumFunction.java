package com.consol.citrus.functions.core;

import java.util.Iterator;
import java.util.List;

import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.Function;

public class SumFunction implements Function {

    public String execute(List<String> parameterList) {
        if (parameterList == null || parameterList.isEmpty()) {
            throw new InvalidFunctionUsageException("Function parameters must not be empty");
        }

        double result = 0.0;

        for (Iterator iterator = parameterList.iterator(); iterator.hasNext();) {
            String token = (String) iterator.next();
            result += Double.valueOf(token);
        }

        return Double.valueOf(result).toString();
    }

}
