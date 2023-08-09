/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.yaml.actions;

import org.apache.tools.ant.DefaultLogger;
import org.citrusframework.TestCase;
import org.citrusframework.actions.AntRunAction;
import org.citrusframework.yaml.YamlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class AntRunTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadAntRun() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/yaml/actions/antrun-test.yaml");

        context.getReferenceResolver().bind("logger", new DefaultLogger());

        testLoader.load();
        TestCase result = testLoader.getTestCase();

        int actionIndex = 0;

        AntRunAction action = (AntRunAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getBuildFilePath(), "classpath:org/citrusframework/yaml/build.xml");
        Assert.assertEquals(action.getTarget(), "sayHello");
        Assert.assertNull(action.getTargets());
        Assert.assertEquals(action.getProperties().size(), 0L);
        Assert.assertNull(action.getPropertyFilePath());
        Assert.assertNull(action.getBuildListener());

        action = (AntRunAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getBuildFilePath(), "classpath:org/citrusframework/yaml/build.xml");
        Assert.assertNull(action.getTarget());
        Assert.assertEquals(action.getTargets(), "sayHello,sayGoodbye");
        Assert.assertEquals(action.getProperties().size(), 0L);
        Assert.assertNull(action.getPropertyFilePath());
        Assert.assertNull(action.getBuildListener());

        action = (AntRunAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getBuildFilePath(), "classpath:org/citrusframework/yaml/build.xml");
        Assert.assertEquals(action.getTarget(), "sayHello");
        Assert.assertNull(action.getTargets());
        Assert.assertEquals(action.getProperties().size(), 2L);
        Assert.assertEquals(action.getProperties().get("welcomeText"), "Hello World!");
        Assert.assertEquals(action.getProperties().get("goodbyeText"), "Goodbye!");
        Assert.assertNull(action.getPropertyFilePath());
        Assert.assertNull(action.getBuildListener());

        action = (AntRunAction) result.getTestAction(actionIndex);
        Assert.assertEquals(action.getBuildFilePath(), "classpath:org/citrusframework/yaml/build.xml");
        Assert.assertEquals(action.getTarget(), "sayHello");
        Assert.assertNull(action.getTargets());
        Assert.assertEquals(action.getProperties().size(), 0L);
        Assert.assertEquals(action.getPropertyFilePath(), "classpath:org/citrusframework/yaml/build.properties");
        Assert.assertNotNull(action.getBuildListener());
        Assert.assertEquals(action.getBuildListener().getClass(), DefaultLogger.class);
    }

}
