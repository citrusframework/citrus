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

package org.citrusframework.knative.xml;

import java.util.HashMap;

import io.fabric8.knative.client.KnativeClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesCrudDispatcher;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import io.fabric8.mockwebserver.Context;
import io.fabric8.mockwebserver.MockWebServer;
import org.citrusframework.Citrus;
import org.citrusframework.CitrusContext;
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.context.StaticTestContextFactory;
import org.citrusframework.context.TestContext;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.xml.XmlTestLoader;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class AbstractXmlActionTest extends AbstractTestNGUnitTest {

    protected Citrus citrus;

    @Mock
    protected CitrusContext citrusContext;

    private final KubernetesMockServer k8sServer = new KubernetesMockServer(new Context(), new MockWebServer(),
            new HashMap<>(), new KubernetesCrudDispatcher(), false);

    protected KubernetesClient k8sClient;

    protected KnativeClient knativeClient;

    @BeforeClass
    public void setupMocks() {
        MockitoAnnotations.openMocks(this);
        citrus = CitrusInstanceManager.newInstance(() -> citrusContext);

        k8sServer.init();
        k8sClient = k8sServer.createClient();
        knativeClient = k8sClient.adapt(KnativeClient.class);
    }

    @AfterClass(alwaysRun = true)
    public void stop() {
        k8sServer.destroy();
    }

    @Override
    protected TestContext createTestContext() {
        TestContext context = super.createTestContext();
        when(citrusContext.getReferenceResolver()).thenReturn(context.getReferenceResolver());
        when(citrusContext.getMessageValidatorRegistry()).thenReturn(context.getMessageValidatorRegistry());
        when(citrusContext.getTestContextFactory()).thenReturn(new StaticTestContextFactory(context));
        doAnswer(invocationOnMock -> {
            CitrusAnnotations.parseConfiguration(invocationOnMock.getArgument(0, Object.class), citrusContext);
            return null;
        }).when(citrusContext).parseConfiguration((Object) any());
        doAnswer(invocationOnMock-> {
            context.getReferenceResolver().bind(invocationOnMock.getArgument(0), invocationOnMock.getArgument(1));
            return null;
        }).when(citrusContext).addComponent(anyString(), any());
        CitrusAnnotations.injectAll(this, citrus, context);

        context.getReferenceResolver().bind("k8sClient", k8sClient);
        context.getReferenceResolver().bind("knativeClient", knativeClient);

        return context;
    }

    protected XmlTestLoader createTestLoader(String sourcePath) {
        XmlTestLoader testLoader = new XmlTestLoader(this.getClass(), "Test", this.getClass().getPackageName());
        CitrusAnnotations.injectAll(testLoader, citrus, context);
        CitrusAnnotations.injectTestRunner(testLoader, new DefaultTestCaseRunner(context));
        testLoader.setSource(sourcePath);

        return testLoader;
    }
}
