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

package org.citrusframework.message.selector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageSelector;
import org.citrusframework.message.MessageSelectorBuilder;

/**
 * Message selector delegates incoming messages to several other selector implementations
 * according to selector names.
 *
 * @author Christoph Deppisch
 * @since 3.0
 */
public class DelegatingMessageSelector implements MessageSelector {

    /** List of header elements to match */
    private final Map<String, String> matchingHeaders;

    /** List of available message selector factories */
    private final List<MessageSelectorFactory> factories;

    /** Test context */
    private final TestContext context;

    /**
     * Default constructor using a selector string.
     */
    public DelegatingMessageSelector(String selector, TestContext context) {
        this.context = context;
        this.matchingHeaders = MessageSelectorBuilder.withString(selector).toKeyValueMap();

        if (matchingHeaders.isEmpty()) {
            throw new CitrusRuntimeException("Invalid empty message selector");
        }

        factories = new ArrayList<>();

        if (context.getReferenceResolver() != null) {
            factories.addAll(context.getReferenceResolver().resolveAll(MessageSelectorFactory.class).values());
        }

        factories.addAll(MessageSelector.lookup().values());
    }

    @Override
    public boolean accept(Message message) {
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
    public void addMessageSelectorFactory(MessageSelectorFactory factory) {
        this.factories.add(factory);
    }

}
