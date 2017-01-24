/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.dsl.design;

import com.consol.citrus.TestCase;
import com.consol.citrus.kubernetes.actions.KubernetesExecuteAction;
import com.consol.citrus.kubernetes.command.*;
import com.consol.citrus.kubernetes.message.KubernetesMessageHeaders;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class KubernetesTestDesignerTest extends AbstractTestNGUnitTest {
    
    @Test
    public void testKubernetesBuilder() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                kubernetes()
                    .info()
                    .validate((result, context) -> Assert.assertNotNull(result));

                kubernetes().namespaces().list();
                kubernetes().nodes().list();

                kubernetes().pods()
                            .list()
                            .withoutLabel("running")
                            .label("app", "myApp");

                kubernetes().pods().get("myPod");
                kubernetes().pods().delete("myPod");

                kubernetes().nodes()
                            .watch()
                            .label("new");

                kubernetes().services()
                        .watch()
                        .name("myService")
                        .namespace("myNamespace")
                        .validate((event, context) -> Assert.assertNotNull(event));
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 8);
        Assert.assertEquals(test.getActions().get(0).getClass(), KubernetesExecuteAction.class);

        KubernetesExecuteAction action = (KubernetesExecuteAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "kubernetes-execute");
        Assert.assertEquals(action.getCommand().getClass(), Info.class);
        Assert.assertEquals(action.getCommand().getName(), "info");
        Assert.assertNotNull(action.getCommand().getResultCallback());

        action = (KubernetesExecuteAction)test.getActions().get(1);
        Assert.assertEquals(action.getName(), "kubernetes-execute");
        Assert.assertEquals(action.getCommand().getClass(), ListNamespaces.class);
        Assert.assertEquals(action.getCommand().getName(), "list-namespaces");

        action = (KubernetesExecuteAction)test.getActions().get(2);
        Assert.assertEquals(action.getName(), "kubernetes-execute");
        Assert.assertEquals(action.getCommand().getClass(), ListNodes.class);
        Assert.assertEquals(action.getCommand().getName(), "list-nodes");

        action = (KubernetesExecuteAction)test.getActions().get(3);
        Assert.assertEquals(action.getName(), "kubernetes-execute");
        Assert.assertEquals(action.getCommand().getClass(), ListPods.class);
        Assert.assertEquals(action.getCommand().getName(), "list-pods");
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL), "!running,app=myApp");

        action = (KubernetesExecuteAction)test.getActions().get(4);
        Assert.assertEquals(action.getName(), "kubernetes-execute");
        Assert.assertEquals(action.getCommand().getClass(), GetPod.class);
        Assert.assertEquals(action.getCommand().getName(), "get-pod");
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.NAME), "myPod");

        action = (KubernetesExecuteAction)test.getActions().get(5);
        Assert.assertEquals(action.getName(), "kubernetes-execute");
        Assert.assertEquals(action.getCommand().getClass(), DeletePod.class);
        Assert.assertEquals(action.getCommand().getName(), "delete-pod");
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.NAME), "myPod");

        action = (KubernetesExecuteAction)test.getActions().get(6);
        Assert.assertEquals(action.getName(), "kubernetes-execute");
        Assert.assertEquals(action.getCommand().getClass(), WatchNodes.class);
        Assert.assertEquals(action.getCommand().getName(), "watch-nodes");
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL), "new");

        action = (KubernetesExecuteAction)test.getActions().get(7);
        Assert.assertEquals(action.getName(), "kubernetes-execute");
        Assert.assertEquals(action.getCommand().getClass(), WatchServices.class);
        Assert.assertEquals(action.getCommand().getName(), "watch-services");
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.NAME), "myService");
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.NAMESPACE), "myNamespace");
    }
}
