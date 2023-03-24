/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.vertx.endpoint;

import org.citrusframework.endpoint.AbstractPollableEndpointConfiguration;
import org.citrusframework.vertx.message.VertxMessageConverter;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class VertxEndpointConfiguration extends AbstractPollableEndpointConfiguration {

    /** Vert.x cluster hostname */
    private String host = "localhost";

    /** Vert.x cluster port */
    private int port = 0;

    /** Address on the event bus */
    private String address;

    /** Should use publish subscribe */
    private boolean pubSubDomain = false;

    /** Message converter */
    private VertxMessageConverter messageConverter = new VertxMessageConverter();

    /**
     * Gets the address on the vert.x event bus.
     * @return
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address on the vert.x event bus.
     * @param address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Gets the cluster hostname.
     * @return
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the cluster hostname.
     * @param host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Gets the cluster port.
     * @return
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the cluster port.
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Does domain use publish subscribe communication.
     * @return the pubSubDomain
     */
    public boolean isPubSubDomain() {
        return pubSubDomain;
    }

    /**
     * Sets if domain uses publish subscribe communication.
     * @param pubSubDomain the pubSubDomain to set
     */
    public void setPubSubDomain(boolean pubSubDomain) {
        this.pubSubDomain = pubSubDomain;
    }

    /**
     * Gets the message converter.
     * @return
     */
    public VertxMessageConverter getMessageConverter() {
        return messageConverter;
    }

    /**
     * Sets the message converter.
     * @param messageConverter
     */
    public void setMessageConverter(VertxMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }
}
