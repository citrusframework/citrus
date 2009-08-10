package com.consol.citrus.functions;

import java.util.List;

import com.consol.citrus.exceptions.TestSuiteException;

public interface Function {

    /**
     * @param parameterList
     * @return
     */
    public String execute(List<String> parameterList) throws TestSuiteException;
}
