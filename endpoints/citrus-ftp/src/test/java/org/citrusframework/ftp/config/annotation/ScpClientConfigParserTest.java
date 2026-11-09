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

package org.citrusframework.ftp.config.annotation;

import org.citrusframework.TestActor;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.ftp.client.ScpClient;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class ScpClientConfigParserTest extends AbstractUnitTest {

    @CitrusEndpoint(name = "scpClient1")
    @ScpClientConfig(port = 22221)
    private ScpClient scpClient1;

    @CitrusEndpoint
    @ScpClientConfig(host = "localhost",
            port=22222,
            portOption = "-p",
            username="user",
            password="consol",
            privateKeyPath="classpath:org/citrusframework/scp/citrus.priv",
            privateKeyPassword="consol",
            timeout=10000L)
    private ScpClient scpClient2;

    @CitrusEndpoint
    @ScpClientConfig(host = "localhost",
            port=22223,
            errorStrategy = ErrorHandlingStrategy.THROWS_EXCEPTION,
            correlator="replyMessageCorrelator")
    private ScpClient scpClient3;

    @CitrusEndpoint
    @ScpClientConfig(host = "localhost",
            port=22224,
            pollingInterval=250,
            actor="testActor")
    private ScpClient scpClient4;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private MessageCorrelator messageCorrelator;
    @Mock
    private TestActor testActor;

    @BeforeEach
    @Override
    public void prepareTest() {
        super.prepareTest();
        lenient().when(referenceResolver.resolve("replyMessageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testScpClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st scp client
        Assertions.assertEquals("localhost", scpClient1.getEndpointConfiguration().getHost());
        Assertions.assertEquals("-P", scpClient1.getEndpointConfiguration().getPortOption());
        Assertions.assertEquals(22221, scpClient1.getEndpointConfiguration().getPort());
        Assertions.assertEquals(DefaultMessageCorrelator.class, scpClient1.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertEquals(ErrorHandlingStrategy.PROPAGATE, scpClient1.getEndpointConfiguration().getErrorHandlingStrategy());
        Assertions.assertEquals(5000L, scpClient1.getEndpointConfiguration().getTimeout());
        Assertions.assertTrue(scpClient1.getEndpointConfiguration().isAutoReadFiles());
        Assertions.assertNull(scpClient1.getEndpointConfiguration().getPrivateKeyPath());
        Assertions.assertNull(scpClient1.getEndpointConfiguration().getPrivateKeyPassword());
        Assertions.assertFalse(scpClient1.getEndpointConfiguration().isStrictHostChecking());
        Assertions.assertNull(scpClient1.getEndpointConfiguration().getKnownHosts());

        // 2nd scp client
        Assertions.assertEquals("localhost", scpClient2.getEndpointConfiguration().getHost());
        Assertions.assertEquals("-p", scpClient2.getEndpointConfiguration().getPortOption());
        Assertions.assertEquals(22222, scpClient2.getEndpointConfiguration().getPort());
        Assertions.assertEquals(DefaultMessageCorrelator.class, scpClient2.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertEquals("user", scpClient2.getEndpointConfiguration().getUser());
        Assertions.assertEquals("consol", scpClient2.getEndpointConfiguration().getPassword());
        Assertions.assertEquals("classpath:org/citrusframework/scp/citrus.priv", scpClient2.getEndpointConfiguration().getPrivateKeyPath());
        Assertions.assertEquals("consol", scpClient2.getEndpointConfiguration().getPrivateKeyPassword());
        Assertions.assertEquals(10000L, scpClient2.getEndpointConfiguration().getTimeout());

        // 3rd scp client
        Assertions.assertEquals("localhost", scpClient3.getEndpointConfiguration().getHost());
        Assertions.assertEquals(22223, scpClient3.getEndpointConfiguration().getPort());
        Assertions.assertNotNull(scpClient3.getEndpointConfiguration().getCorrelator());
        Assertions.assertEquals(messageCorrelator, scpClient3.getEndpointConfiguration().getCorrelator());
        Assertions.assertEquals(ErrorHandlingStrategy.THROWS_EXCEPTION, scpClient3.getEndpointConfiguration().getErrorHandlingStrategy());

        // 4th scp client
        Assertions.assertNotNull(scpClient4.getActor());
        Assertions.assertEquals(testActor, scpClient4.getActor());
        Assertions.assertEquals(22224, scpClient4.getEndpointConfiguration().getPort());
        Assertions.assertEquals(250L, scpClient4.getEndpointConfiguration().getPollingInterval());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("scp.client").isPresent());
    }
}
