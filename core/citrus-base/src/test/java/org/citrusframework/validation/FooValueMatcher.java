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

import org.citrusframework.context.TestContext;

public class FooValueMatcher implements ValueMatcher {
    @Override
    public boolean supports(Class<?> controlType) {
        return FooValue.class.isAssignableFrom(controlType);
    }

    @Override
    public boolean validate(Object received, Object control, TestContext context) {
        return ((FooValue) received).value.equals(((FooValue) control).value);
    }

    static class FooValue {
        public final String value;

        FooValue(String value) {
            this.value = value;
        }
    }
}
