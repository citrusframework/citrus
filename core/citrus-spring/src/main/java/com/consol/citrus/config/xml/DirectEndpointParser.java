package com.consol.citrus.config.xml;

import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.endpoint.direct.DirectEndpoint;
import com.consol.citrus.endpoint.direct.DirectEndpointConfiguration;

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
