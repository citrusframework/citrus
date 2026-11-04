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

import io.fabric8.kubernetes.api.model.Secret;
import org.apache.commons.codec.binary.Base64;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.kubernetes.actions.CreateSecretAction;
import org.citrusframework.kubernetes.integration.KubernetesCreateSecretsIT;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.xml.XmlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CreateSecretTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadKubernetesActions() throws IOException {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/xml/create-secret.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("CreateSecretTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(2L, result.getActionCount());
        Assertions.assertEquals(CreateSecretAction.class, result.getTestAction(0).getClass());
        Assertions.assertTrue(result.getTestResult().isSuccess());

        String namespace = "test";
        Secret secret = k8sClient.secrets().inNamespace(namespace).withName("my-secret-1").get();
        Assertions.assertNotNull(secret);
        Assertions.assertEquals(1, secret.getData().size());
        Assertions.assertEquals(Base64.encodeBase64String("bar".getBytes(StandardCharsets.UTF_8)), secret.getData().get("foo"));

        String secretContent = FileUtils.readToString(Resources.fromClasspath("secret.properties", KubernetesCreateSecretsIT.class));

        secret = k8sClient.secrets().inNamespace(namespace).withName("my-secret-2").get();
        Assertions.assertNotNull(secret);
        Assertions.assertEquals(1, secret.getData().size());
        Assertions.assertEquals(
                Base64.encodeBase64String(secretContent.getBytes(StandardCharsets.UTF_8)), secret.getData().get("secret.properties"));
    }
}
