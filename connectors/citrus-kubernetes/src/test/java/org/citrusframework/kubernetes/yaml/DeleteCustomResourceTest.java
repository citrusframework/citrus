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

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.kubernetes.actions.DeleteCustomResourceAction;
import org.citrusframework.kubernetes.integration.Foo;
import org.citrusframework.yaml.YamlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DeleteCustomResourceTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadKubernetesActions() throws IOException {
        String namespace = "test";
        Foo foo = new Foo();
        foo.setSpec(new Foo.FooSpec());
        foo.getSpec().setMessage("Hello");
        foo.getMetadata().setName("my-foo");
        foo.getMetadata().setNamespace(namespace);

        k8sClient.resources(Foo.class)
                .inNamespace(namespace)
                .resource(foo)
                .create();

        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/yaml/delete-custom-resource-test.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "DeleteCustomResourceTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 1L);
        Assert.assertEquals(result.getTestAction(0).getClass(), DeleteCustomResourceAction.class);
        Assert.assertTrue(result.getTestResult().isSuccess());

        foo = k8sClient.resources(Foo.class).inNamespace(namespace).withName("my-foo").get();
        Assert.assertNull(foo);
    }
}
