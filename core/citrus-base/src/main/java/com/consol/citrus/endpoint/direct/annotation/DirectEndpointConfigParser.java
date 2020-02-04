package com.consol.citrus.endpoint.direct.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.config.annotation.AbstractAnnotationConfigParser;
import com.consol.citrus.context.ReferenceResolver;
import com.consol.citrus.endpoint.direct.DirectEndpoint;
import com.consol.citrus.endpoint.direct.DirectEndpointBuilder;
import com.consol.citrus.message.MessageQueue;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 */
public class DirectEndpointConfigParser extends AbstractAnnotationConfigParser<DirectEndpointConfig, DirectEndpoint> {

    /**
     * Constructor matching super.
     * @param referenceResolver
     */
    public DirectEndpointConfigParser(ReferenceResolver referenceResolver) {
        super(referenceResolver);
    }

    @Override
    public DirectEndpoint parse(DirectEndpointConfig annotation) {
        DirectEndpointBuilder builder = new DirectEndpointBuilder();

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

        return builder.initialize().build();
    }
}
