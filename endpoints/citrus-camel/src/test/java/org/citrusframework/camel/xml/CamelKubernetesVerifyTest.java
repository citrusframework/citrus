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

package org.citrusframework.camel.xml;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.camel.CamelSettings;
import org.citrusframework.camel.actions.CamelKubernetesVerifyAction;
import org.citrusframework.camel.jbang.CamelJBang;
import org.citrusframework.camel.jbang.KubernetesPlugin;
import org.citrusframework.jbang.JBangSupport;
import org.citrusframework.jbang.ProcessAndOutput;
import org.citrusframework.xml.XmlTestLoader;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CamelKubernetesVerifyTest extends AbstractXmlActionTest {

    @Mock
    private CamelJBang camelJBang;

    @Mock
    private JBangSupport camelApp;

    @Mock
    private KubernetesPlugin k8sPlugin;

    @Mock
    private ProcessAndOutput pao;

    @Mock
    private Process process;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(camelJBang.camelApp()).thenReturn(camelApp);
        when(camelJBang.kubernetes()).thenReturn(k8sPlugin);
        when(pao.getProcess()).thenReturn(process);
    }

    @Test
    public void shouldLoadCamelActions() throws Exception {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/camel/xml/camel-jbang-kubernetes-verify-test.xml");

        CamelContext citrusCamelContext = new DefaultCamelContext();
        citrusCamelContext.start();

        context.getReferenceResolver().bind(CamelSettings.getContextName(), citrusCamelContext);
        context.getReferenceResolver().bind("camelContext", citrusCamelContext);

        context.getReferenceResolver().bind("camel-jbang", camelJBang);

        when(k8sPlugin.logs("--name", "my-route")).thenReturn(pao);
        when(k8sPlugin.logs("--label", "app=camel")).thenReturn(pao);
        when(process.exitValue()).thenReturn(0);
        when(pao.getOutput()).thenReturn("Camel rocks!");

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "CamelJBangKubernetesVerifyTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 2L);
        Assert.assertEquals(result.getTestAction(0).getClass(), CamelKubernetesVerifyAction.class);
        Assert.assertEquals(result.getTestAction(0).getName(), "kubernetes-verify-integration");

        verify(k8sPlugin).logs("--name", "my-route");
        verify(k8sPlugin).logs("--label", "app=camel");
    }
}
