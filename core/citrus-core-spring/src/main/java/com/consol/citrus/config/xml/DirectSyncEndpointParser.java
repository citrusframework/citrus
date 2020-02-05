package com.consol.citrus.config.xml;

import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.endpoint.direct.DirectSyncEndpoint;
import com.consol.citrus.endpoint.direct.DirectSyncEndpointConfiguration;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for synchronous message queue endpoint components.
 *
 * @author Christoph Deppisch
 * @since 3.0
 */
public class DirectSyncEndpointParser extends AbstractDirectEndpointParser {

    @Override
    protected Class<? extends Endpoint> getEndpointClass() {
        return DirectSyncEndpoint.class;
    }

    @Override
    protected Class<? extends EndpointConfiguration> getEndpointConfigurationClass() {
        return DirectSyncEndpointConfiguration.class;
    }

    @Override
    protected void parseEndpointConfiguration(BeanDefinitionBuilder endpointConfiguration, Element element, ParserContext parserContext) {
        super.parseEndpointConfiguration(endpointConfiguration, element, parserContext);

        BeanDefinitionParserUtils.setPropertyReference(endpointConfiguration,
                element.getAttribute("message-correlator"), "correlator");

        BeanDefinitionParserUtils.setPropertyValue(endpointConfiguration,
                element.getAttribute("polling-interval"), "pollingInterval");
    }
}
