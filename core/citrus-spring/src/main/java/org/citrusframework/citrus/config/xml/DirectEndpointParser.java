package org.citrusframework.citrus.config.xml;

import org.citrusframework.citrus.endpoint.Endpoint;
import org.citrusframework.citrus.endpoint.EndpointConfiguration;
import org.citrusframework.citrus.endpoint.direct.DirectEndpoint;
import org.citrusframework.citrus.endpoint.direct.DirectEndpointConfiguration;

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
