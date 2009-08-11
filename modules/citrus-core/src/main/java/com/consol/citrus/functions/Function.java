package com.consol.citrus.functions;

import java.util.List;

public interface Function {

    /**
     * @param parameterList
     * @return
     */
    public String execute(List<String> parameterList);
}
