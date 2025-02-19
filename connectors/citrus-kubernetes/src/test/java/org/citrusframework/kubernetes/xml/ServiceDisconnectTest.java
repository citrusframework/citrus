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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServiceDisconnectTest extends AbstractXmlActionTest {

    @Mock
    private LocalPortForward portForward;

    @Mock
    private HttpClient serviceClient;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldLoadKubernetesActions() throws IOException {
        context.getReferenceResolver().bind("my-service:port-forward", portForward);
        context.getReferenceResolver().bind("my-service.client", serviceClient);

        when(portForward.isAlive()).thenReturn(true);

        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/xml/service-disconnect-test.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "ServiceDisconnectTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 1L);
        Assert.assertEquals(result.getTestAction(0).getClass(), ServiceDisconnectAction.class);
        Assert.assertTrue(result.getTestResult().isSuccess());

        verify(portForward).isAlive();
        verify(portForward).close();
    }
}
