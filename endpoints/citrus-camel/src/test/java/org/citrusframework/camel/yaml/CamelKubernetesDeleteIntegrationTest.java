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

package org.citrusframework.camel.yaml;

import java.nio.file.Paths;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.camel.CamelSettings;
import org.citrusframework.camel.actions.CamelKubernetesDeleteIntegrationAction;
import org.citrusframework.camel.cli.CamelCli;
import org.citrusframework.camel.cli.KubernetesPlugin;
import org.citrusframework.jbang.ProcessAndOutput;
import org.citrusframework.spi.Resources;
import org.citrusframework.yaml.YamlTestLoader;
import org.mockito.Mock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CamelKubernetesDeleteIntegrationTest extends AbstractYamlActionTest {

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
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/camel/yaml/camel-cli-kubernetes-delete.citrus.it.yaml");

        CamelContext citrusCamelContext = new DefaultCamelContext();
        citrusCamelContext.start();

        context.getReferenceResolver().bind(CamelSettings.getContextName(), citrusCamelContext);
        context.getReferenceResolver().bind("camelContext", citrusCamelContext);

        context.getReferenceResolver().bind("camel-cli", camelCli);

        when(k8sPlugin.delete(eq("route"), any(String[].class))).thenReturn(pao);
        when(process.exitValue()).thenReturn(0);
        when(pao.getOutput()).thenReturn("SUCCESS!");

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("CamelCliKubernetesDeleteTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(1L, result.getActionCount());
        Assertions.assertEquals(CamelKubernetesDeleteIntegrationAction.class, result.getTestAction(0).getClass());
        Assertions.assertEquals("camel:cli:kubernetes:delete", result.getTestAction(0).getName());

        verify(camelCli).workingDir(Paths.get(Resources.create("classpath:org/citrusframework/camel/integration/route.yaml")
                .file().getParentFile().toPath().toAbsolutePath().toString()));
        verify(k8sPlugin).delete(eq("route"), eq(new String[] {}));
    }
}
