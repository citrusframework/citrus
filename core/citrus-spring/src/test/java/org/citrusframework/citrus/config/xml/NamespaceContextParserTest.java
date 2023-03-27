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

package org.citrusframework.citrus.config.xml;

import java.util.Map;

import org.citrusframework.citrus.testng.AbstractBeanDefinitionParserTest;
import org.citrusframework.citrus.xml.namespace.NamespaceContextBuilder;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class NamespaceContextParserTest extends AbstractBeanDefinitionParserTest {

    @BeforeClass
    @Override
    protected void parseBeanDefinitions() {
    }

    @Test
    public void testNamespaceContextParser() throws Exception {
        beanDefinitionContext = createApplicationContext("context");
        Map<String, NamespaceContextBuilder> namespaceContexts = beanDefinitionContext.getBeansOfType(NamespaceContextBuilder.class);

        Assert.assertEquals(namespaceContexts.size(), 1L);

        NamespaceContextBuilder namespaceContextBean = namespaceContexts.values().iterator().next();
        Assert.assertEquals(namespaceContextBean.getNamespaceMappings().size(), 3L);
        Assert.assertEquals(namespaceContextBean.getNamespaceMappings().get("ns0"), "http://citrusframework.org/schemas/1");
        Assert.assertEquals(namespaceContextBean.getNamespaceMappings().get("ns1"), "http://citrusframework.org/schemas/2");
        Assert.assertEquals(namespaceContextBean.getNamespaceMappings().get("ns2"), "http://citrusframework.org/schemas/3");
    }

}
