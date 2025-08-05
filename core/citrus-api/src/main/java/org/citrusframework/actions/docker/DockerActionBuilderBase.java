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

package org.citrusframework.actions.docker;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.docker.command.CommandResultCallback;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.ValidationContext;

public interface DockerActionBuilderBase<R, T extends TestAction, B extends DockerActionBuilderBase<R, T, B>>
        extends TestActionBuilder<T> {

    B client(Object dockerClient);

    B mapper(Object mapper);

    B validator(MessageValidator<? extends ValidationContext> validator);

    B result(String result);

    /**
     * Adds command parameter to current command.
     */
    B withParam(String name, String value);

    /**
     * Adds validation callback with command result.
     */
    B validateCommandResult(CommandResultCallback<R> callback);

    @SuppressWarnings("unchecked")
    default B validateCommandResult(Object callback) {
        try {
            return validateCommandResult((CommandResultCallback<R>) callback);
        } catch (ClassCastException e) {
            throw new CitrusRuntimeException("Invalid command result callback type: " + callback.getClass().getName(), e);
        }
    }

}
