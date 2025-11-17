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

package org.citrusframework.validation.matcher.parameter;

import java.util.List;
import java.util.Optional;

import org.citrusframework.context.TestContext;

/**
 * Validation matcher parameters allows a single string argument.
 */
public class OptionalStringParameter extends StringParameter {

    @Override
    public void configure(List<String> parameterList, TestContext context) {
        if (parameterList == null || parameterList.isEmpty()) {
            return;
        }

        super.configure(parameterList, context);
    }

    @Override
    public String getValue() {
        return Optional.ofNullable(super.getValue()).orElse("");
    }

    public boolean isPresent() {
        return !getValue().isEmpty();
    }
}
