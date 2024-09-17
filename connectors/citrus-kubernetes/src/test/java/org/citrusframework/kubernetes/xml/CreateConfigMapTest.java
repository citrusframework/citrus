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
import java.nio.charset.StandardCharsets;

import io.fabric8.kubernetes.api.model.ConfigMap;
import org.apache.commons.codec.binary.Base64;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.kubernetes.actions.CreateConfigMapAction;
import org.citrusframework.kubernetes.integration.KubernetesCreateConfigMapsIT;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.xml.XmlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CreateConfigMapTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadKubernetesActions() throws IOException {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/xml/create-config-map-test.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "CreateConfigMapTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 2L);
        Assert.assertEquals(result.getTestAction(0).getClass(), CreateConfigMapAction.class);
        Assert.assertTrue(result.getTestResult().isSuccess());

        String namespace = "test";
        ConfigMap configMap = k8sClient.configMaps().inNamespace(namespace).withName("my-config-map-1").get();
        Assert.assertNotNull(configMap);
        Assert.assertEquals(configMap.getData().size(), 1);
        Assert.assertEquals(configMap.getData().get("foo"), Base64.encodeBase64String("bar".getBytes(StandardCharsets.UTF_8)));

        String configMapContent = FileUtils.readToString(Resources.fromClasspath("configMap.properties", KubernetesCreateConfigMapsIT.class));

        configMap = k8sClient.configMaps().inNamespace(namespace).withName("my-config-map-2").get();
        Assert.assertNotNull(configMap);
        Assert.assertEquals(configMap.getData().size(), 1);
        Assert.assertEquals(configMap.getData().get("configMap.properties"),
                Base64.encodeBase64String(configMapContent.getBytes(StandardCharsets.UTF_8)));
    }
}
