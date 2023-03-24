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
public class SubstringFunctionTest extends UnitTestSupport {
    SubstringFunction function = new SubstringFunction();

    @Test
    public void testFunction() {
        List<String> params = new ArrayList<String>();
        params.add("Hallo,TestFramework");
        params.add("6");
        Assert.assertEquals(function.execute(params, context), "TestFramework");

        params.clear();
        params.add("This is a test");
        params.add("0");
        Assert.assertEquals(function.execute(params, context), "This is a test");
    }

    @Test
    public void testEndIndex() {
        List<String> params = new ArrayList<String>();
        params.add("Hallo,TestFramework");
        params.add("6");
        params.add("10");
        Assert.assertEquals(function.execute(params, context), "Test");

        params.clear();
        params.add("This is a test");
        params.add("0");
        params.add("4");
        Assert.assertEquals(function.execute(params, context), "This");
    }

    @Test(expectedExceptions = {StringIndexOutOfBoundsException.class})
    public void testIndexOutOfBounds() {
        List<String> params = new ArrayList<String>();
        params.add("Test");
        params.add("-1");
        function.execute(params, context);
    }

    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testMissingBeginIndex() {
        function.execute(Collections.singletonList("This is a test"), context);
    }

    @Test(expectedExceptions = {NumberFormatException.class})
    public void testNotANumber() {
        List<String> params = new ArrayList<String>();
        params.add("Hallo,TestFramework");
        params.add("one");
        function.execute(params, context);
    }

    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(Collections.<String>emptyList(), context);
    }
}
