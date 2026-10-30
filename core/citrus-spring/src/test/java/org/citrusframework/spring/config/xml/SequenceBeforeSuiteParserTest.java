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

package org.citrusframework.spring.config.xml;

import java.util.Map;

import org.citrusframework.spring.actions.CustomTestAction;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.actions.SleepAction;
import org.citrusframework.container.SequenceBeforeSuite;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @since 2.0
 */
public class SequenceBeforeSuiteParserTest extends AbstractBeanDefinitionParserTest {

    @BeforeAll
    @Override
    protected void parseBeanDefinitions() {
    }

    @Test
    public void testSequenceBeforeParser() {
        beanDefinitionContext = createApplicationContext("context");
        Map<String, SequenceBeforeSuite> container = beanDefinitionContext.getBeansOfType(SequenceBeforeSuite.class);

        Assertions.assertEquals(3L, container.size());

        SequenceBeforeSuite sequenceBefore = container.get("beforeSuite");
        Assertions.assertEquals("beforeSuite", sequenceBefore.getName());
        Assertions.assertEquals(0L, sequenceBefore.getSuiteNames().size());
        Assertions.assertEquals(0L, sequenceBefore.getTestGroups().size());
        Assertions.assertEquals(3L, sequenceBefore.getActionCount());

        Assertions.assertEquals(EchoAction.class, sequenceBefore.getActions().get(0).getClass());
        Assertions.assertEquals(CustomTestAction.class, sequenceBefore.getActions().get(1).getClass());
        Assertions.assertEquals(EchoAction.class, sequenceBefore.getActions().get(2).getClass());

        Assertions.assertTrue(sequenceBefore.shouldExecute("", null));
        Assertions.assertTrue(sequenceBefore.shouldExecute("suiteA", null));
        Assertions.assertTrue(sequenceBefore.shouldExecute("suiteB", null));
        Assertions.assertTrue(sequenceBefore.shouldExecute("suiteZ", new String[] {"unit"}));

        sequenceBefore.execute(context);

        sequenceBefore = container.get("beforeSuite2");
        Assertions.assertEquals("beforeSuite2", sequenceBefore.getName());
        Assertions.assertEquals(2L, sequenceBefore.getSuiteNames().size());
        Assertions.assertEquals(2L, sequenceBefore.getTestGroups().size());
        Assertions.assertEquals(1L, sequenceBefore.getActionCount());

        Assertions.assertFalse(sequenceBefore.shouldExecute("", null));
        Assertions.assertFalse(sequenceBefore.shouldExecute("suiteA", null));
        Assertions.assertFalse(sequenceBefore.shouldExecute("suiteB", null));
        Assertions.assertFalse(sequenceBefore.shouldExecute("suiteZ", null));
        Assertions.assertFalse(sequenceBefore.shouldExecute("suiteZ", new String[]{}));
        Assertions.assertFalse(sequenceBefore.shouldExecute("suiteZ", new String[]{"unit"}));
        Assertions.assertFalse(sequenceBefore.shouldExecute("suiteZ", new String[]{"e2e"}));
        Assertions.assertFalse(sequenceBefore.shouldExecute("suiteA", new String[]{"other"}));
        Assertions.assertTrue(sequenceBefore.shouldExecute("suiteA", new String[]{"unit"}));
        Assertions.assertTrue(sequenceBefore.shouldExecute("suiteB", new String[]{"other", "unit", "e2e"}));
        Assertions.assertTrue(sequenceBefore.shouldExecute("suiteA", new String[] {"e2e"}));
        Assertions.assertTrue(sequenceBefore.shouldExecute("suiteB", new String[] {"other", "unit", "e2e"}));

        Assertions.assertEquals(SleepAction.class, sequenceBefore.getActions().get(0).getClass());

        sequenceBefore.execute(context);

        sequenceBefore = container.get("beforeSuite3");
        Assertions.assertEquals("beforeSuite3", sequenceBefore.getName());
        Assertions.assertEquals(0L, sequenceBefore.getSuiteNames().size());
        Assertions.assertEquals(0L, sequenceBefore.getTestGroups().size());
        Assertions.assertEquals(0L, sequenceBefore.getEnv().size());
        Assertions.assertEquals(1L, sequenceBefore.getSystemProperties().size());
        Assertions.assertEquals(1L, sequenceBefore.getActionCount());

        Assertions.assertFalse(sequenceBefore.shouldExecute("suiteA", null));
        System.setProperty("before-suite", "false");
        Assertions.assertFalse(sequenceBefore.shouldExecute("suiteA", null));
        System.setProperty("before-suite", "true");
        Assertions.assertTrue(sequenceBefore.shouldExecute("suiteA", null));
        Assertions.assertEquals(SleepAction.class, sequenceBefore.getActions().get(0).getClass());

        sequenceBefore.execute(context);
    }
}
