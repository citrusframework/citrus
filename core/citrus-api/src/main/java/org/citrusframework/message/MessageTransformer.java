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

package org.citrusframework.message;

import org.citrusframework.context.TestContext;

/**
 * Transformer is able to completely change a given message.
 */
@FunctionalInterface
public interface MessageTransformer {

    /**
     * Transform message with given test context and return new message.
     * @param message the message to process.
     * @param context the current test context.
     * @return the processed message.
     */
    Message transform(Message message, TestContext context);

    /**
     * Fluent builder
     * @param <T> transformer type
     * @param <B> builder reference to self
     */
    interface Builder<T extends MessageTransformer, B extends Builder<T, B>> {

        /**
         * Builds new message processor instance.
         * @return the built processor.
         */
        T build();
    }
}
