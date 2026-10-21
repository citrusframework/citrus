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

package org.citrusframework.util;

import java.util.function.Predicate;

import org.citrusframework.CitrusSettings;

/**
 * Tests if a string represents a path expression that references a dynamic variable
 * (e.g. <code>${variableName}</code>). Such an expression can only be classified as
 * JSONPath or XPath once the variable has been resolved at runtime.
 */
public class IsDynamicPathExpressionPredicate implements Predicate<String> {

    private static final IsDynamicPathExpressionPredicate INSTANCE = new IsDynamicPathExpressionPredicate();

    private IsDynamicPathExpressionPredicate() {
        // Singleton
    }

    public static IsDynamicPathExpressionPredicate getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean test(String toTest) {
        return StringUtils.hasText(toTest) && toTest.contains(CitrusSettings.VARIABLE_PREFIX);
    }
}
