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

package org.citrusframework.yaml.actions;

import org.apache.tools.ant.DefaultLogger;
import org.citrusframework.TestCase;
import org.citrusframework.actions.AntRunAction;
import org.citrusframework.yaml.YamlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AntRunTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadAntRun() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/yaml/actions/antrun.citrus.it.yaml");

        context.getReferenceResolver().bind("logger", new DefaultLogger());

        testLoader.load();
        TestCase result = testLoader.getTestCase();

        int actionIndex = 0;

        AntRunAction action = (AntRunAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals("classpath:org/citrusframework/yaml/build.xml", action.getBuildFilePath());
        Assertions.assertEquals("sayHello", action.getTarget());
        Assertions.assertNull(action.getTargets());
        Assertions.assertEquals(0L, action.getProperties().size());
        Assertions.assertNull(action.getPropertyFilePath());
        Assertions.assertNull(action.getBuildListener());

        action = (AntRunAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals("classpath:org/citrusframework/yaml/build.xml", action.getBuildFilePath());
        Assertions.assertNull(action.getTarget());
        Assertions.assertEquals("sayHello,sayGoodbye", action.getTargets());
        Assertions.assertEquals(0L, action.getProperties().size());
        Assertions.assertNull(action.getPropertyFilePath());
        Assertions.assertNull(action.getBuildListener());

        action = (AntRunAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals("classpath:org/citrusframework/yaml/build.xml", action.getBuildFilePath());
        Assertions.assertEquals("sayHello", action.getTarget());
        Assertions.assertNull(action.getTargets());
        Assertions.assertEquals(2L, action.getProperties().size());
        Assertions.assertEquals("Hello World!", action.getProperties().get("welcomeText"));
        Assertions.assertEquals("Goodbye!", action.getProperties().get("goodbyeText"));
        Assertions.assertNull(action.getPropertyFilePath());
        Assertions.assertNull(action.getBuildListener());

        action = (AntRunAction) result.getTestAction(actionIndex);
        Assertions.assertEquals("classpath:org/citrusframework/yaml/build.xml", action.getBuildFilePath());
        Assertions.assertEquals("sayHello", action.getTarget());
        Assertions.assertNull(action.getTargets());
        Assertions.assertEquals(0L, action.getProperties().size());
        Assertions.assertEquals("classpath:org/citrusframework/yaml/build.properties", action.getPropertyFilePath());
        Assertions.assertNotNull(action.getBuildListener());
        Assertions.assertEquals(DefaultLogger.class, action.getBuildListener().getClass());
    }
}
