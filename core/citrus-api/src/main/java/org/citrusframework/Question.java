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

package org.citrusframework;

import java.util.Objects;
import java.util.function.Function;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;
import org.citrusframework.util.BehaviorNames;
import org.citrusframework.util.StringUtils;

/**
 * Typed counterpart of {@link TestBehavior}: a named, reusable unit that runs against the test
 * and returns a typed Java value instead of writing a string variable.
 * <p>
 * A question is answered synchronously at the point of the call. It reads what the test already
 * has, or performs a synchronous exchange. Correlated async messaging stays the job of
 * receive-actions and message selectors.
 */
@FunctionalInterface
public interface Question<T> {

    /**
     * Answers the question against the given test action runner and test context.
     * @param runner the active test action runner.
     * @param context the active test context.
     * @return the typed answer, may be null.
     */
    T answeredBy(TestActionRunner runner, TestContext context);

    /**
     * Name of this question in test reports. Defaults to the de-camel-cased simple class name,
     * so {@code TheOrderId} is reported as {@code the order id}. Lambdas and anonymous classes
     * are reported as {@value BehaviorNames#GENERIC_NAME}; use {@link #about(String, Function)}
     * to give them a name.
     * @return the report name.
     */
    default String getName() {
        return BehaviorNames.of(getClass());
    }

    /**
     * Pure read over the test context - no actions executed. Mostly useful for lambdas,
     * which cannot name themselves.
     * @param name the report name.
     * @param read the read function.
     * @param <T> the answer type.
     * @return the question.
     */
    static <T> Question<T> about(String name, Function<TestContext, T> read) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Missing question name");
        }

        Objects.requireNonNull(read, "Missing question");

        return new Question<>() {
            @Override
            public T answeredBy(TestActionRunner runner, TestContext context) {
                return read.apply(context);
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    /**
     * Reads a test variable, so tests need no dealing with {@link TestContext} internals.
     * Reported as {@code variable <name>}.
     * @param name the variable name.
     * @param type the answer type the variable is converted to.
     * @param <T> the answer type.
     * @return the question.
     */
    static <T> Question<T> variable(String name, Class<T> type) {
        return about("variable " + name, context -> context.getVariable(name, type));
    }

    /**
     * Reads a stored message by name from the message store.
     * Reported as {@code message <name>}.
     * @param messageName the message name.
     * @return the question.
     */
    static Question<Message> message(String messageName) {
        return about("message " + messageName, context -> context.getMessageStore().getMessage(messageName));
    }
}
