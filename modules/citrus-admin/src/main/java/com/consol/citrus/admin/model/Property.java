/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.admin.model;

import java.util.Arrays;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class Property {

    private final String id;
    private final String fieldName;
    private final String displayName;
    private final String value;

    private String optionKey;
    private List<String> options;

    /**
     * Constructor using form field id, displayName and value.
     * @param id
     * @param fieldName
     * @param displayName
     * @param value
     */
    public Property(String id, String fieldName, String displayName, String value) {
        this.id = id;
        this.fieldName = fieldName;
        this.displayName = displayName;
        this.value = value;
    }

    /**
     * Adds option key to search for when collecting options at runtime.
     * @param key
     * @return
     */
    public Property optionKey(String key) {
        this.optionKey = key;
        return this;
    }

    /**
     * Adds options as variable arguments.
     * @param options
     * @return
     */
    public Property options(String ... options) {
        this.options = Arrays.asList(options);
        return this;
    }

    /**
     * Adds options.
     * @param options
     * @return
     */
    public Property options(List<String> options) {
        this.options = options;
        return this;
    }

    /**
     * Gets the field name on endpoint object model.
     * @return
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Gets the display name.
     * @return
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the parameter id.
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the parameter value.
     * @return
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the parameter options when using drop down list.
     * @return
     */
    public List<String> getOptions() {
        return options;
    }

    /**
     * Returns the option key used when searching for options.
     * @return
     */
    public String getOptionKey() {
        return optionKey;
    }
}
