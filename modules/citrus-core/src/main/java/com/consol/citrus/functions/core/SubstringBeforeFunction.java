package com.consol.citrus.functions.core;

import java.util.List;

import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.functions.Function;

public class SubstringBeforeFunction implements Function {

    public String execute(List<String> parameterList) throws CitrusRuntimeException {
        if (parameterList == null || parameterList.size() < 2) {
            throw new InvalidFunctionUsageException("Function parameters not set correctly");
        }

        String resultString = parameterList.get(0);

        if (parameterList.size()>1) {
            String searchString = parameterList.get(1);
            resultString = resultString.substring(0, resultString.indexOf(searchString));
        }

        return resultString;
    }
}
