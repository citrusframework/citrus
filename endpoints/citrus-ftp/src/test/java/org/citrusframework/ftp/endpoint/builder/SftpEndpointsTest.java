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
import org.citrusframework.ftp.client.SftpClientBuilder;
import org.citrusframework.ftp.server.SftpServerBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SftpEndpointsTest {

    @Test
    public void shouldLookupEndpoints() {
        Map<String, EndpointBuilder<?>> endpointBuilders = EndpointBuilder.lookup();
        Assert.assertTrue(endpointBuilders.containsKey("sftp.client"));
        Assert.assertTrue(endpointBuilders.containsKey("sftp.server"));
    }

    @Test
    public void shouldLookupEndpoint() {
        Assert.assertTrue(EndpointBuilder.lookup("sftp.client").isPresent());
        Assert.assertEquals(EndpointBuilder.lookup("sftp.client").get().getClass(), SftpClientBuilder.class);
        Assert.assertTrue(EndpointBuilder.lookup("sftp.server").isPresent());
        Assert.assertEquals(EndpointBuilder.lookup("sftp.server").get().getClass(), SftpServerBuilder.class);
    }
}
