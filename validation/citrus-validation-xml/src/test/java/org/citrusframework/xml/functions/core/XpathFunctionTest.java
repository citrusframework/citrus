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

package org.citrusframework.xml.functions.core;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.base.functions.DefaultFunctionLibrary;
import org.citrusframework.functions.Function;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @since 2.6
 */
public class XpathFunctionTest extends AbstractUnitTest {

    private final XpathFunction function = new XpathFunction();

    private final String xmlSource = "<person><name>Sheldon</name><age>29</age></person>";

    @Test
    public void testExecuteXpath() {
        List<String> parameters = new ArrayList<>();
        parameters.add(xmlSource);
        parameters.add("/person/name");
        Assertions.assertEquals("Sheldon", function.execute(parameters, context));
    }

    @Test
    public void testExecuteXpathWithNamespaces() {
        List<String> parameters = new ArrayList<>();
        String xmlSourceNamespace = "<person xmlns=\"http://citrus.sample.org/person\"><name>Sheldon</name><age>29</age></person>";
        parameters.add(xmlSourceNamespace);
        parameters.add("/p:person/p:name");

        context.getNamespaceContextBuilder().getNamespaceMappings().put("p", "http://citrus.sample.org/person");

        Assertions.assertEquals("Sheldon", function.execute(parameters, context));
    }

    @Test
    public void testExecuteXpathUnknown() {
        List<String> parameters = new ArrayList<>();
        parameters.add(xmlSource);
        parameters.add("/person/unknown");
        assertThrows(CitrusRuntimeException.class, () -> function.execute(parameters, context));
    }

    @Test
    public void shouldLookupFunction() {
        Assertions.assertTrue(Function.lookup().containsKey("xpath"));
        Assertions.assertEquals(XpathFunction.class, Function.lookup().get("xpath").getClass());

        Assertions.assertEquals(XpathFunction.class, new DefaultFunctionLibrary().getFunction("xpath").getClass());
    }
}
