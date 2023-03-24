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

import org.citrusframework.exceptions.NoSuchFunctionException;

import java.util.HashMap;
import java.util.Map;

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

        return members.containsKey(functionName.substring(functionName.indexOf(':') + 1, functionName.indexOf('(')));
    }

    /**
     * Set the function library content.
     * @param members
     */
    public void setMembers(Map<String, Function> members) {
        this.members = members;
    }

    /**
     * Gets the function library members.
     * @return
     */
    public Map<String, Function> getMembers() {
        return members;
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
