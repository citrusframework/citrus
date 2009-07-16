package com.consol.citrus.functions;

import java.util.HashMap;
import java.util.Map;

import com.consol.citrus.exceptions.NoSuchFunctionException;

public class FunctionLibrary {
    private Map members = new HashMap();

    private static final String DEFAULT_PREFIX = "citrus:";

    private String name = DEFAULT_PREFIX;

    private String prefix = DEFAULT_PREFIX;

    public Function getFunction(String functionName) throws NoSuchFunctionException {
        if (!members.containsKey(functionName)) {
            throw new NoSuchFunctionException("Can not find function " + functionName + " in library " + name + " (" + prefix + ")");
        }

        return (Function)members.get(functionName);
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

    public void setMembers(Map members) {
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
