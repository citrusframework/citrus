package com.consol.citrus.endpoint.direct.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.config.annotation.AbstractAnnotationConfigParser;
import com.consol.citrus.context.ReferenceResolver;
import com.consol.citrus.endpoint.direct.DirectSyncEndpoint;
import com.consol.citrus.endpoint.direct.DirectSyncEndpointBuilder;
import com.consol.citrus.message.MessageCorrelator;
import com.consol.citrus.message.MessageQueue;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 */
public class DirectSyncEndpointConfigParser extends AbstractAnnotationConfigParser<DirectSyncEndpointConfig, DirectSyncEndpoint> {

    /**
     * Constructor matching super.
     * @param referenceResolver
     */
    public DirectSyncEndpointConfigParser(ReferenceResolver referenceResolver) {
        super(referenceResolver);
    }

    @Override
    public DirectSyncEndpoint parse(DirectSyncEndpointConfig annotation) {
        DirectSyncEndpointBuilder builder = new DirectSyncEndpointBuilder();

        String queue = annotation.queue();
        String queueName = annotation.queueName();

        if (StringUtils.hasText(queue)) {
            builder.queue(getReferenceResolver().resolve(annotation.queue(), MessageQueue.class));
        }

        if (StringUtils.hasText(queueName)) {
            builder.queue(annotation.queueName());
        }

        builder.timeout(annotation.timeout());

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(getReferenceResolver().resolve(annotation.actor(), TestActor.class));
        }

        if (StringUtils.hasText(annotation.correlator())) {
            builder.correlator(getReferenceResolver().resolve(annotation.correlator(), MessageCorrelator.class));
        }

        builder.pollingInterval(annotation.pollingInterval());

        return builder.initialize().build();
    }
}
