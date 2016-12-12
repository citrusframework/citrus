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
import com.consol.citrus.context.TestContext;
import com.consol.citrus.kubernetes.actions.KubernetesExecuteAction;
import com.consol.citrus.kubernetes.command.*;
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
                kubernetes().info()
                    .validateCommandResult(new CommandResultCallback<Info.InfoModel>() {
                        @Override
                        public void doWithCommandResult(Info.InfoModel result, TestContext context) {
                            Assert.assertNotNull(result);
                        }
                    });

                kubernetes().listNamespaces();
                kubernetes().listNodes();

                kubernetes().listPods()
                            .withoutLabel("running")
                            .label("app", "myApp");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 4);
        Assert.assertEquals(test.getActions().get(0).getClass(), KubernetesExecuteAction.class);

        KubernetesExecuteAction action = (KubernetesExecuteAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "kubernetes-execute");
        Assert.assertEquals(action.getCommand().getClass(), Info.class);
        Assert.assertNotNull(action.getCommand().getResultCallback());

        action = (KubernetesExecuteAction)test.getActions().get(1);
        Assert.assertEquals(action.getName(), "kubernetes-execute");
        Assert.assertEquals(action.getCommand().getClass(), ListNamespaces.class);

        action = (KubernetesExecuteAction)test.getActions().get(2);
        Assert.assertEquals(action.getName(), "kubernetes-execute");
        Assert.assertEquals(action.getCommand().getClass(), ListNodes.class);

        action = (KubernetesExecuteAction)test.getActions().get(3);
        Assert.assertEquals(action.getName(), "kubernetes-execute");
        Assert.assertEquals(action.getCommand().getClass(), ListPods.class);
        Assert.assertEquals(action.getCommand().getParameters().get("label"), "!running,app=myApp");
    }
}
