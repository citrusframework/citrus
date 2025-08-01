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

package org.citrusframework.actions.selenium;

import java.util.Map;

import org.citrusframework.TestAction;

public interface SeleniumFillFormActionBuilder<T extends TestAction, B extends SeleniumFillFormActionBuilder<T, B>>
        extends SeleniumActionBuilderBase<T, B> {

    B field(Object o, String value);

    B field(String id, String value);

    B fromJson(String formFieldsJson);

    B submit();

    B submit(String id);

    B submit(Object button);

    B fields(Map<String, String> fields);
}
