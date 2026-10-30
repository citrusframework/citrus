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

import org.citrusframework.spring.BeanDefinitionParserTestSupport;
import org.citrusframework.variable.GlobalVariables;
import org.citrusframework.spring.variable.GlobalVariablesPropertyLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GlobalVariablesParserTest extends BeanDefinitionParserTestSupport {

    @BeforeAll
    @Override
    protected void parseBeanDefinitions() {
    }

    @Test
    public void testGlobalVariablesParser() {
        beanDefinitionContext = createApplicationContext("context");
        Map<String, GlobalVariables> globalVariables = beanDefinitionContext.getBeansOfType(GlobalVariables.class);
        Map<String, GlobalVariablesPropertyLoader> globalVariablesPropertyLoaders = beanDefinitionContext.getBeansOfType(GlobalVariablesPropertyLoader.class);

        Assertions.assertEquals(1L, globalVariables.size());
        Assertions.assertEquals(1L, globalVariablesPropertyLoaders.size());

        GlobalVariables globalVariablesBean = globalVariables.values().iterator().next();
        Assertions.assertEquals(4L, globalVariablesBean.getVariables().size());
        Assertions.assertEquals("val1", globalVariablesBean.getVariables().get("var1"));
        Assertions.assertEquals("val2", globalVariablesBean.getVariables().get("var2"));
        Assertions.assertEquals("val3", globalVariablesBean.getVariables().get("var3"));
        Assertions.assertEquals("Globale Variable geladen", globalVariablesBean.getVariables().get("property.load.test"));

        GlobalVariablesPropertyLoader globalVariablesPropertyLoaderBean = globalVariablesPropertyLoaders.values().iterator().next();
        Assertions.assertEquals(1L, globalVariablesPropertyLoaderBean.getPropertyFiles().size());
        Assertions.assertEquals("classpath:org/citrusframework/spring/variable/loadtest.properties", globalVariablesPropertyLoaderBean.getPropertyFiles().get(0));
    }

    @Test
    public void testGlobalVariablesEmptyParser() {
        beanDefinitionContext = createApplicationContext("empty");
        Map<String, GlobalVariables> globalVariables = beanDefinitionContext.getBeansOfType(GlobalVariables.class);

        Assertions.assertEquals(1L, globalVariables.size());

        GlobalVariables globalVariablesBean = globalVariables.values().iterator().next();
        Assertions.assertEquals(0L, globalVariablesBean.getVariables().size());
    }
}
