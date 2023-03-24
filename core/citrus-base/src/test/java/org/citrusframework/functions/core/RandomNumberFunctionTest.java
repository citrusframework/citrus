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
public class RandomNumberFunctionTest extends UnitTestSupport {
    private final RandomNumberFunction function = new RandomNumberFunction();

    @Test
    public void testFunction() {
        List<String> params = new ArrayList<String>();
        params.add("3");

        Assert.assertTrue(Integer.valueOf(function.execute(params, context)) < 1000);

        params = new ArrayList<String>();
        params.add("3");
        params.add("false");

        String generated = function.execute(params, context);
        Assert.assertTrue(generated.length() <= 3);
        Assert.assertTrue(generated.length() > 0);
    }

    @Test
    public void testLeadingZeroNumbers() {
        String generated = RandomNumberFunction.checkLeadingZeros("0001", true);
        Assert.assertTrue(Integer.valueOf(generated.substring(0, 1)) > 0);

        generated = RandomNumberFunction.checkLeadingZeros("0009", true);
        Assert.assertEquals(generated.length(), 4);

        generated = RandomNumberFunction.checkLeadingZeros("00000", true);
        Assert.assertEquals(generated.length(), 5);
        Assert.assertTrue(Integer.valueOf(generated.substring(0, 1)) > 0);
        Assert.assertTrue(generated.endsWith("0000"));

        generated = RandomNumberFunction.checkLeadingZeros("009809", true);
        Assert.assertEquals(generated.length(), 6);
        Assert.assertTrue(Integer.valueOf(generated.substring(0, 1)) > 0);
        Assert.assertTrue(generated.endsWith("09809"));

        generated = RandomNumberFunction.checkLeadingZeros("01209", true);
        Assert.assertEquals(generated.length(), 5);
        Assert.assertTrue(Integer.valueOf(generated.substring(0, 1)) > 0);
        Assert.assertTrue(generated.endsWith("1209"));

        generated = RandomNumberFunction.checkLeadingZeros("1209", true);
        Assert.assertEquals(generated.length(), 4);
        Assert.assertEquals(generated, "1209");

        generated = RandomNumberFunction.checkLeadingZeros("00000", false);
        Assert.assertEquals(generated.length(), 1);
        Assert.assertEquals(generated, "0");

        generated = RandomNumberFunction.checkLeadingZeros("0009", false);
        Assert.assertEquals(generated.length(), 1);
        Assert.assertEquals(generated, "9");

        generated = RandomNumberFunction.checkLeadingZeros("01209", false);
        Assert.assertEquals(generated.length(), 4);
        Assert.assertEquals(generated, "1209");

        generated = RandomNumberFunction.checkLeadingZeros("1209", false);
        Assert.assertEquals(generated.length(), 4);
        Assert.assertEquals(generated, "1209");
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
        params.add("true");
        params.add("too much");

        function.execute(params, context);
    }
}
