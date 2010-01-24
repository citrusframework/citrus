/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.functions.core;

import java.util.List;

import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.Function;

/**
 * Function searches for occurrences of a given character sequence and replaces all
 * findings with given replacement string.
 * 
 * @author Christoph Deppisch
 */
public class TranslateFunction implements Function {

    /**
     * @see com.consol.citrus.functions.Function#execute(java.util.List)
     * @throws InvalidFunctionUsageException
     */
    public String execute(List<String> parameterList) {
        if (parameterList == null || parameterList.size() < 3) {
            throw new InvalidFunctionUsageException("Function parameters not set correctly");
        }

        String resultString = parameterList.get(0);

        String regex = null;
        String replacement = null;

        if (parameterList.size()>1) {
            regex = parameterList.get(1);
        }

        if (parameterList.size()>2) {
            replacement = parameterList.get(2);
        }

        if(regex != null && replacement != null) {
            resultString = resultString.replaceAll(regex, replacement);
        }

        return resultString;
    }
}
