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

package org.citrusframework.validation;

import java.util.Map;

import org.citrusframework.context.TestContext;

@FunctionalInterface
public interface GenericValidationProcessor<T> {

    /**
     * Subclasses do override this method for validation purpose.
     * @param payload the message payload object.
     * @param headers the message headers
     * @param context the current test context
     */
    void validate(T payload, Map<String, Object> headers, TestContext context);
}
