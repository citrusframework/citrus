package com.consol.citrus.endpoint.direct.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.config.annotation.AnnotationConfigParser;
import com.consol.citrus.endpoint.direct.DirectSyncEndpoint;
import com.consol.citrus.endpoint.direct.DirectSyncEndpointBuilder;
import com.consol.citrus.message.MessageCorrelator;
import com.consol.citrus.message.MessageQueue;
import com.consol.citrus.spi.ReferenceResolver;
import org.springframework.util.StringUtils;

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
