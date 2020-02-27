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

package com.consol.citrus.validation;

import java.util.Map;
import java.util.function.Supplier;

import com.consol.citrus.validation.context.ValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
@Configuration
public class MessageValidatorConfig {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(MessageValidatorConfig.class);

    private static final MessageValidatorRegistry MESSAGE_VALIDATOR_REGISTRY = new DefaultMessageValidatorRegistry();

    @Bean
    public static BeanDefinitionRegistryPostProcessor messageValidatorRegistrationProcessor() {
        return new BeanDefinitionRegistryPostProcessor() {
            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
                for (Map.Entry<String, MessageValidator<? extends ValidationContext>> entry : MESSAGE_VALIDATOR_REGISTRY.getMessageValidators().entrySet()) {
                    if (!registry.containsBeanDefinition(entry.getKey())) {
                        MessageValidator messageValidator = entry.getValue();
                        Supplier<MessageValidator> supplier = () -> messageValidator;
                        log.info(String.format("Register message validator bean '%s' of type %s", entry.getKey(), messageValidator.getClass()));
                        registry.registerBeanDefinition(entry.getKey(), BeanDefinitionBuilder.genericBeanDefinition(MessageValidator.class, supplier).getBeanDefinition());
                    }
                }
            }

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            }
        };
    }

    @Bean(name = MessageValidatorRegistry.BEAN_NAME)
    public MessageValidatorRegistryFactory messageValidatorRegistry() {
        return new MessageValidatorRegistryFactory(MESSAGE_VALIDATOR_REGISTRY);
    }
}
