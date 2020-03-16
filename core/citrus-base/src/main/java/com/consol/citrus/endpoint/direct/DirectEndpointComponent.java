package com.consol.citrus.endpoint.direct;

import java.util.Map;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.AbstractEndpointComponent;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.message.DefaultMessageQueue;

/**
 * Direct endpoint component creates synchronous or asynchronous channel endpoint and sets configuration properties
 * accordingly.
 *
 * @author Christoph Deppisch
 * @since 3.0
 */
public class DirectEndpointComponent extends AbstractEndpointComponent {

    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context) {
        DirectEndpoint endpoint;
        final String queueName;
        if (resourcePath.startsWith("sync:")) {
            DirectSyncEndpointConfiguration endpointConfiguration = new DirectSyncEndpointConfiguration();
            endpoint = new DirectSyncEndpoint(endpointConfiguration);
            queueName = resourcePath.substring("sync:".length());
        } else {
            endpoint = new DirectEndpoint();
            queueName = resourcePath;
        }

        endpoint.getEndpointConfiguration().setQueueName(queueName);
        if (!context.getReferenceResolver().isResolvable(queueName)) {
            context.getReferenceResolver().bind(queueName, new DefaultMessageQueue());
        }

        enrichEndpointConfiguration(endpoint.getEndpointConfiguration(), parameters, context);

        return endpoint;
    }
}
