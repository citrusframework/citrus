package com.consol.citrus.dsl.builder;

import java.util.Arrays;
import java.util.List;

import com.consol.citrus.AbstractTestActionBuilder;
import com.consol.citrus.actions.PurgeMessageChannelAction;
import com.consol.citrus.spi.ReferenceResolver;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.core.MessageSelector;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolver;

/**
 * @author Christoph Deppisch
 */
public class PurgeMessageChannelActionBuilder extends AbstractTestActionBuilder<PurgeMessageChannelAction, PurgeMessageChannelActionBuilder> {

    private final PurgeMessageChannelAction.Builder delegate = new PurgeMessageChannelAction.Builder();

    public PurgeMessageChannelActionBuilder selector(MessageSelector messageSelector) {
        delegate.selector(messageSelector);
        return this;
    }

    public PurgeMessageChannelActionBuilder channelResolver(ReferenceResolver referenceResolver) {
        delegate.channelResolver(referenceResolver);
        return this;
    }

    public PurgeMessageChannelActionBuilder channelResolver(DestinationResolver<MessageChannel> channelResolver) {
        delegate.channelResolver(channelResolver);
        return this;
    }

    public PurgeMessageChannelActionBuilder channelNames(List<String> channelNames) {
        delegate.channelNames(channelNames);
        return this;
    }

    public PurgeMessageChannelActionBuilder channelNames(String... channelNames) {
        delegate.channelNames(channelNames);
        return this;
    }

    public PurgeMessageChannelActionBuilder channel(String name) {
        delegate.channel(name);
        return this;
    }

    public PurgeMessageChannelActionBuilder channels(List<MessageChannel> channels) {
        delegate.channels(channels);
        return this;
    }

    public PurgeMessageChannelActionBuilder channels(MessageChannel... channels) {
        return channels(Arrays.asList(channels));
    }

    public PurgeMessageChannelActionBuilder channel(MessageChannel channel) {
        delegate.channel(channel);
        return this;
    }

    public PurgeMessageChannelActionBuilder withApplicationContext(ApplicationContext applicationContext) {
        delegate.withApplicationContext(applicationContext);
        return this;
    }

    public PurgeMessageChannelActionBuilder beanFactory(BeanFactory beanFactory) {
        delegate.beanFactory(beanFactory);
        return this;
    }

    @Override
    public PurgeMessageChannelAction build() {
        return delegate.build();
    }
}
