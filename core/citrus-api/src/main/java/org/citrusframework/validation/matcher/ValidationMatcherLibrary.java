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

package org.citrusframework.validation.matcher;

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.exceptions.NoSuchValidationMatcherException;

/**
 * Library holding a set of validation matchers. Each library defines a validation prefix as namespace, so
 * there will be no naming conflicts when using multiple libraries at a time.
 *
 * @author Christian Wied
 */
public class ValidationMatcherLibrary {
    /** Map of validationMatchers in this library */
    private Map<String, ValidationMatcher> members = new HashMap<>();

    /** Name of validationMatcher library */
    private String name = "standard";

    /** validationMatcher library prefix */
    private String prefix = "";

    /**
     * Try to find validationMatcher in library by name.
     *
     * @param validationMatcherName validationMatcher name.
     * @return the validationMatcher instance.
     * @throws org.citrusframework.exceptions.NoSuchValidationMatcherException
     */
    public ValidationMatcher getValidationMatcher(String validationMatcherName) throws NoSuchValidationMatcherException {
        if (!members.containsKey(validationMatcherName)) {
            throw new NoSuchValidationMatcherException("Can not find validation matcher " + validationMatcherName + " in library " + name + " (" + prefix + ")");
        }

        return members.get(validationMatcherName);
    }

    /**
     * Does this library know a validationMatcher with the given name.
     *
     * @param validationMatcherName name to search for.
     * @return boolean flag to mark existence.
     */
    public boolean knowsValidationMatcher(String validationMatcherName) {
        // custom libraries:
        if (validationMatcherName.contains(":")) {
            String validationMatcherPrefix = validationMatcherName.substring(0, validationMatcherName.indexOf(':') + 1);

            if (!validationMatcherPrefix.equals(prefix)) {
                return false;
            }
            return members.containsKey(validationMatcherName.substring(validationMatcherName.indexOf(':') + 1, validationMatcherName.indexOf('(')));
        } else {
            // standard citrus-library without prefix:
            return members.containsKey(validationMatcherName.substring(0, validationMatcherName.indexOf('(')));
        }
    }

    /**
     * Set the validationMatcher library content.
     * @param members
     */
    public void setMembers(Map<String, ValidationMatcher> members) {
        this.members = members;
    }

    /**
     * Gets the matcher library members.
     * @return
     */
    public Map<String, ValidationMatcher> getMembers() {
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
     * Get the validationMatcher library name.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Get the name of the validationMatcher library.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
}
