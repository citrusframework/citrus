/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.mail.server;

import com.consol.citrus.endpoint.AbstractEndpointBuilder;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.mail.message.MailMessageConverter;
import com.consol.citrus.mail.model.MailMarshaller;

import java.util.Properties;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class MailServerBuilder extends AbstractEndpointBuilder<MailServer> {

    /** Endpoint target */
    private MailServer endpoint = new MailServer();

    @Override
    protected MailServer getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the port property.
     * @param port
     * @return
     */
    public MailServerBuilder port(int port) {
        endpoint.setPort(port);
        return this;
    }

    /**
     * Sets the autoStart property.
     * @param autoStart
     * @return
     */
    public MailServerBuilder autoStart(boolean autoStart) {
        endpoint.setAutoStart(autoStart);
        return this;
    }

    /**
     * Sets the mail marshaller.
     * @param marshaller
     * @return
     */
    public MailServerBuilder marshaller(MailMarshaller marshaller) {
        endpoint.setMarshaller(marshaller);
        return this;
    }

    /**
     * Sets the Java mail properties.
     * @param javaMailProperties
     * @return
     */
    public MailServerBuilder javaMailProperties(Properties javaMailProperties) {
        endpoint.setJavaMailProperties(javaMailProperties);
        return this;
    }

    /**
     * Enables/disables auto accept.
     * @param autoAccept
     * @return
     */
    public MailServerBuilder autoAccept(boolean autoAccept) {
        endpoint.setAutoAccept(autoAccept);
        return this;
    }

    /**
     * Enables/disables split multipart.
     * @param splitMultipart
     * @return
     */
    public MailServerBuilder splitMultipart(boolean splitMultipart) {
        endpoint.setSplitMultipart(splitMultipart);
        return this;
    }

    /**
     * Sets the message converter.
     * @param messageConverter
     * @return
     */
    public MailServerBuilder messageConverter(MailMessageConverter messageConverter) {
        endpoint.setMessageConverter(messageConverter);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public MailServerBuilder timeout(long timeout) {
        endpoint.setDefaultTimeout(timeout);
        return this;
    }

    /**
     * Sets the endpoint adapter.
     * @param endpointAdapter
     * @return
     */
    public MailServerBuilder endpointAdapter(EndpointAdapter endpointAdapter) {
        endpoint.setEndpointAdapter(endpointAdapter);
        return this;
    }

    /**
     * Sets the debug logging enabled flag.
     * @param enabled
     * @return
     */
    public MailServerBuilder debugLogging(boolean enabled) {
        endpoint.setDebugLogging(enabled);
        return this;
    }
}
