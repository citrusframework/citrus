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
import java.util.Random;

import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.Function;

/**
 * Function returning a random numeric value. Argument specifies the number of digits and
 * padding boolean flag.
 * 
 * @author Christoph Deppisch
 */
public class RandomNumberFunction implements Function {
    private static Random generator = new Random(System.currentTimeMillis());

    /**
     * @see com.consol.citrus.functions.Function#execute(java.util.List)
     * @throws InvalidFunctionUsageException
     */
    public String execute(List<String> parameterList) {
        int numberLength;
        boolean paddingOn = true;

        if (parameterList == null || parameterList.isEmpty()) {
            throw new InvalidFunctionUsageException("Function parameters must not be empty");
        }

        if (parameterList.size() > 2) {
            throw new InvalidFunctionUsageException("Too many parameters for function");
        }

        numberLength = new Integer(parameterList.get(0)).intValue();
        if (numberLength < 0) {
            throw new InvalidFunctionUsageException("Invalid parameter definition. Number of letters must not be positive non-zero integer value");
        }

        if (parameterList.size() > 1) {
            paddingOn = Boolean.valueOf(parameterList.get(1));
        }

        return getRandomNumber(numberLength, paddingOn);
    }

    public static String getRandomNumber(int numberLength, boolean paddingOn) {
        if (numberLength < 1) {
            throw new InvalidFunctionUsageException("numberLength must be greater than 0 - supplied " + numberLength);
        }

        StringBuffer sBuf = new StringBuffer();
        for (int i = 0; i < numberLength; i++) {
            sBuf.append(generator.nextInt(10));
        }

        if (!paddingOn) {
            removePadding(sBuf);
        }

        return sBuf.toString();
    }

    private static void removePadding(StringBuffer sBuf) {
        for (int i = 0; i < sBuf.length(); i++) {
            if (sBuf.charAt(i) == '0') {
                continue;
            } else {
                if (i > 0) {
                    sBuf.delete(0, i);
                    // very unlikely, ensures that empty string is not returned
                    if (sBuf.length() < 1) {
                        sBuf.append('0');
                    }
                }
                break;
            }
        }
    }
}
