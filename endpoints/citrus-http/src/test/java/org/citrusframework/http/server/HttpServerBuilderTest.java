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

package org.citrusframework.http.server;

import org.citrusframework.http.message.HttpMessageConverter;
import org.citrusframework.http.security.HttpSecureConnection;
import org.citrusframework.spi.SimpleReferenceResolver;
import org.eclipse.jetty.server.ServerConnector;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HttpServerBuilderTest {

    @Test
    public void shouldResolveSecuredConnectionByName() {
        ServerConnector serverConnector = mock(ServerConnector.class);
        HttpSecureConnection secureConnection = mock(HttpSecureConnection.class);
        when(secureConnection.getServerConnector(9443)).thenReturn(serverConnector);

        SimpleReferenceResolver referenceResolver = new SimpleReferenceResolver();
        referenceResolver.bind("mySsl", secureConnection);
        referenceResolver.bind("myConverter", new HttpMessageConverter());

        HttpServerBuilder builder = new HttpServerBuilder();
        builder.setReferenceResolver(referenceResolver);
        builder.setMessageConverter("myConverter");
        builder.setSecurePort(9443);
        builder.setSecuredConnection("mySsl");

        HttpServer server = builder.build();

        verify(secureConnection).getServerConnector(9443);
        assertThat(server.getConnector()).isSameAs(serverConnector);
    }
}
