package com.consol.citrus.dsl.builder;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import java.util.Arrays;
import java.util.List;

import com.consol.citrus.AbstractTestActionBuilder;
import com.consol.citrus.jms.actions.PurgeJmsQueuesAction;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;

/**
 * @author Christoph Deppisch
 */
public final class PurgeJmsQueuesActionBuilder extends AbstractTestActionBuilder<PurgeJmsQueuesAction, PurgeJmsQueuesActionBuilder> implements ReferenceResolverAware {

    private final PurgeJmsQueuesAction.Builder delegate = new PurgeJmsQueuesAction.Builder();

    public PurgeJmsQueuesActionBuilder connectionFactory(ConnectionFactory connectionFactory) {
        delegate.connectionFactory(connectionFactory);
        return this;
    }

    public PurgeJmsQueuesActionBuilder queues(List<Queue> queues) {
        delegate.queues(queues);
        return this;
    }

    public PurgeJmsQueuesActionBuilder queues(Queue... queues) {
        return queues(Arrays.asList(queues));
    }

    public PurgeJmsQueuesActionBuilder queue(Queue queue) {
        delegate.queue(queue);
        return this;
    }

    public PurgeJmsQueuesActionBuilder queueNames(List<String> names) {
        delegate.queueNames(names);
        return this;
    }

    public PurgeJmsQueuesActionBuilder queueNames(String... names) {
        return queueNames(Arrays.asList(names));
    }

    public PurgeJmsQueuesActionBuilder queue(String name) {
        delegate.queue(name);
        return this;
    }

    public PurgeJmsQueuesActionBuilder timeout(long receiveTimeout) {
        delegate.timeout(receiveTimeout);
        return this;
    }

    public PurgeJmsQueuesActionBuilder sleep(long millis) {
        delegate.sleep(millis);
        return this;
    }

    public PurgeJmsQueuesActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        delegate.withReferenceResolver(referenceResolver);
        return this;
    }

    @Override
    public PurgeJmsQueuesAction build() {
        return delegate.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        delegate.setReferenceResolver(referenceResolver);
    }

}
