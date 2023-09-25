/*
 * Copyright 2006-2012 the original author or authors.
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

package org.citrusframework.channel.selector;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.MessageSelectorBuilder;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.core.MessageSelector;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;

import java.util.*;

/**
 * Message selector dispatches incoming messages to several other selector implementations
 * according to selector names.
 * 
 * By default uses {@link HeaderMatchingMessageSelector} and supports {@link RootQNameMessageSelector} and
 * {@link XpathPayloadMessageSelector}.
 * 
 * @author Christoph Deppisch
 * @since 1.2
 */
public class DispatchingMessageSelector implements MessageSelector {

    /** List of header elements to match */
    private final Map<String, String> matchingHeaders;
    
    /** Spring bean factory */
    private final BeanFactory beanFactory;

    /** List of available message selector factories */
    private final List<MessageSelectorFactory> factories = new ArrayList<>();

    /** Test context */
    private final TestContext context;

    /**
     * Default constructor using a selector string.
     */
    public DispatchingMessageSelector(String selector, BeanFactory beanFactory, TestContext context) {
        this.beanFactory = beanFactory;
        this.context = context;
        this.matchingHeaders = MessageSelectorBuilder.withString(selector).toKeyValueMap();
        
        Assert.isTrue(matchingHeaders.size() > 0, "Invalid empty message selector");

        factories.add(new RootQNameMessageSelector.Factory());
        factories.add(new XpathPayloadMessageSelector.Factory());
        factories.add(new JsonPathPayloadMessageSelector.Factory());
        factories.add(new PayloadMatchingMessageSelector.Factory());

        if (beanFactory instanceof ApplicationContext) {
            Map<String, MessageSelectorFactory> factoryBeans = ((ApplicationContext) beanFactory).getBeansOfType(MessageSelectorFactory.class);
            factories.addAll(factoryBeans.values());
        }

        factories.parallelStream()
                .filter(factory -> factory instanceof BeanFactoryAware)
                .map(factory -> (BeanFactoryAware) factory)
                .forEach(factory -> factory.setBeanFactory(beanFactory));
    }
    
    @Override
    public boolean accept(Message<?> message) {
        return matchingHeaders.entrySet()
                              .stream()
                              .allMatch(entry -> factories.stream()
                                                     .filter(factory -> factory.supports(entry.getKey()))
                                                     .findAny()
                                                     .orElseGet(HeaderMatchingMessageSelector.Factory::new)
                                                     .create(entry.getKey(), entry.getValue(), context)
                                                     .accept(message));
    }

    /**
     * Add message selector factory to list of delegates.
     * @param factory
     */
    public void addMessageSelectorFactory(MessageSelectorFactory<?> factory) {
        if (factory instanceof BeanFactoryAware) {
            ((BeanFactoryAware) factory).setBeanFactory(beanFactory);
        }

        this.factories.add(factory);
    }

}
