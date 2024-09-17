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
import org.testng.Assert;
import org.testng.annotations.Test;

public class CreateSecretTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadKubernetesActions() throws IOException {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/xml/create-secret-test.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "CreateSecretTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 2L);
        Assert.assertEquals(result.getTestAction(0).getClass(), CreateSecretAction.class);
        Assert.assertTrue(result.getTestResult().isSuccess());

        String namespace = "test";
        Secret secret = k8sClient.secrets().inNamespace(namespace).withName("my-secret-1").get();
        Assert.assertNotNull(secret);
        Assert.assertEquals(secret.getData().size(), 1);
        Assert.assertEquals(secret.getData().get("foo"), Base64.encodeBase64String("bar".getBytes(StandardCharsets.UTF_8)));

        String secretContent = FileUtils.readToString(Resources.fromClasspath("secret.properties", KubernetesCreateSecretsIT.class));

        secret = k8sClient.secrets().inNamespace(namespace).withName("my-secret-2").get();
        Assert.assertNotNull(secret);
        Assert.assertEquals(secret.getData().size(), 1);
        Assert.assertEquals(secret.getData().get("secret.properties"),
                Base64.encodeBase64String(secretContent.getBytes(StandardCharsets.UTF_8)));
    }
}
