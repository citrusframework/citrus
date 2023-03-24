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

package org.citrusframework.endpoint.adapter;

import org.citrusframework.endpoint.AbstractEndpointAdapter;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * Static endpoint adapter always responds with static response message. No endpoint is provided as this is a
 * static endpoint adapter. Clients trying to get endpoint for interaction will receive runtime exception.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public abstract class StaticEndpointAdapter extends AbstractEndpointAdapter {

    @Override
    public Endpoint getEndpoint() {
        throw new CitrusRuntimeException(String.format("Unable to create endpoint for static endpoint adapter type '%s'", getClass()));
    }

    @Override
    public EndpointConfiguration getEndpointConfiguration() {
        throw new CitrusRuntimeException(String.format("Unable to provide endpoint configuration for static endpoint adapter type '%s'", getClass()));
    }
}
