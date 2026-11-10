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

package org.citrusframework.ssh.config.annotation;

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.ssh.client.SshClient;
import org.citrusframework.ssh.message.SshMessageConverter;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class SshClientConfigParserTest extends AbstractUnitTest {

    @CitrusEndpoint(name = "sshClient1")
    @SshClientConfig(user="citrus")
    private SshClient sshClient1;

    @CitrusEndpoint
    @SshClientConfig(host="dev7",
            port=10022,
            user="foo",
            password="bar",
            privateKeyPath="classpath:org/citrusframework/ssh/citrus.priv",
            privateKeyPassword="consol",
            strictHostChecking=true,
            commandTimeout=10000,
            connectionTimeout=5000,
            knownHosts="classpath:org/citrusframework/ssh/known_hosts",
            timeout=10000L,
            messageConverter="sshMessageConverter")
    private SshClient sshClient2;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private SshMessageConverter messageConverter;
    @Mock
    private TestActor testActor;

    @BeforeEach
    @Override
    public void prepareTest() {
        super.prepareTest();
        lenient().when(referenceResolver.resolve("sshMessageConverter", SshMessageConverter.class)).thenReturn(messageConverter);
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testSshClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st client
        Assertions.assertEquals("localhost", sshClient1.getEndpointConfiguration().getHost());
        Assertions.assertEquals(2222, sshClient1.getEndpointConfiguration().getPort());
        Assertions.assertEquals("citrus", sshClient1.getEndpointConfiguration().getUser());
        Assertions.assertNull(sshClient1.getEndpointConfiguration().getPassword());
        Assertions.assertNull(sshClient1.getEndpointConfiguration().getPrivateKeyPath());
        Assertions.assertNull(sshClient1.getEndpointConfiguration().getPrivateKeyPassword());
        Assertions.assertNull(sshClient1.getEndpointConfiguration().getKnownHosts());
        Assertions.assertEquals(1000 * 60 * 5, sshClient1.getEndpointConfiguration().getCommandTimeout());
        Assertions.assertEquals(1000 * 60, sshClient1.getEndpointConfiguration().getConnectionTimeout());
        Assertions.assertFalse(sshClient1.getEndpointConfiguration().isStrictHostChecking());
        Assertions.assertNotNull(sshClient1.getEndpointConfiguration().getMessageConverter());

        // 2nd client
        Assertions.assertEquals("dev7", sshClient2.getEndpointConfiguration().getHost());
        Assertions.assertEquals(10022, sshClient2.getEndpointConfiguration().getPort());
        Assertions.assertEquals("foo", sshClient2.getEndpointConfiguration().getUser());
        Assertions.assertEquals("bar", sshClient2.getEndpointConfiguration().getPassword());
        Assertions.assertEquals("classpath:org/citrusframework/ssh/citrus.priv", sshClient2.getEndpointConfiguration().getPrivateKeyPath());
        Assertions.assertEquals("consol", sshClient2.getEndpointConfiguration().getPrivateKeyPassword());
        Assertions.assertEquals("classpath:org/citrusframework/ssh/known_hosts", sshClient2.getEndpointConfiguration().getKnownHosts());
        Assertions.assertEquals(10000, sshClient2.getEndpointConfiguration().getCommandTimeout());
        Assertions.assertEquals(5000, sshClient2.getEndpointConfiguration().getConnectionTimeout());
        Assertions.assertTrue(sshClient2.getEndpointConfiguration().isStrictHostChecking());
        Assertions.assertEquals(messageConverter, sshClient2.getEndpointConfiguration().getMessageConverter());
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser<?, ?>> validators = AnnotationConfigParser.lookup();
        Assertions.assertEquals(4L, validators.size());
        Assertions.assertNotNull(validators.get("direct.async"));
        Assertions.assertEquals(DirectEndpointConfigParser.class, validators.get("direct.async").getClass());
        Assertions.assertNotNull(validators.get("direct.sync"));
        Assertions.assertEquals(DirectSyncEndpointConfigParser.class, validators.get("direct.sync").getClass());
        Assertions.assertNotNull(validators.get("ssh.client"));
        Assertions.assertEquals(SshClientConfigParser.class, validators.get("ssh.client").getClass());
        Assertions.assertNotNull(validators.get("ssh.server"));
        Assertions.assertEquals(SshServerConfigParser.class, validators.get("ssh.server").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("ssh.client").isPresent());
    }
}
