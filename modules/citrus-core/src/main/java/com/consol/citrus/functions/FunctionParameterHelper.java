/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class FunctionParameterHelper {
    private FunctionParameterHelper() {}
    
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
