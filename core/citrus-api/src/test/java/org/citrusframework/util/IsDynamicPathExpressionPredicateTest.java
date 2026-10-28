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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IsDynamicPathExpressionPredicateTest {

    @Test
    public void shouldIdentifyDynamicPathExpression() {
        Assertions.assertTrue(IsDynamicPathExpressionPredicate.getInstance().test("${VERSION_PATH}"));
        Assertions.assertTrue(IsDynamicPathExpressionPredicate.getInstance().test("boolean:${VERSION_PATH}"));
        Assertions.assertTrue(IsDynamicPathExpressionPredicate.getInstance().test("/root/version/${ATTRIBUTE}"));
        Assertions.assertTrue(IsDynamicPathExpressionPredicate.getInstance().test("${VERSION_PATH} "));
    }

    @Test
    public void shouldNotIdentifyStaticPathExpression() {
        Assertions.assertFalse(IsDynamicPathExpressionPredicate.getInstance().test(null));
        Assertions.assertFalse(IsDynamicPathExpressionPredicate.getInstance().test(""));
        Assertions.assertFalse(IsDynamicPathExpressionPredicate.getInstance().test("  "));
        Assertions.assertFalse(IsDynamicPathExpressionPredicate.getInstance().test("/root/version/@number"));
        Assertions.assertFalse(IsDynamicPathExpressionPredicate.getInstance().test("$.version.number"));
        Assertions.assertFalse(IsDynamicPathExpressionPredicate.getInstance().test("boolean:/root/version/@number"));
    }
}
