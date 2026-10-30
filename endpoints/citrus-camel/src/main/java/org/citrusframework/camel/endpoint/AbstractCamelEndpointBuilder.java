/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.camel.endpoint;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.citrusframework.base.endpoint.AbstractEndpointBuilder;
import org.citrusframework.camel.message.CamelMessageConverter;
import org.citrusframework.util.StringUtils;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.api.yaml.SchemaType;

@SchemaType(module = "citrus-camel")
@XmlType(name = "", propOrder = {})
@SuppressWarnings("unchecked")
public abstract class AbstractCamelEndpointBuilder<T extends CamelEndpoint, B extends AbstractCamelEndpointBuilder<T, B>> extends AbstractEndpointBuilder<T> {

    private String camelContext;
    private String messageConverter;

    @Override
    public T build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(camelContext)) {
                camelContext(referenceResolver.resolve(camelContext, CamelContext.class));
            }

            if (StringUtils.hasText(messageConverter)) {
                messageConverter(referenceResolver.resolve(messageConverter, CamelMessageConverter.class));
            }
        }

        return super.build();
    }

    /**
     * Sets the endpointUri property.
     */
    public B endpointUri(String endpointUri) {
        getEndpoint().getEndpointConfiguration().setEndpointUri(endpointUri);
        return (B) this;
    }

    @SchemaProperty(description = "The Camel endpoint uri.")
    @XmlAttribute(name = "endpoint-uri")
    public void setEndpointUri(String endpointUri) {
        endpointUri(endpointUri);
    }

    /**
     * Sets the endpoint property.
     */
    public B endpoint(Endpoint camelEndpoint) {
        getEndpoint().getEndpointConfiguration().setEndpoint(camelEndpoint);
        return (B) this;
    }

    /**
     * Sets the camelContext property.
     */
    public B camelContext(CamelContext camelContext) {
        getEndpoint().getEndpointConfiguration().setCamelContext(camelContext);
        return (B) this;
    }

    @SchemaProperty(description = "The Camel context to use.")
    @XmlAttribute(name = "camel-context")
    public void setCamelContext(String camelContext) {
        this.camelContext = camelContext;
    }

    /**
     * Sets the messageConverter property.
     */
    public B messageConverter(CamelMessageConverter messageConverter) {
        getEndpoint().getEndpointConfiguration().setMessageConverter(messageConverter);
        return (B) this;
    }

    @SchemaProperty(advanced = true, description = "Sets the message converter as a bean reference.")
    @XmlAttribute(name = "message-converter")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Sets the default timeout.
     */
    public B timeout(long timeout) {
        getEndpoint().getEndpointConfiguration().setTimeout(timeout);
        return (B) this;
    }

    @SchemaProperty(description = "The endpoint timeout when waiting for messages.")
    @XmlAttribute
    public void setTimeout(long timeout) {
        timeout(timeout);
    }
}
