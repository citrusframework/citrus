/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.kubernetes.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KubernetesRequest {

    /** The command */
    @JsonProperty("command")
    private String command;

    @JsonProperty("label")
    protected String label;
    @JsonProperty("name")
    protected String name;
    @JsonProperty("namespace")
    protected String namespace;

    /**
     * Gets the command property.
     * @return
     */
    public String getCommand() {
        return command;
    }

    /**
     * Sets the command property.
     * @param command
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * Gets the label property.
     * @return
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label property.
     * @param value
     */
    public void setLabel(String value) {
        this.label = value;
    }

    /**
     * Gets the namespace property.
     * @return
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Sets the namespace property.
     * @param value
     */
    public void setNamespace(String value) {
        this.namespace = value;
    }

    /**
     * Gets the name property.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name property.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
}
