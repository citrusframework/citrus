package org.citrusframework.config.xml;

import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.endpoint.direct.DirectEndpointConfiguration;

/**
 * Simple endpoint parser.
 *
 * @author Christoph Deppisch
 * @since 3.0
 */
public class DirectEndpointParser extends AbstractDirectEndpointParser {

    @Override
    protected Class<? extends Endpoint> getEndpointClass() {
        return DirectEndpoint.class;
    }

    @Override
    protected Class<? extends EndpointConfiguration> getEndpointConfigurationClass() {
        return DirectEndpointConfiguration.class;
    }
}
