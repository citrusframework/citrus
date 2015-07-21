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

package com.consol.citrus.config.xml;

import com.consol.citrus.testng.AbstractBeanDefinitionParserTest;
import com.consol.citrus.validation.MessageValidatorRegistry;
import com.consol.citrus.validation.json.JsonPathMessageValidator;
import com.consol.citrus.validation.json.JsonTextMessageValidator;
import com.consol.citrus.validation.script.GroovyJsonMessageValidator;
import com.consol.citrus.validation.script.GroovyXmlMessageValidator;
import com.consol.citrus.validation.text.PlainTextMessageValidator;
import com.consol.citrus.validation.xhtml.XhtmlMessageValidator;
import com.consol.citrus.validation.xml.DomXmlMessageValidator;
import com.consol.citrus.validation.xml.XpathMessageValidator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class MessageValidatorRegistryParserTest extends AbstractBeanDefinitionParserTest {

    @BeforeClass
    @Override
    protected void parseBeanDefinitions() {
    }

    @Test
    public void testNamespaceContextParser() throws Exception {
        beanDefinitionContext = createApplicationContext("context");
        Map<String, MessageValidatorRegistry> messageValidators = beanDefinitionContext.getBeansOfType(MessageValidatorRegistry.class);

        Assert.assertEquals(messageValidators.size(), 1L);

        MessageValidatorRegistry messageValidatorBean = messageValidators.values().iterator().next();
        Assert.assertEquals(messageValidatorBean.getMessageValidators().size(), 8L);
        Assert.assertEquals(messageValidatorBean.getMessageValidators().get(0).getClass(), DomXmlMessageValidator.class);
        Assert.assertEquals(messageValidatorBean.getMessageValidators().get(1).getClass(), XpathMessageValidator.class);
        Assert.assertEquals(messageValidatorBean.getMessageValidators().get(2).getClass(), GroovyXmlMessageValidator.class);
        Assert.assertEquals(messageValidatorBean.getMessageValidators().get(3).getClass(), PlainTextMessageValidator.class);
        Assert.assertEquals(messageValidatorBean.getMessageValidators().get(4).getClass(), JsonTextMessageValidator.class);
        Assert.assertEquals(messageValidatorBean.getMessageValidators().get(5).getClass(), JsonPathMessageValidator.class);
        Assert.assertEquals(messageValidatorBean.getMessageValidators().get(6).getClass(), GroovyJsonMessageValidator.class);
        Assert.assertEquals(messageValidatorBean.getMessageValidators().get(7).getClass(), XhtmlMessageValidator.class);
    }
}
