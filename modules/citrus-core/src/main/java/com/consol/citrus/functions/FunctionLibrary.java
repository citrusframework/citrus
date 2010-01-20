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

import java.util.HashMap;
import java.util.Map;

import com.consol.citrus.exceptions.NoSuchFunctionException;

public class FunctionLibrary {
    private Map<String, Function> members = new HashMap<String, Function>();

    private static final String DEFAULT_PREFIX = "citrus:";

    private String name = DEFAULT_PREFIX;

    private String prefix = DEFAULT_PREFIX;

    public Function getFunction(String functionName) throws NoSuchFunctionException {
        if (!members.containsKey(functionName)) {
            throw new NoSuchFunctionException("Can not find function " + functionName + " in library " + name + " (" + prefix + ")");
        }

        return members.get(functionName);
    }

    public boolean knowsFunction(String functionName) {
        String functionPrefix = functionName.substring(0, functionName.indexOf(':') + 1);

        if (functionPrefix.equals(prefix) == false) {
            return false;
        }

        if (members.containsKey(functionName.substring(functionName.indexOf(':') + 1, functionName.indexOf('(')))) {
            return true;
        } else {
            return false;
        }
    }

    public void setMembers(Map<String, Function> members) {
        this.members = members;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
