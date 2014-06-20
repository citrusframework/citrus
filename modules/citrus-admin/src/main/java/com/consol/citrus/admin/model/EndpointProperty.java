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

import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class EndpointProperty {

    private final String id;
    private final String displayName;
    private final String value;

    private List<String> options;

    /**
     * Constructor using form field id and value.
     * @param id
     * @param value
     */
    public EndpointProperty(String id, String value) {
        this(id, id, value);
    }

    /**
     * Constructor using form field id, displayName and value.
     * @param id
     * @param displayName
     * @param value
     */
    public EndpointProperty(String id, String displayName, String value) {
        this.id = id;
        this.displayName = displayName;
        this.value = value;
    }

    /**
     * Constructor using additional field options.
     * @param id
     * @param displayName
     * @param value
     * @param options
     */
    public EndpointProperty(String id, String displayName, String value, List<String> options) {
        this(id, displayName, value);
        this.options = options;
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

}
