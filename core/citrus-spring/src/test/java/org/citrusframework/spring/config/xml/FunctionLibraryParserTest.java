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

package org.citrusframework.spring.config.xml;

import java.util.Map;

import org.citrusframework.spring.functions.CustomFunction;
import org.citrusframework.functions.FunctionLibrary;
import org.citrusframework.functions.FunctionParameterHelper;
import org.citrusframework.base.functions.core.ConcatFunction;
import org.citrusframework.base.functions.core.RandomNumberFunction;
import org.citrusframework.base.functions.core.RandomStringFunction;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @since 2.0
 */
public class FunctionLibraryParserTest extends AbstractBeanDefinitionParserTest {

    @BeforeAll
    @Override
    protected void parseBeanDefinitions() {
    }

    @Test
    public void testFunctionLibraryParser() {
        beanDefinitionContext = createApplicationContext("context");
        Map<String, FunctionLibrary> functionLibraries = beanDefinitionContext.getBeansOfType(FunctionLibrary.class);

        Assertions.assertEquals(2L, functionLibraries.size());

        FunctionLibrary functionLibraryBean = functionLibraries.get("functionLib");
        Assertions.assertEquals("functionLib", functionLibraryBean.getName());
        Assertions.assertEquals("foo", functionLibraryBean.getPrefix());
        Assertions.assertEquals(3L, functionLibraryBean.getMembers().size());
        Assertions.assertEquals(RandomNumberFunction.class, functionLibraryBean.getMembers().get("randomNr").getClass());
        Assertions.assertEquals(RandomStringFunction.class, functionLibraryBean.getMembers().get("randomStr").getClass());
        Assertions.assertEquals(CustomFunction.class, functionLibraryBean.getMembers().get("custom").getClass());

        Assertions.assertEquals("Hello Christoph!", functionLibraryBean.getMembers().get("custom").execute(FunctionParameterHelper.getParameterList("Christoph"), context));

        functionLibraryBean = functionLibraries.get("functionLib2");
        Assertions.assertEquals("functionLib2", functionLibraryBean.getName());
        Assertions.assertEquals("bar", functionLibraryBean.getPrefix());
        Assertions.assertEquals(2L, functionLibraryBean.getMembers().size());
        Assertions.assertEquals(ConcatFunction.class, functionLibraryBean.getMembers().get("concat").getClass());
        Assertions.assertEquals(CustomFunction.class, functionLibraryBean.getMembers().get("custom").getClass());

        Assertions.assertEquals("Hello Citrus!", functionLibraryBean.getMembers().get("custom").execute(FunctionParameterHelper.getParameterList("Citrus"), context));
    }
}
