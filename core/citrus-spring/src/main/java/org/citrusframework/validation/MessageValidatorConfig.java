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

package org.citrusframework.validation;

import java.util.Map;
import java.util.function.Supplier;

import org.citrusframework.validation.context.ValidationContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration loads message validator registry via factory bean and automatically adds all validators as Spring beans
 * in factory post processor.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
@Configuration
public class MessageValidatorConfig {

    private static final MessageValidatorRegistry MESSAGE_VALIDATOR_REGISTRY = new DefaultMessageValidatorRegistry();

    @Bean
    public static BeanDefinitionRegistryPostProcessor messageValidatorRegistrationProcessor() {
        return new BeanDefinitionRegistryPostProcessor() {
            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
                for (Map.Entry<String, MessageValidator<? extends ValidationContext>> entry : MESSAGE_VALIDATOR_REGISTRY.getMessageValidators().entrySet()) {
                    if (!registry.containsBeanDefinition(entry.getKey())) {
                        MessageValidator<? extends ValidationContext> messageValidator = entry.getValue();
                        Supplier<MessageValidator> supplier = () -> messageValidator;
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
