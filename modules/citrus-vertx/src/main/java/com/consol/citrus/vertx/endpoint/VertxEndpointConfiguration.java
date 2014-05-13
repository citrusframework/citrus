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

package com.consol.citrus.vertx.endpoint;

import com.consol.citrus.endpoint.AbstractEndpointConfiguration;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class VertxEndpointConfiguration extends AbstractEndpointConfiguration {

    /** Vert.x cluster hostname */
    private String clusterHost = "localhost";

    /** Vert.x cluster port */
    private int clusterPort = -1;

    /** Address on the event bus */
    private String address;

    /** Should use publish subscribe */
    private boolean pubSubDomain = false;

    /** Polling interval when waiting for synchronous reply message to arrive */
    private long pollingInterval = 500;

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
     * Gets the pollingInterval.
     * @return the pollingInterval the pollingInterval to get.
     */
    public long getPollingInterval() {
        return pollingInterval;
    }

    /**
     * Sets the pollingInterval.
     * @param pollingInterval the pollingInterval to set
     */
    public void setPollingInterval(long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    /**
     * Gets the cluster hostname.
     * @return
     */
    public String getClusterHost() {
        return clusterHost;
    }

    /**
     * Sets the cluster hostname.
     * @param clusterHost
     */
    public void setClusterHost(String clusterHost) {
        this.clusterHost = clusterHost;
    }

    /**
     * Gets the cluster port.
     * @return
     */
    public int getClusterPort() {
        return clusterPort;
    }

    /**
     * Sets the cluster port.
     * @param clusterPort
     */
    public void setClusterPort(int clusterPort) {
        this.clusterPort = clusterPort;
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
}
