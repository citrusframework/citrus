/*
 * Copyright 2006-2016 the original author or authors.
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

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class XpathFunctionTest extends AbstractTestNGUnitTest {

    private XpathFunction function = new XpathFunction();

    private String xmlSource = "<person><name>Sheldon</name><age>29</age></person>";
    private String xmlSourceNamespace = "<person xmlns=\"http://citrus.sample.org/person\"><name>Sheldon</name><age>29</age></person>";

    @Test
    public void testExecuteXpath() throws Exception {
        List<String> parameters = new ArrayList<>();
        parameters.add(xmlSource);
        parameters.add("/person/name");
        Assert.assertEquals(function.execute(parameters, context), "Sheldon");
    }

    @Test
    public void testExecuteXpathWithNamespaces() throws Exception {
        List<String> parameters = new ArrayList<>();
        parameters.add(xmlSourceNamespace);
        parameters.add("/p:person/p:name");

        context.getNamespaceContextBuilder().getNamespaceMappings().put("p", "http://citrus.sample.org/person");

        Assert.assertEquals(function.execute(parameters, context), "Sheldon");
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testExecuteXpathUnknown() throws Exception {
        List<String> parameters = new ArrayList<>();
        parameters.add(xmlSource);
        parameters.add("/person/unknown");
        function.execute(parameters, context);
    }
}