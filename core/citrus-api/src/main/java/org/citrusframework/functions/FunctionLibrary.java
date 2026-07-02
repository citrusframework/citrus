/*
 * Copyright the original author or authors.
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

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.NoSuchFunctionException;

/**
 * Library holding a set of functions. Each library defines a function prefix as namespace, so
 * there will be no naming conflicts when using multiple libraries at a time.
 *
 */
public class FunctionLibrary {
    /** Default function prefix */
    static final String DEFAULT_PREFIX = "citrus:";

    /** Map of functions in this library */
    private Map<String, Function> members = new HashMap<>();

    /** Name of function library */
    private String name = "standard";

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
     * Adds new member in this library.
     */
    public void addMember(String name, Function function) {
        if (members.containsKey(name)) {
            throw new CitrusRuntimeException(String.format("Failed to add function. " +
                    "Duplicate function with name '%s' in library '%s'", name, getName()));
        }
        members.put(name, function);
    }

    /**
     * Set the function library content.
     */
    public void setMembers(Map<String, Function> members) {
        this.members = members;
    }

    /**
     * Gets the function library members.
     */
    public Map<String, Function> getMembers() {
        return members;
    }

    /**
     * Get the library prefix.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Set the library prefix.
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Get the function library name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the name of the function library.
     */
    public void setName(String name) {
        this.name = name;
    }
}
