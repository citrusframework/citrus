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

package org.citrusframework.kubernetes.xml;

import java.io.IOException;

import io.fabric8.kubernetes.client.LocalPortForward;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.kubernetes.actions.ServiceDisconnectAction;
import org.citrusframework.xml.XmlTestLoader;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ServiceDisconnectTest extends AbstractXmlActionTest {

    @Mock
    private LocalPortForward portForward;

    @Mock
    private HttpClient serviceClient;


    @Test
    public void shouldLoadKubernetesActions() throws IOException {
        context.getReferenceResolver().bind("my-service:port-forward", portForward);
        context.getReferenceResolver().bind("my-service.client", serviceClient);

        lenient().when(portForward.isAlive()).thenReturn(true);

        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/xml/service-disconnect.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("ServiceDisconnectTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(1L, result.getActionCount());
        Assertions.assertEquals(ServiceDisconnectAction.class, result.getTestAction(0).getClass());
        Assertions.assertTrue(result.getTestResult().isSuccess());

        verify(portForward).isAlive();
        verify(portForward).close();
    }
}
