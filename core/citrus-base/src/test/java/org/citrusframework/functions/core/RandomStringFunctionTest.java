/*
 * Copyright 2006-2010 the original author or authors.
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
import java.util.Collections;
import java.util.List;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class RandomStringFunctionTest extends UnitTestSupport {
    private RandomStringFunction function = new RandomStringFunction();

    @Test
    public void testFunction() {
        List<String> params = new ArrayList<String>();
        params.add("3");

        Assert.assertTrue(function.execute(params, context).length() == 3);

        params = new ArrayList<String>();
        params.add("3");
        params.add("UPPERCASE");

        Assert.assertTrue(function.execute(params, context).length() == 3);

        params = new ArrayList<String>();
        params.add("3");
        params.add("LOWERCASE");

        Assert.assertTrue(function.execute(params, context).length() == 3);

        params = new ArrayList<String>();
        params.add("3");
        params.add("MIXED");

        Assert.assertTrue(function.execute(params, context).length() == 3);

        params = new ArrayList<String>();
        params.add("3");
        params.add("UNKNOWN");

        Assert.assertTrue(function.execute(params, context).length() == 3);
    }

    @Test
    public void testWithNumbers() {
        List<String> params = new ArrayList<String>();
        params = new ArrayList<String>();
        params.add("10");
        params.add("UPPERCASE");
        params.add("true");

        Assert.assertTrue(function.execute(params, context).length() == 10);

        params = new ArrayList<String>();
        params.add("10");
        params.add("LOWERCASE");
        params.add("true");

        Assert.assertTrue(function.execute(params, context).length() == 10);

        params = new ArrayList<String>();
        params.add("10");
        params.add("MIXED");
        params.add("true");

        Assert.assertTrue(function.execute(params, context).length() == 10);

        params = new ArrayList<String>();
        params.add("10");
        params.add("UNKNOWN");
        params.add("true");

        Assert.assertTrue(function.execute(params, context).length() == 10);
    }

    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testWrongParameterUsage() {
        function.execute(Collections.singletonList("-1"), context);
    }

    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(Collections.<String>emptyList(), context);
    }

    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testTooManyParameters() {
        List<String> params = new ArrayList<String>();
        params.add("3");
        params.add("UPPERCASE");
        params.add("true");
        params.add("too much");

        function.execute(params, context);
    }
}
