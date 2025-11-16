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

package org.citrusframework.functions.core;

import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class RandomPatternFunctionTest {

    private final RandomPatternFunction function = new RandomPatternFunction();
    private final TestContext context = new TestContext();

    @Test(expectedExceptions = InvalidFunctionUsageException.class)
    public void testExecuteWithNullParameterList() {
        function.execute((List<String>) null, context);
    }

    @Test(expectedExceptions = InvalidFunctionUsageException.class)
    public void testExecuteWithEmptyPattern() {
        function.execute(List.of(""), context);
    }

    @Test
    public void testExecuteWithValidPattern() {
        String pattern = "[a-zA-Z0-9]{10}";
        String result = function.execute(List.of(pattern), context);
        assertTrue(result.matches(pattern), "Generated string does not match the pattern");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testExecuteWithInvalidPattern() {
        String pattern = "[0-3]([a-c]|[e-g]{1"; // Invalid regex pattern with "Character range is out of order"
        function.execute(List.of(pattern), context);
    }

    @DataProvider(name = "patternProvider")
    public Object[][] patternProvider() {
        return new Object[][]{
                {"testExecuteWithComplexPattern", "(foo|bar)[0-9]{2,4}"},
                {"testIpv6", "(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))"},
                {"testIpv4", "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)){3}"},
                {"testEmail", "[a-z]{5,15}\\.?[a-z]{5,15}\\@[a-z]{5,15}\\.[a-z]{2}"},
                {"testUri", "((http|https)://[a-zA-Z0-9-]+(\\.[a-zA-Z]{2,})+(/[a-zA-Z0-9-]+){1,6})|(file:///[a-zA-Z0-9-]+(/[a-zA-Z0-9-]+){1,6})"}
        };
    }

    @Test(dataProvider = "patternProvider")
    public void testPatterns(String description, String pattern) {
        for (int i = 0; i < 100; i++) {
            String result = function.execute(List.of(pattern), context);
            assertTrue(result.matches(pattern), "Generated string does not match the pattern: " + description);
        }
    }
}
