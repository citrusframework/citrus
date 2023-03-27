/*
 * Copyright 2006-2018 the original author or authors.
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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;

import org.citrusframework.citrus.UnitTestSupport;
import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.citrusframework.citrus.exceptions.InvalidFunctionUsageException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class UrlEncodeFunctionTest extends UnitTestSupport {
    private final UrlEncodeFunction function = new UrlEncodeFunction();

    @Test
    public void testFunction() {
        Assert.assertEquals(function.execute(Collections.singletonList("foo@citrusframework"), context), "foo%40citrusframework");
    }

    @Test
    public void testCustomCharset() {
        Assert.assertEquals(function.execute(Arrays.asList("foo@citrusframework", "UTF-8"), context), "foo%40citrusframework");
    }

    @Test
    public void testUnsupportedCharset() {
        try {
            function.execute(Arrays.asList("foo@citrusframework", "UNKNOWN"), context);
            Assert.fail("Missing exception due to unsupported charset encoding");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getCause().getClass().equals(UnsupportedEncodingException.class));
        }
    }

    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(Collections.emptyList(), context);
    }
}
