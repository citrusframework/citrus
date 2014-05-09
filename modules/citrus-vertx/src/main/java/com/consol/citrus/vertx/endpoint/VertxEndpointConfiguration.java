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
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class VertxEndpointConfiguration extends AbstractEndpointConfiguration {

    /** Vert.x instance */
    private Vertx vertx = VertxFactory.newVertx();

    /** Address on the event bus */
    private String address;

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
     * Gets the vert.x instance.
     * @return
     */
    public Vertx getVertx() {
        return vertx;
    }

    /**
     * Sets the vert.x instance.
     * @param vertx
     */
    public void setVertx(Vertx vertx) {
        this.vertx = vertx;
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
}
