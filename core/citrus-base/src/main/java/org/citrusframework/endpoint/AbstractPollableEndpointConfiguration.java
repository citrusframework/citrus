/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.endpoint;

/**
 * Abstract pollable endpoint configuration adds polling interval settings.
 *
 * @author Christoph Deppisch
 * @since 2.1
 */
public class AbstractPollableEndpointConfiguration extends AbstractEndpointConfiguration implements PollableEndpointConfiguration {

    /** Polling interval when waiting for synchronous reply message to arrive */
    private long pollingInterval = 500;

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
