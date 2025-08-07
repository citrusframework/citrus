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

package org.citrusframework.kubernetes.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.citrusframework.actions.kubernetes.command.KubernetesCommandResult;

/**
 * @since 2.7
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommandResult<T> implements KubernetesCommandResult<T> {

    /** The result model */
    @JsonProperty("result")
    private T result;

    /** Optional error on this result */
    @JsonProperty("error")
    private RuntimeException error;

    /**
     * Default constructor.
     */
    public CommandResult() {
        super();
    }

    /**
     * Constructor using result model.
     */
    public CommandResult(T result) {
        this.result = result;
    }

    /**
     * Constructor using error.
     */
    public CommandResult(KubernetesClientException error) {
        this.error = error;
    }

    @Override
    public boolean hasError() {
        return error != null;
    }

    @Override
    public T getResult() {
        return result;
    }

    @Override
    public void setResult(T result) {
        this.result = result;
    }

    @Override
    public RuntimeException getError() {
        return error;
    }

    @Override
    public void setError(RuntimeException error) {
        this.error = error;
    }
}
