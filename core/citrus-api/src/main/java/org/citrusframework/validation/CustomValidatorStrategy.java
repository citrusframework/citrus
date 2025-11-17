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

/**
 * Defines how custom validators are applied during message validation.
 *
 * <p><strong>Historical behavior:</strong><br>
 * By default, custom validators were executed exclusively.
 * This meant that when a custom validator was provided, the default validation logic (such as body or JSON validation) was skipped entirely.
 * For example, in the snippet below the body validation would have no effect on the test result:
 *
 * <pre>
 * ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
 *         .getMessageBuilderSupport()
 *         .body("expected body")
 *         .validate((message, context) -> customValidatorInvoked.set(true))
 *         .build();
 * </pre>
 *
 * <p>
 * This behavior originally existed to allow users to fully replace the default validation logic — for example, by providing custom JSON validation that must not be duplicated or interfered with by the built-in validators.
 *
 * <p><strong>New behavior option:</strong><br>
 * In some scenarios this exclusivity can be unexpected or misleading.
 * For these cases the {@link CustomValidatorStrategy#COMBINED} strategy allows Citrus to run both the default validators and the custom validator logic together.
 */
public enum CustomValidatorStrategy {

    COMBINED,
    EXCLUSIVE;
}
