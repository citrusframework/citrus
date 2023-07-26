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

package org.citrusframework.springintegration.xml;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActor;
import org.citrusframework.actions.PurgeMessageChannelAction;
import org.citrusframework.context.SpringBeanReferenceResolver;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.springframework.integration.core.MessageSelector;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolver;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "description",
        "channels",
})
@XmlRootElement(name = "purge-channels")
public class PurgeChannels implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    @XmlTransient
    private final PurgeMessageChannelAction.Builder builder = new PurgeMessageChannelAction.Builder();

    @XmlElement
    private String description;
    @XmlAttribute
    private String actor;

    @XmlAttribute(name = "channel-resolver")
    protected String channelResolver;

    @XmlAttribute(name = "message-selector")
    protected String messageSelector;

    @XmlElement(name = "channel")
    protected List<Channel> channels;

    @XmlTransient
    private ReferenceResolver referenceResolver;

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getActor() {
        return actor;
    }

    public void setChannelResolver(String channelResolver) {
        this.channelResolver = channelResolver;
    }

    public String getChannelResolver() {
        return channelResolver;
    }

    public void setMessageSelector(String messageSelector) {
        this.messageSelector = messageSelector;
    }

    public String getMessageSelector() {
        return messageSelector;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    public List<Channel> getChannels() {
        if (channels == null) {
            channels = new ArrayList<>();
        }

        return channels;
    }

    @Override
    public TestAction build() {
        builder.setReferenceResolver(referenceResolver);
        builder.description(description);

        for (Channel channel : getChannels()) {
            if (channel.name != null) {
                builder.channel(channel.getName());
            }

            if (channel.ref != null && referenceResolver != null) {
                builder.channel(referenceResolver.resolve(channel.ref, MessageChannel.class));
            }
        }

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

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {})
    public static class Channel {
        @XmlAttribute
        protected String name;

        @XmlAttribute
        protected String ref;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setRef(String ref) {
            this.ref = ref;
        }

        public String getRef() {
            return ref;
        }
    }
}
