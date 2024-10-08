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

package org.citrusframework.validation.interceptor;

import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.MessageProcessors;
import org.citrusframework.variable.dictionary.DataDictionary;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Registry factory bean automatically adds all validation matcher libraries that live in the Spring bean application context.
 * The default validation matcher library is also added via Spring bean reference. This is why this registry explicitly doe not use default registry
 * in order to not duplicate the default validation matcher library.
 *
 */
public class MessageProcessorsFactory implements FactoryBean<MessageProcessors>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final MessageProcessors registry;

    /**
     * Default constructor.
     */
    public MessageProcessorsFactory() {
        this(new MessageProcessors());
    }

    /**
     * Constructor initializes with given registry.
     * @param registry
     */
    public MessageProcessorsFactory(MessageProcessors registry) {
        this.registry = registry;
    }

    @Override
    public MessageProcessors getObject() throws Exception {
        if (applicationContext != null) {
            applicationContext.getBeansOfType(MessageProcessor.class)
                    .entrySet()
                    .stream()
                    .filter(entry -> !(entry.getValue() instanceof DataDictionary<?>) || ((DataDictionary<?>) entry.getValue()).isGlobalScope())
                    .forEach(entry -> registry.addMessageProcessor(entry.getValue()));
        }

        return registry;
    }

    @Override
    public Class<?> getObjectType() {
        return MessageProcessors.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
