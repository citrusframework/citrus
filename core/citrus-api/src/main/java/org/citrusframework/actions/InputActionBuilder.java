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

package org.citrusframework.actions;

import java.io.BufferedReader;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;

public interface InputActionBuilder<T extends TestAction>
        extends ActionBuilder<T, InputActionBuilder<T>>, TestActionBuilder<T> {

    /**
     * Sets the message displayed to the user.
     * @param message the message to set
     */
    InputActionBuilder<T> message(String message);

    /**
     * Stores the result to a test variable.
     * @param variable the variable to set
     */
    InputActionBuilder<T> result(String variable);

    /**
     * Sets the input reader.
     * @param reader the input reader to set
     */
    InputActionBuilder<T> reader(BufferedReader reader);

    /**
     * Sets the valid answers.
     * @param answers the validAnswers to set
     */
    InputActionBuilder<T> answers(String... answers);

    interface BuilderFactory {

        /**
         * Fluent API action building entry method used in Java DSL.
         */
        InputActionBuilder<?> input();

        default InputActionBuilder<?> input(String message) {
            return input().message(message);
        }
    }

}
