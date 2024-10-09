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

package org.citrusframework.validation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Registry factory bean automatically adds all message validators that live in the Spring bean application context.
 * The validators that are located in the Spring bean application context may overwrite existing default message validators that
 * have the same name.
 *
 */
public class MessageValidatorRegistryFactory implements FactoryBean<MessageValidatorRegistry>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final MessageValidatorRegistry registry;

    public MessageValidatorRegistryFactory() {
        this(new DefaultMessageValidatorRegistry());
    }

    public MessageValidatorRegistryFactory(MessageValidatorRegistry messageValidatorRegistry) {
        this.registry = messageValidatorRegistry;
    }

    @Override
    public MessageValidatorRegistry getObject() throws Exception {
        if (applicationContext != null) {
            applicationContext.getBeansOfType(MessageValidator.class)
                    .forEach(registry::addMessageValidator);
        }

        return registry;
    }

    @Override
    public Class<?> getObjectType() {
        return MessageValidatorRegistry.class;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
