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

package org.citrusframework.ssh.endpoint.builder;

import java.util.Map;

import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.ssh.client.SshClientBuilder;
import org.citrusframework.ssh.server.SshServerBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SshEndpointsTest {

    @Test
    public void shouldLookupEndpoints() {
        Map<String, EndpointBuilder<?>> endpointBuilders = EndpointBuilder.lookup();
        Assertions.assertTrue(endpointBuilders.containsKey("ssh.client"));
        Assertions.assertTrue(endpointBuilders.containsKey("ssh.server"));
    }

    @Test
    public void shouldLookupEndpoint() {
        Assertions.assertTrue(EndpointBuilder.lookup("ssh.client").isPresent());
        Assertions.assertEquals(SshClientBuilder.class, EndpointBuilder.lookup("ssh.client").get().getClass());
        Assertions.assertTrue(EndpointBuilder.lookup("ssh.server").isPresent());
        Assertions.assertEquals(SshServerBuilder.class, EndpointBuilder.lookup("ssh.server").get().getClass());
    }
}
