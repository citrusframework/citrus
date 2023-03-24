/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.config.xml;

import java.util.Map;

import org.citrusframework.BeanDefinitionParserTestSupport;
import org.citrusframework.variable.GlobalVariables;
import org.citrusframework.variable.GlobalVariablesPropertyLoader;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class GlobalVariablesParserTest extends BeanDefinitionParserTestSupport {

    @BeforeClass
    @Override
    protected void parseBeanDefinitions() {
    }

    @Test
    public void testGlobalVariablesParser() throws Exception {
        beanDefinitionContext = createApplicationContext("context");
        Map<String, GlobalVariables> globalVariables = beanDefinitionContext.getBeansOfType(GlobalVariables.class);
        Map<String, GlobalVariablesPropertyLoader> globalVariablesPropertyLoaders = beanDefinitionContext.getBeansOfType(GlobalVariablesPropertyLoader.class);

        Assert.assertEquals(globalVariables.size(), 1L);
        Assert.assertEquals(globalVariablesPropertyLoaders.size(), 1L);

        GlobalVariables globalVariablesBean = globalVariables.values().iterator().next();
        Assert.assertEquals(globalVariablesBean.getVariables().size(), 4L);
        Assert.assertEquals(globalVariablesBean.getVariables().get("var1"), "val1");
        Assert.assertEquals(globalVariablesBean.getVariables().get("var2"), "val2");
        Assert.assertEquals(globalVariablesBean.getVariables().get("var3"), "val3");
        Assert.assertEquals(globalVariablesBean.getVariables().get("property.load.test"), "Globale Variable geladen");

        GlobalVariablesPropertyLoader globalVariablesPropertyLoaderBean = globalVariablesPropertyLoaders.values().iterator().next();
        Assert.assertEquals(globalVariablesPropertyLoaderBean.getPropertyFiles().size(), 1L);
        Assert.assertEquals(globalVariablesPropertyLoaderBean.getPropertyFiles().get(0), "classpath:org/citrusframework/variable/loadtest.properties");
    }

    @Test
    public void testGlobalVariablesEmptyParser() throws Exception {
        beanDefinitionContext = createApplicationContext("empty");
        Map<String, GlobalVariables> globalVariables = beanDefinitionContext.getBeansOfType(GlobalVariables.class);

        Assert.assertEquals(globalVariables.size(), 1L);

        GlobalVariables globalVariablesBean = globalVariables.values().iterator().next();
        Assert.assertEquals(globalVariablesBean.getVariables().size(), 0L);
    }
}
