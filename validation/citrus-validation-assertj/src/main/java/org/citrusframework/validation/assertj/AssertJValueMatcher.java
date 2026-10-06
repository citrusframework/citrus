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
package org.citrusframework.validation.assertj;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.validation.ValueMatcher;

/**
 * Value matcher for expected values given as AssertJ {@link org.assertj.core.api.Condition} or {@link AssertJCheck}.
 * <p>
 * A failed check raises a {@link ValidationException} carrying the AssertJ failure description, instead of
 * returning {@code false}, so the description is not lost.
 */
public class AssertJValueMatcher implements ValueMatcher {

    @Override
    public boolean validate(Object received, Object control, TestContext context) {
        try {
            AssertJCheck.from(control).check(received, context);
            return true;
        } catch (AssertionError e) {
            throw new ValidationException("AssertJ validation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(Class<?> controlType) {
        return AssertJCheck.isSupported(controlType);
    }
}
