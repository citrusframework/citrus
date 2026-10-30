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

import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.MessageValidatorRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 2.0
 */
public class MessageValidatorRegistryParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testNamespaceContextParser() {
        Map<String, MessageValidatorRegistry> messageValidators = beanDefinitionContext.getBeansOfType(MessageValidatorRegistry.class);

        Assertions.assertEquals(1L, messageValidators.size());

        MessageValidatorRegistry messageValidatorBean = messageValidators.values().iterator().next();
        Assertions.assertEquals(3L, messageValidatorBean.getMessageValidators().size());
        Assertions.assertEquals(DefaultMessageHeaderValidator.class, messageValidatorBean.getMessageValidator(DefaultMessageHeaderValidator.class.getName()).getClass());
        Assertions.assertEquals(beanDefinitionContext.getBean("validator1"), messageValidatorBean.getMessageValidator("validator1"));
        Assertions.assertEquals(beanDefinitionContext.getBean("validator2"), messageValidatorBean.getMessageValidator("validator2"));
    }
}
