/*
 * Copyright 2006-2013 the original author or authors.
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

import java.util.*;

/**
 * Basic endpoint value object holds name, type and all endpoint configuration properties.
 *
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class EndpointData {

    private String id;
    private String type;
    private List<EndpointProperty> properties = new ArrayList<EndpointProperty>();

    /**
     * Default constructor.
     */
    public EndpointData() {
    }

    /**
     * Constructor using endpoint type field and identifier.
     * @param type
     * @param id
     */
    public EndpointData(String type, String id) {
        this.id = id;
        this.type = type;
    }

    /**
     * Adds a new configuration property as key value pair.
     * @param endpointProperty
     * @return
     */
    public EndpointData add(EndpointProperty endpointProperty) {
        properties.add(endpointProperty);
        return this;
    }

    /**
     * Gets the identifier usually the Spring bean name.
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the identifier usually the Spring bean name.
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the endpoint type such as http, jms, camel, etc.
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the key value endpoint properties.
     * @return
     */
    public List<EndpointProperty> getProperties() {
        return properties;
    }

    /**
     * Sets the endpoint properties as key value properties.
     * @param properties
     */
    public void setProperties(List<EndpointProperty> properties) {
        this.properties = properties;
    }
}
