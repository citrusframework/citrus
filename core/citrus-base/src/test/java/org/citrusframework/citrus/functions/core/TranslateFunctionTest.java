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

package org.citrusframework.citrus.functions.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.citrusframework.citrus.UnitTestSupport;
import org.citrusframework.citrus.exceptions.InvalidFunctionUsageException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class TranslateFunctionTest extends UnitTestSupport {
    TranslateFunction function = new TranslateFunction();

    @Test
    public void testFunction() {
        List<String> params = new ArrayList<String>();
        params.add("H.llo TestFr.mework");
        params.add("\\.");
        params.add("a");

        Assert.assertEquals(function.execute(params, context), "Hallo TestFramework");
    }

    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testMissingParameter() {
        List<String> params = new ArrayList<String>();
        params.add("H.llo TestFr.mework");
        params.add("\\.");
        function.execute(params, context);
    }

    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(Collections.<String>emptyList(), context);
    }
}