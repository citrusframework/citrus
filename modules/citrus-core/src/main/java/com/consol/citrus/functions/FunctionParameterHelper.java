/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Helper class parsing a parameter string and converting the tokens to a parameter list.
 * 
 * @author Christoph Deppisch
 */
public class FunctionParameterHelper {
    
    /**
     * Prevent class instantiation.
     */
    private FunctionParameterHelper() {}
    
    /**
     * Convert a parameter string to a list of parameters.
     * 
     * @param parameterString comma separated parameter string.
     * @return list of parameters.
     */
    public static List<String> getParameterList(String parameterString) {
        List<String> parameterList = new ArrayList<String>();

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
