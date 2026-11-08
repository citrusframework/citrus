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
import org.citrusframework.camel.actions.CamelKubernetesVerifyIntegrationAction;
import org.citrusframework.camel.cli.CamelCli;
import org.citrusframework.camel.cli.KubernetesPlugin;
import org.citrusframework.jbang.ProcessAndOutput;
import org.citrusframework.xml.XmlTestLoader;
import org.mockito.Mock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CamelKubernetesVerifyIntegrationTest extends AbstractXmlActionTest {

    @Mock
    private CamelCli camelCli;

    @Mock
    private KubernetesPlugin k8sPlugin;

    @Mock
    private ProcessAndOutput pao;

    @Mock
    private Process process;

    @BeforeEach
    public void setup() {
        lenient().when(camelCli.kubernetes()).thenReturn(k8sPlugin);
        lenient().when(pao.getProcess()).thenReturn(process);
    }

    @Test
    public void shouldLoadCamelActions() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/camel/xml/camel-cli-kubernetes-verify.citrus.it.xml");

        CamelContext citrusCamelContext = new DefaultCamelContext();
        citrusCamelContext.start();

        context.getReferenceResolver().bind(CamelSettings.getContextName(), citrusCamelContext);
        context.getReferenceResolver().bind("camelContext", citrusCamelContext);

        context.getReferenceResolver().bind("camel-cli", camelCli);

        lenient().when(k8sPlugin.logs("--name", "my-route")).thenReturn(pao);
        lenient().when(k8sPlugin.logs("--label", "app=camel")).thenReturn(pao);
        lenient().when(process.exitValue()).thenReturn(0);
        lenient().when(pao.getOutput()).thenReturn("Camel rocks!");

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("CamelCliKubernetesVerifyTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(2L, result.getActionCount());
        Assertions.assertEquals(CamelKubernetesVerifyIntegrationAction.class, result.getTestAction(0).getClass());
        Assertions.assertEquals("camel:cli:kubernetes:verify", result.getTestAction(0).getName());

        verify(k8sPlugin).logs("--name", "my-route");
        verify(k8sPlugin).logs("--label", "app=camel");
    }
}
