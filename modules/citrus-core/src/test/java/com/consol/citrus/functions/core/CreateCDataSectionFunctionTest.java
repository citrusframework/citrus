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

package com.consol.citrus.functions.core;

import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * @author Christoph Deppisch
 */
public class CreateCDataSectionFunctionTest extends AbstractTestNGUnitTest {
    CreateCDataSectionFunction function = new CreateCDataSectionFunction();
    
    private String xmlFragment = "<foo><bar>I like Citrus!</bar></foo>";
    private String resultXml = "<![CDATA[<foo><bar>I like Citrus!</bar></foo>]]>";
    
    @Test
    public void testFunction() {
        Assert.assertEquals(function.execute(Collections.singletonList(xmlFragment), context), resultXml);
    }
    
    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(Collections.<String>emptyList(), context);
    }
}
