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

package org.citrusframework.mail.endpoint.builder;

import org.citrusframework.endpoint.builder.ClientServerEndpointBuilder;
import org.citrusframework.mail.client.MailClientBuilder;
import org.citrusframework.mail.server.MailServerBuilder;

public final class MailEndpoints extends ClientServerEndpointBuilder<MailClientBuilder, MailServerBuilder> {
    /**
     * Private constructor setting the client and server builder implementation.
     */
    private MailEndpoints() {
        super(new MailClientBuilder(), new MailServerBuilder());
    }

    /**
     * Static entry method for mail endpoints.
     * @return
     */
    public static MailEndpoints mail() {
        return new MailEndpoints();
    }
}
