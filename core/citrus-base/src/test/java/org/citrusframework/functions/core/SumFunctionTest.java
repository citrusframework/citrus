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
import java.util.List;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.testng.Assert;
import org.testng.annotations.Test;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class SumFunctionTest extends UnitTestSupport {
    SumFunction function = new SumFunction();

    @Test
    public void testFunction() {
        List<String> params = new ArrayList<>();
        params.add("3");
        params.add("5.3");
        params.add("4.7");
        params.add("0");

        Assert.assertEquals(function.execute(params, context), "13.0");
    }

    @Test(expectedExceptions = {NumberFormatException.class})
    public void testWrongParameterUsage() {
        function.execute(singletonList("no digit"), context);
    }

    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(emptyList(), context);
    }
}
