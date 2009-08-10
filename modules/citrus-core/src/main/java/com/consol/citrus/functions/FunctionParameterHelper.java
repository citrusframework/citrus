package com.consol.citrus.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class FunctionParameterHelper {
    public static List<String> getParameterList(String parameterString) {
        List parameterList = new ArrayList();

        StringTokenizer tok = new StringTokenizer(parameterString, ",");
        while (tok.hasMoreElements()) {
            String param = tok.nextToken().trim();

            if (param.charAt(0) == '\'' && param.charAt(param.length()-1) == '\'') {
                param = param.substring(1, param.length()-1);
            }

            parameterList.add(param);
        }

        return parameterList;
    }
}
