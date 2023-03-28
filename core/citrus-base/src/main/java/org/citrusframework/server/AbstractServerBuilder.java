/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.server;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.endpoint.EndpointAdapter;

/**
 * @author Christoph Deppisch
 */
public abstract class AbstractServerBuilder<T extends AbstractServer, B extends AbstractServerBuilder<T, B>> extends AbstractEndpointBuilder<T> {

    private final B self;

    protected AbstractServerBuilder() {
        this.self = (B) this;
    }

    /**
     * Sets the autoStart property.
     * @param autoStart
     * @return
     */
    public B autoStart(boolean autoStart) {
        getEndpoint().setAutoStart(autoStart);
        return self;
    }

    /**
     * Sets the endpoint adapter.
     * @param endpointAdapter
     * @return
     */
    public B endpointAdapter(EndpointAdapter endpointAdapter) {
        getEndpoint().setEndpointAdapter(endpointAdapter);
        return self;
    }

    /**
     * Sets the debug logging enabled flag.
     * @param enabled
     * @return
     */
    public B debugLogging(boolean enabled) {
        getEndpoint().setDebugLogging(enabled);
        return self;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public B timeout(long timeout) {
        if (getEndpoint().getEndpointConfiguration() != null) {
            getEndpoint().getEndpointConfiguration().setTimeout(timeout);
        }

        getEndpoint().setDefaultTimeout(timeout);
        return self;
    }
}
