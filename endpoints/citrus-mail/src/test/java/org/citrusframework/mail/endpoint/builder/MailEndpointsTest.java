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

import java.util.Map;

import org.citrusframework.endpoint.EndpointBuilder;
import org.junit.jupiter.api.Assertions;
import org.citrusframework.mail.client.MailClientBuilder;
import org.citrusframework.mail.server.MailServerBuilder;
import org.junit.jupiter.api.Test;

public class MailEndpointsTest {

    @Test
    public void shouldLookupEndpoints() {
        Map<String, EndpointBuilder<?>> endpointBuilders = EndpointBuilder.lookup();
        Assertions.assertTrue(endpointBuilders.containsKey("mail"));
        Assertions.assertTrue(endpointBuilders.containsKey("mail.client"));
        Assertions.assertTrue(endpointBuilders.containsKey("mail.server"));
    }

    @Test
    public void shouldLookupEndpoint() {
        Assertions.assertTrue(EndpointBuilder.lookup("mail").isPresent());
        Assertions.assertEquals(MailEndpointBuilder.class, EndpointBuilder.lookup("mail").get().getClass());
        Assertions.assertTrue(EndpointBuilder.lookup("mail.client").isPresent());
        Assertions.assertEquals(MailClientBuilder.class, EndpointBuilder.lookup("mail.client").get().getClass());
        Assertions.assertTrue(EndpointBuilder.lookup("mail.server").isPresent());
        Assertions.assertEquals(MailServerBuilder.class, EndpointBuilder.lookup("mail.server").get().getClass());
    }
}
