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

import java.util.Collections;

import io.fabric8.kubernetes.api.model.ConditionBuilder;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.kubernetes.actions.VerifyCustomResourceAction;
import org.citrusframework.kubernetes.integration.Foo;
import org.citrusframework.xml.XmlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class VerifyCustomResourceTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadKubernetesActions() {
        String namespace = "test";
        Foo foo = new Foo();
        foo.setSpec(new Foo.FooSpec());
        foo.getSpec().setMessage("Hello");
        foo.getMetadata().setName("my-foo");
        foo.getMetadata().setNamespace(namespace);
        foo.getMetadata().getLabels().put("test", "citrus");
        foo.setStatus(new Foo.FooStatus());
        foo.getStatus().setConditions(Collections.singletonList(
                new ConditionBuilder()
                        .withType("Ready")
                        .withStatus("true")
                        .build()));

        k8sClient.resources(Foo.class)
                .inNamespace(namespace)
                .resource(foo)
                .create();

        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/xml/verify-custom-resource-test.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "VerifyCustomResourceTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 2L);
        Assert.assertEquals(result.getTestAction(0).getClass(), VerifyCustomResourceAction.class);
        Assert.assertTrue(result.getTestResult().isSuccess());
    }
}
