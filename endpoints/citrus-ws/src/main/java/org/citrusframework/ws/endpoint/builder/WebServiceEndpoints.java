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

package org.citrusframework.ws.endpoint.builder;

import org.citrusframework.endpoint.builder.ClientServerEndpointBuilder;
import org.citrusframework.ws.client.WebServiceClientBuilder;
import org.citrusframework.ws.server.WebServiceServerBuilder;

public final class WebServiceEndpoints extends ClientServerEndpointBuilder<WebServiceClientBuilder, WebServiceServerBuilder>  {

    /**
     * Private constructor setting the client and server builder implementation.
     */
    private WebServiceEndpoints() {
        super(new WebServiceClientBuilder(), new WebServiceServerBuilder());
    }

    /**
     * Static entry method for Soap client and server endpoints.
     * @return
     */
    public static WebServiceEndpoints soap() {
        return new WebServiceEndpoints();
    }
}
