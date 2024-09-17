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

package org.citrusframework.kubernetes.yaml;

import java.io.IOException;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.kubernetes.actions.DeleteSecretAction;
import org.citrusframework.yaml.YamlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DeleteSecretTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadKubernetesActions() throws IOException {
        String namespace = "test";
        Secret secret = new SecretBuilder()
                .withNewMetadata()
                    .withName("my-secret")
                    .withNamespace(namespace)
                .endMetadata()
                .build();

        k8sClient.secrets()
                .inNamespace(namespace)
                .resource(secret)
                .create();

        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/yaml/delete-secret-test.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "DeleteSecretTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 1L);
        Assert.assertEquals(result.getTestAction(0).getClass(), DeleteSecretAction.class);
        Assert.assertTrue(result.getTestResult().isSuccess());

        secret = k8sClient.secrets().inNamespace(namespace).withName("my-secret").get();
        Assert.assertNull(secret);
    }
}
