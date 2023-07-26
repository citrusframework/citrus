/*
 *  Copyright 2023 the original author or authors.
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements. See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.springintegration.yaml;

import java.util.List;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActor;
import org.citrusframework.actions.PurgeMessageChannelAction;
import org.citrusframework.context.SpringBeanReferenceResolver;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.springframework.integration.core.MessageSelector;
import org.springframework.messaging.core.DestinationResolver;

public class PurgeChannels implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    private final PurgeMessageChannelAction.Builder builder = new PurgeMessageChannelAction.Builder();

    private String description;
    private String actor;

    private String channelResolver;
    private String messageSelector;

    private ReferenceResolver referenceResolver;

    public void setDescription(String value) {
        this.description = value;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public void setChannel(String channel) {
        builder.channel(channel);
    }

    public void setChannels(List<String> channels) {
        builder.channelNames(channels);
    }

    public void setChannelResolver(String channelResolver) {
        this.channelResolver = channelResolver;
    }

    public void setMessageSelector(String messageSelector) {
        this.messageSelector = messageSelector;
    }

    public void setSelector(String messageSelector) {
        this.messageSelector = messageSelector;
    }

    @Override
    public TestAction build() {
        builder.setReferenceResolver(referenceResolver);
        builder.description(description);

        if (referenceResolver != null) {
            if (referenceResolver instanceof SpringBeanReferenceResolver) {
                builder.beanFactory(((SpringBeanReferenceResolver) referenceResolver).getApplicationContext());
                builder.withApplicationContext(((SpringBeanReferenceResolver) referenceResolver).getApplicationContext());
            }

            if (actor != null) {
                builder.actor(referenceResolver.resolve(actor, TestActor.class));
            }

            if (channelResolver != null) {
                builder.channelResolver(referenceResolver.resolve(channelResolver, DestinationResolver.class));
            } else {
                builder.channelResolver(referenceResolver);
            }

            if (messageSelector != null) {
                builder.selector(referenceResolver.resolve(messageSelector, MessageSelector.class));
            }
        }

        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
