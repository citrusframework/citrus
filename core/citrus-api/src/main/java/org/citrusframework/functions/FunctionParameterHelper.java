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

package org.citrusframework.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Helper class parsing a parameter string and converting the tokens to a parameter list.
 * 
 * @author Christoph Deppisch
 */
public final class FunctionParameterHelper {
    
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
        List<String> parameterList = new ArrayList<>();

        StringTokenizer tok = new StringTokenizer(parameterString, ",");
        while (tok.hasMoreElements()) {
            String param = tok.nextToken().trim();
            parameterList.add(cutOffSingleQuotes(param));
        }

        List<String> postProcessed = new ArrayList<>();
        for (int i = 0; i < parameterList.size(); i++) {
            int next = i + 1;

            String processed = parameterList.get(i);

            if (processed.startsWith("'") && !processed.endsWith("'")) {
                while (next < parameterList.size()) {
                    if (parameterString.contains(processed + ", " + parameterList.get(next))) {
                        processed += ", " + parameterList.get(next);
                    } else if (parameterString.contains(processed + "," + parameterList.get(next))) {
                        processed += "," + parameterList.get(next);
                    } else if (parameterString.contains(processed + " , " + parameterList.get(next))) {
                        processed += " , " + parameterList.get(next);
                    } else {
                        processed += parameterList.get(next);
                    }

                    i++;
                    if (parameterList.get(next).endsWith("'")) {
                        break;
                    } else {
                        next++;
                    }
                }

            }

            postProcessed.add(cutOffSingleQuotes(processed));
        }

        return postProcessed;
    }

    private static String cutOffSingleQuotes(String param) {
        if (param.equals("'")) {
            return "";
        }

        if (param.length() > 1 && param.charAt(0) == '\'' && param.charAt(param.length()-1) == '\'') {
            return param.substring(1, param.length()-1);
        }

        return param;
    }
}
