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

import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.DefaultFunctionLibrary;
import org.citrusframework.functions.Function;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class EscapeXmlFunctionTest extends AbstractTestNGUnitTest {
    EscapeXmlFunction function = new EscapeXmlFunction();

    @Test
    public void testFunction() {
        String xmlFragment = "<foo><bar>Yes, I like Citrus!</bar></foo>";
        String escapedXml = "&lt;foo&gt;&lt;bar&gt;Yes, I like Citrus!&lt;/bar&gt;&lt;/foo&gt;";
        Assert.assertEquals(function.execute(Collections.singletonList(xmlFragment), context), escapedXml);
    }

    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(Collections.<String>emptyList(), context);
    }

    @Test
    public void shouldLookupFunction() {
        Assert.assertTrue(Function.lookup().containsKey("escapeXml"));
        Assert.assertEquals(Function.lookup().get("escapeXml").getClass(), EscapeXmlFunction.class);

        Assert.assertEquals(new DefaultFunctionLibrary().getFunction("escapeXml").getClass(), EscapeXmlFunction.class);
    }
}
