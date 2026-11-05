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

package org.citrusframework.groovy.yaml;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.groovy.actions.GroovyAction;
import org.citrusframework.yaml.YamlTestLoader;
import org.citrusframework.yaml.actions.YamlTestActionBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GroovyTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadGroovyActions() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/groovy/yaml/groovy.citrus.it.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("GroovyTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(4L, result.getActionCount());
        Assertions.assertEquals(GroovyAction.class, result.getTestAction(0).getClass());

        int actionIndex = 0;

        GroovyAction action = (GroovyAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(action.getScriptResourcePath());
        Assertions.assertEquals("classpath:org/citrusframework/script/script-template.groovy", action.getScriptTemplatePath());
        Assertions.assertEquals("println 'Hello Citrus'", action.getScript().trim());

        action = (GroovyAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(action.getScriptResourcePath());
        Assertions.assertNotNull(action.getScript());
        Assertions.assertFalse(action.isUseScriptTemplate());

        action = (GroovyAction) result.getTestAction(actionIndex++);
        Assertions.assertNull(action.getScriptResourcePath());
        Assertions.assertEquals("classpath:org/citrusframework/groovy/script/custom-script-template.groovy", action.getScriptTemplatePath());
        Assertions.assertNotNull(action.getScript());

        action = (GroovyAction) result.getTestAction(actionIndex);
        Assertions.assertNotNull(action.getScriptResourcePath());
        Assertions.assertEquals("classpath:org/citrusframework/groovy/script/example.groovy", action.getScriptResourcePath());
        Assertions.assertNull(action.getScript());
    }

    @Test
    public void shouldLookupTestActionBuilder() {
        Assertions.assertTrue(YamlTestActionBuilder.lookup().containsKey("groovy"));
        Assertions.assertTrue(YamlTestActionBuilder.lookup("groovy").isPresent());
        Assertions.assertEquals(Groovy.class, YamlTestActionBuilder.lookup("groovy").get().getClass());
    }
}
