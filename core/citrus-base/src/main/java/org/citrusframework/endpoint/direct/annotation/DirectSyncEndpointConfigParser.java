package org.citrusframework.endpoint.direct.annotation;

import org.citrusframework.TestActor;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.direct.DirectSyncEndpoint;
import org.citrusframework.endpoint.direct.DirectSyncEndpointBuilder;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 */
public class DirectSyncEndpointConfigParser implements AnnotationConfigParser<DirectSyncEndpointConfig, DirectSyncEndpoint> {

    @Override
    public DirectSyncEndpoint parse(DirectSyncEndpointConfig annotation, ReferenceResolver referenceResolver) {
        DirectSyncEndpointBuilder builder = new DirectSyncEndpointBuilder();

        String queue = annotation.queue();
        String queueName = annotation.queueName();

        if (StringUtils.hasText(queue)) {
            builder.queue(referenceResolver.resolve(annotation.queue(), MessageQueue.class));
        }

        if (StringUtils.hasText(queueName)) {
            builder.queue(annotation.queueName());
        }

        builder.timeout(annotation.timeout());

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        if (StringUtils.hasText(annotation.correlator())) {
            builder.correlator(referenceResolver.resolve(annotation.correlator(), MessageCorrelator.class));
        }

        builder.pollingInterval(annotation.pollingInterval());

        return builder.initialize().build();
    }
}
