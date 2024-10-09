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
 * Interface for classes being able to build control messages for validation.
 *
 */
@FunctionalInterface
public interface MessageBuilder {

    /**
     * Builds the control message.
     * @param context the current test context.
     * @param messageType the message type to build.
     * @return the constructed message object.
     */
    Message build(TestContext context, String messageType);
}
