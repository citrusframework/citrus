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

package org.citrusframework.mail.client;

import java.util.Map;
import java.util.StringTokenizer;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpointComponent;
import org.citrusframework.endpoint.Endpoint;

import static java.lang.Integer.parseInt;

/**
 * Component creates proper mail client from endpoint uri resource and parameters.
 *
 * @since 1.4.1
 */
public class MailEndpointComponent extends AbstractEndpointComponent {

    /**
     * Default constructor using the name for this component.
     */
    public MailEndpointComponent() {
        super("mail");
    }

    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context) {
        MailClient client = new MailClient();

        if (resourcePath.contains(":")) {
            StringTokenizer tok = new StringTokenizer(resourcePath, ":");

            client.getEndpointConfiguration().setHost(tok.nextToken());

            if (tok.hasMoreTokens()) {
                client.getEndpointConfiguration().setPort(parseInt(tok.nextToken()));
            }
        } else {
            client.getEndpointConfiguration().setHost(resourcePath);
        }

        enrichEndpointConfiguration(client.getEndpointConfiguration(), parameters, context);
        return client;
    }
}
