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
import io.fabric8.kubernetes.api.model.KubernetesResource;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KubernetesResponse {

    /** The command */
    @JsonProperty("command")
    private String command;

    /** The result model */
    @JsonProperty("result")
    private KubernetesResource<?> result;

    /** Optional error on this result */
    @JsonProperty("error")
    private String error;

    /** Optional action of watch response */
    @JsonProperty("action")
    protected String action;

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
     * Gets the error property.
     * @return
     */
    public String getError() {
        return error;
    }

    /**
     * Sets the error property.
     * @param value
     */
    public void setError(String value) {
        this.error = value;
    }

    /**
     * Gets the result property.
     * @return
     */
    public KubernetesResource<?> getResult() {
        return result;
    }

    /**
     * Sets the result property.
     * @param value
     */
    public void setResult(KubernetesResource<?> value) {
        this.result = value;
    }

    /**
     * Gets the action property.
     * @return
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the action property.
     * @param value
     */
    public void setAction(String value) {
        this.action = value;
    }
}
