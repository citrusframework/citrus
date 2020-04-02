package com.consol.citrus.endpoint.direct.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.config.annotation.AnnotationConfigParser;
import com.consol.citrus.endpoint.direct.DirectEndpoint;
import com.consol.citrus.endpoint.direct.DirectEndpointBuilder;
import com.consol.citrus.message.MessageQueue;
import com.consol.citrus.spi.ReferenceResolver;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 */
public class DirectEndpointConfigParser implements AnnotationConfigParser<DirectEndpointConfig, DirectEndpoint> {

    @Override
    public DirectEndpoint parse(DirectEndpointConfig annotation, ReferenceResolver referenceResolver) {
        DirectEndpointBuilder builder = new DirectEndpointBuilder();

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

        return builder.initialize().build();
    }
}
