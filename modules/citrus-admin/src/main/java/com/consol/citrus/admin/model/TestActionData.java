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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class TestActionData {

    private final String type;
    private final Class<?> modelType;
    private List<Property> properties = new ArrayList<Property>();

    /**
     * Constructor using action type field and identifier.
     * @param type
     */
    public TestActionData(String type, Class<?> modelType) {
        this.modelType = modelType;
        this.type = type;
    }

    /**
     * Adds a new configuration property as key value pair.
     * @param property
     * @return
     */
    public TestActionData add(Property property) {
        if (property != null) {
            properties.add(property);
        }
        return this;
    }

    /**
     * Gets the action type such as http, jms, camel, etc.
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the model type that is logically linked to this action data.
     * @return
     */
    public Class<?> getModelType() {
        return modelType;
    }

    /**
     * Gets the key value action properties.
     * @return
     */
    public List<Property> getProperties() {
        return properties;
    }

    /**
     * Sets the action properties as key value properties.
     * @param properties
     */
    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

}
