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

import org.testng.Assert;
import org.testng.annotations.Test;

public class IsYamlPredicateTest {

    @Test
    public void shouldIdentifyYamlContent() {
        Assert.assertTrue(IsYamlPredicate.getInstance().test(""));
        Assert.assertTrue(IsYamlPredicate.getInstance().test("- route:"));
        Assert.assertTrue(IsYamlPredicate.getInstance().test("- route: {}"));
        Assert.assertTrue(IsYamlPredicate.getInstance().test("route:"));
        Assert.assertTrue(IsYamlPredicate.getInstance().test("route:\n"));
        Assert.assertTrue(IsYamlPredicate.getInstance().test("route: {}"));
        Assert.assertTrue(IsYamlPredicate.getInstance().test("route: {}\n"));
        Assert.assertFalse(IsYamlPredicate.getInstance().test("foo"));
    }

}
