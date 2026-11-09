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

package org.citrusframework.ftp.endpoint.builder;

import java.util.Map;

import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.ftp.client.FtpClientBuilder;
import org.citrusframework.ftp.server.FtpServerBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FtpEndpointsTest {

    @Test
    public void shouldLookupEndpoints() {
        Map<String, EndpointBuilder<?>> endpointBuilders = EndpointBuilder.lookup();
        Assertions.assertTrue(endpointBuilders.containsKey("ftp"));
        Assertions.assertTrue(endpointBuilders.containsKey("ftp.client"));
        Assertions.assertTrue(endpointBuilders.containsKey("ftp.server"));
    }

    @Test
    public void shouldLookupEndpoint() {
        Assertions.assertTrue(EndpointBuilder.lookup("ftp").isPresent());
        Assertions.assertEquals(FtpEndpointBuilder.class, EndpointBuilder.lookup("ftp").get().getClass());
        Assertions.assertTrue(EndpointBuilder.lookup("ftp.client").isPresent());
        Assertions.assertEquals(FtpClientBuilder.class, EndpointBuilder.lookup("ftp.client").get().getClass());
        Assertions.assertTrue(EndpointBuilder.lookup("ftp.server").isPresent());
        Assertions.assertEquals(FtpServerBuilder.class, EndpointBuilder.lookup("ftp.server").get().getClass());
    }
}
