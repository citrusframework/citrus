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

package com.consol.citrus.functions.core;

import java.util.List;

import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.Function;

public class SubstringFunction implements Function {

    public String execute(List<String> parameterList) {
        if (parameterList == null || parameterList.isEmpty()) {
            throw new InvalidFunctionUsageException("Function parameters must not be empty");
        }

        String resultString = parameterList.get(0);

        String beginIndex = null;
        String endIndex = null;

        if (parameterList.size()>1) {
            beginIndex = parameterList.get(1);
        }

        if (parameterList.size()>2) {
            endIndex = parameterList.get(2);
        }

        if (endIndex != null && endIndex.length()>0) {
            resultString = resultString.substring(new Integer(beginIndex).intValue(), new Integer(endIndex).intValue());
        } else {
            resultString = resultString.substring(new Integer(beginIndex).intValue());
        }

        return resultString;
    }

}
