package com.consol.citrus.admin.converter.endpoint;

import com.consol.citrus.admin.model.EndpointData;
import com.consol.citrus.model.config.core.JmsEndpoint;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 1.3.1
 */
@Component
public class JmsEndpointConverter extends AbstractEndpointConverter<JmsEndpoint> {

    @Override
    public EndpointData convert(JmsEndpoint definition) {
        EndpointData endpointData = new EndpointData("jms");

        endpointData.setName(definition.getId());

        if (StringUtils.hasText(definition.getDestinationName())) {
            endpointData.add(property("destinationName", "Destination", definition));
        } else {
            endpointData.add(property("destination", definition.getDestination(), definition));
        }

        endpointData.add(property("connectionFactory", definition));
        endpointData.add(property("jmsTemplate", definition));
        endpointData.add(property("pubSubDomain", definition, "false"));

        addEndpointProperties(endpointData, definition);

        return endpointData;
    }

    @Override
    public Class<JmsEndpoint> getModelClass() {
        return JmsEndpoint.class;
    }
}
