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

package org.citrusframework.server;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaProperty;

public abstract class AbstractServerBuilder<T extends AbstractServer, B extends AbstractServerBuilder<T, B>> extends AbstractEndpointBuilder<T> {

    private final B self;

    private String endpointAdapter;

    protected AbstractServerBuilder() {
        this.self = (B) this;
    }

    @Override
    public T build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(endpointAdapter)) {
                endpointAdapter(referenceResolver.resolve(endpointAdapter, EndpointAdapter.class));
            }
        }
        return super.build();
    }

    /**
     * Sets the autoStart property.
     */
    public B autoStart(boolean autoStart) {
        getEndpoint().setAutoStart(autoStart);
        return self;
    }

    @SchemaProperty(description = "When enabled the server is automatically started after creation.")
    public void setAutoStart(boolean autoStart) {
        autoStart(autoStart);
    }

    /**
     * Sets the endpoint adapter.
     */
    public B endpointAdapter(EndpointAdapter endpointAdapter) {
        getEndpoint().setEndpointAdapter(endpointAdapter);
        return self;
    }

    @SchemaProperty(advanced = true, description = "Sets a custom endpoint adapter to handle requests.")
    public void setEndpointAdapter(String endpointAdapter) {
        this.endpointAdapter = endpointAdapter;
    }

    /**
     * Sets the debug logging enabled flag.
     */
    public B debugLogging(boolean enabled) {
        getEndpoint().setDebugLogging(enabled);
        return self;
    }

    @SchemaProperty(advanced = true, description = "When enabled the server prints debug logging output.")
    public void setDebugLogging(boolean enabled) {
        debugLogging(enabled);
    }

    /**
     * Sets the default timeout.
     */
    public B timeout(long timeout) {
        if (getEndpoint().getEndpointConfiguration() != null) {
            getEndpoint().getEndpointConfiguration().setTimeout(timeout);
        }

        getEndpoint().setDefaultTimeout(timeout);
        return self;
    }

    @SchemaProperty(description = "The server timeout.", defaultValue = "5000")
    public void setTimeout(long timeout) {
        timeout(timeout);
    }
}
