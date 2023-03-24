package org.citrusframework.endpoint.direct;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpointComponent;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.message.DefaultMessageQueue;

/**
 * Direct endpoint component creates synchronous or asynchronous channel endpoint and sets configuration properties
 * accordingly.
 *
 * @author Christoph Deppisch
 * @since 3.0
 */
public class DirectEndpointComponent extends AbstractEndpointComponent {

    /**
     * Default constructor using the name for this component.
     */
    public DirectEndpointComponent() {
        super("direct");
    }

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
            context.getReferenceResolver().bind(queueName, new DefaultMessageQueue(queueName));
        }

        enrichEndpointConfiguration(endpoint.getEndpointConfiguration(), parameters, context);

        return endpoint;
    }
}
