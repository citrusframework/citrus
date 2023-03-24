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

import java.util.Collections;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class UpperCaseFunctionTest extends UnitTestSupport {
    UpperCaseFunction function = new UpperCaseFunction();

    @Test
    public void testFunction() {
        Assert.assertEquals(function.execute(Collections.singletonList("1000"), context), "1000");
        Assert.assertEquals(function.execute(Collections.singletonList("Hallo TestFramework!"), context), "HALLO TESTFRAMEWORK!");
        Assert.assertEquals(function.execute(Collections.singletonList("Today is: 09.02.2009"), context), "TODAY IS: 09.02.2009");
    }

    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(Collections.<String>emptyList(), context);
    }
}
