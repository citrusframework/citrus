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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import java.util.Set;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.testng.Assert;
import org.testng.annotations.Test;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class RandomStringFunctionTest extends UnitTestSupport {

    private final RandomStringFunction function = new RandomStringFunction();

    @Test
    public void testFunction() {
        List<String> params = new ArrayList<>();
        params.add("3");

        Assert.assertEquals(function.execute(params, context).length(), 3);

        params = new ArrayList<>();
        params.add("3");
        params.add("UPPERCASE");

        Assert.assertEquals(function.execute(params, context).length(), 3);

        params = new ArrayList<>();
        params.add("3");
        params.add("LOWERCASE");

        Assert.assertEquals(function.execute(params, context).length(), 3);

        params = new ArrayList<>();
        params.add("3");
        params.add("MIXED");

        Assert.assertEquals(function.execute(params, context).length(), 3);

        params = new ArrayList<>();
        params.add("3");
        params.add("UNKNOWN");

        Assert.assertEquals(function.execute(params, context).length(), 3);
    }

    @Test
    public void testWithNumbers() {
        List<String> params;
        params = new ArrayList<>();
        params.add("10");
        params.add("UPPERCASE");
        params.add("true");

        Assert.assertEquals(function.execute(params, context).length(), 10);

        params = new ArrayList<>();
        params.add("10");
        params.add("LOWERCASE");
        params.add("true");

        Assert.assertEquals(function.execute(params, context).length(), 10);

        params = new ArrayList<>();
        params.add("10");
        params.add("MIXED");
        params.add("true");

        Assert.assertEquals(function.execute(params, context).length(), 10);

        params = new ArrayList<>();
        params.add("10");
        params.add("UNKNOWN");
        params.add("true");

        Assert.assertEquals(function.execute(params, context).length(), 10);
    }

    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testWrongParameterUsage() {
        function.execute(singletonList("-1"), context);
    }

    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(emptyList(), context);
    }

    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testTooManyParameters() {
        List<String> params = new ArrayList<>();
        params.add("3");
        params.add("UPPERCASE");
        params.add("true");
        params.add("0");
        params.add("too many");

        function.execute(params, context);
    }

    @Test
    public void testRandomSize() {
        List<String> params;
        params = new ArrayList<>();
        params.add("10");
        params.add("UPPERCASE");
        params.add("true");
        params.add("8");

        Set<Integer> sizes = new HashSet<>();

        for (int i = 0; i < 1000; i++) {
            String text = function.execute(params, context);
            sizes.add(text.length());
        }

        Assert.assertTrue(sizes.contains(8));
        Assert.assertTrue(sizes.contains(9));
        Assert.assertTrue(sizes.contains(10));

    }
}