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

package com.consol.citrus.functions;

import java.util.HashMap;
import java.util.Map;

import com.consol.citrus.exceptions.NoSuchFunctionException;

/**
 * Library holding a set of functions. Each library defines a function prefix as namespace, so
 * there will be no naming conflicts when using multiple libraries at a time.
 * 
 * @author Christoph Deppisch
 */
public class FunctionLibrary {
    /** Map of functions in this library */
    private Map<String, Function> members = new HashMap<String, Function>();

    /** Default function prefix */
    private static final String DEFAULT_PREFIX = "citrus:";

    /** Name of function library */
    private String name = DEFAULT_PREFIX;

    /** Function library prefix */
    private String prefix = DEFAULT_PREFIX;

    /**
     * Try to find function in library by name.
     * 
     * @param functionName function name.
     * @return the function instance.
     * @throws NoSuchFunctionException
     */
    public Function getFunction(String functionName) throws NoSuchFunctionException {
        if (!members.containsKey(functionName)) {
            throw new NoSuchFunctionException("Can not find function " + functionName + " in library " + name + " (" + prefix + ")");
        }

        return members.get(functionName);
    }

    /**
     * Does this function library know a function with the given name.
     * 
     * @param functionName name to search for.
     * @return boolean flag to mark existence.
     */
    public boolean knowsFunction(String functionName) {
        String functionPrefix = functionName.substring(0, functionName.indexOf(':') + 1);

        if (!functionPrefix.equals(prefix)) {
            return false;
        }

        if (members.containsKey(functionName.substring(functionName.indexOf(':') + 1, functionName.indexOf('(')))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Set the function library content.
     * @param members
     */
    public void setMembers(Map<String, Function> members) {
        this.members = members;
    }

    /**
     * Get the library prefix.
     * @return
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Set the library prefix.
     * @param prefix
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Get the function library name.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Get the name of the function library.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
}
