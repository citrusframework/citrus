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

package org.citrusframework.kubernetes.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.client.KubernetesClientException;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommandResult<R extends KubernetesResource> {

    /** The result model */
    @JsonProperty("result")
    private R result;

    /** Optional error on this result */
    @JsonProperty("error")
    private KubernetesClientException error;

    /**
     * Default constructor.
     */
    public CommandResult() {
        super();
    }

    /**
     * Constructor using result model.
     * @param result
     */
    public CommandResult(R result) {
        this.result = result;
    }

    /**
     * Constructor using error.
     * @param error
     */
    public CommandResult(KubernetesClientException error) {
        this.error = error;
    }

    /**
     * Checks for existing error on this result.
     * @return
     */
    public boolean hasError() {
        return error != null;
    }

    /**
     * Gets the result model.
     * @return
     */
    public R getResult() {
        return result;
    }

    /**
     * Sets the result model.
     * @param result
     */
    public void setResult(R result) {
        this.result = result;
    }

    /**
     * Gets the error.
     * @return
     */
    public KubernetesClientException getError() {
        return error;
    }

    /**
     * Sets the error.
     * @param error
     */
    public void setError(KubernetesClientException error) {
        this.error = error;
    }
}
