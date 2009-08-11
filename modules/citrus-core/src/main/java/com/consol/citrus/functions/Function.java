package com.consol.citrus.functions;

import java.util.List;

import com.consol.citrus.exceptions.CitrusRuntimeException;

public interface Function {

    /**
     * @param parameterList
     * @return
     */
    public String execute(List<String> parameterList) throws CitrusRuntimeException;
}
