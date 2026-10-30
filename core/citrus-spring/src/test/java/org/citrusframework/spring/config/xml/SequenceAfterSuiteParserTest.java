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
import org.citrusframework.container.SequenceAfterSuite;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @since 2.0
 */
public class SequenceAfterSuiteParserTest extends AbstractBeanDefinitionParserTest {

    @BeforeAll
    @Override
    protected void parseBeanDefinitions() {
    }

    @Test
    public void testSequenceAfterParser() {
        beanDefinitionContext = createApplicationContext("context");
        Map<String, SequenceAfterSuite> container = beanDefinitionContext.getBeansOfType(SequenceAfterSuite.class);

        Assertions.assertEquals(3L, container.size());

        SequenceAfterSuite sequenceAfter = container.get("afterSuite");
        Assertions.assertEquals("afterSuite", sequenceAfter.getName());
        Assertions.assertEquals(0L, sequenceAfter.getSuiteNames().size());
        Assertions.assertEquals(0L, sequenceAfter.getTestGroups().size());
        Assertions.assertEquals(3L, sequenceAfter.getActionCount());

        Assertions.assertEquals(EchoAction.class, sequenceAfter.getActions().get(0).getClass());
        Assertions.assertEquals(CustomTestAction.class, sequenceAfter.getActions().get(1).getClass());
        Assertions.assertEquals(EchoAction.class, sequenceAfter.getActions().get(2).getClass());

        Assertions.assertTrue(sequenceAfter.shouldExecute("", null));
        Assertions.assertTrue(sequenceAfter.shouldExecute("suiteA", null));
        Assertions.assertTrue(sequenceAfter.shouldExecute("suiteB", null));
        Assertions.assertTrue(sequenceAfter.shouldExecute("suiteZ", new String[] {"unit"}));

        sequenceAfter.execute(context);

        sequenceAfter = container.get("afterSuite2");
        Assertions.assertEquals("afterSuite2", sequenceAfter.getName());
        Assertions.assertEquals(2L, sequenceAfter.getSuiteNames().size());
        Assertions.assertEquals(2L, sequenceAfter.getTestGroups().size());
        Assertions.assertEquals(1L, sequenceAfter.getActionCount());

        Assertions.assertFalse(sequenceAfter.shouldExecute("", null));
        Assertions.assertFalse(sequenceAfter.shouldExecute("suiteA", null));
        Assertions.assertFalse(sequenceAfter.shouldExecute("suiteB", null));
        Assertions.assertFalse(sequenceAfter.shouldExecute("suiteZ", null));
        Assertions.assertFalse(sequenceAfter.shouldExecute("suiteZ", new String[]{}));
        Assertions.assertFalse(sequenceAfter.shouldExecute("suiteZ", new String[]{"unit"}));
        Assertions.assertFalse(sequenceAfter.shouldExecute("suiteZ", new String[]{"e2e"}));
        Assertions.assertFalse(sequenceAfter.shouldExecute("suiteA", new String[]{"other"}));
        Assertions.assertTrue(sequenceAfter.shouldExecute("suiteA", new String[]{"unit"}));
        Assertions.assertTrue(sequenceAfter.shouldExecute("suiteB", new String[]{"other", "unit", "e2e"}));
        Assertions.assertTrue(sequenceAfter.shouldExecute("suiteA", new String[] {"e2e"}));
        Assertions.assertTrue(sequenceAfter.shouldExecute("suiteB", new String[] {"other", "unit", "e2e"}));

        Assertions.assertEquals(SleepAction.class, sequenceAfter.getActions().get(0).getClass());

        sequenceAfter.execute(context);

        sequenceAfter = container.get("afterSuite3");
        Assertions.assertEquals("afterSuite3", sequenceAfter.getName());
        Assertions.assertEquals(0L, sequenceAfter.getSuiteNames().size());
        Assertions.assertEquals(0L, sequenceAfter.getTestGroups().size());
        Assertions.assertEquals(0L, sequenceAfter.getEnv().size());
        Assertions.assertEquals(1L, sequenceAfter.getSystemProperties().size());
        Assertions.assertEquals(1L, sequenceAfter.getActionCount());

        Assertions.assertFalse(sequenceAfter.shouldExecute("suiteA", null));
        System.setProperty("after-suite", "false");
        Assertions.assertFalse(sequenceAfter.shouldExecute("suiteA", null));
        System.setProperty("after-suite", "true");
        Assertions.assertTrue(sequenceAfter.shouldExecute("suiteA", null));
        Assertions.assertEquals(SleepAction.class, sequenceAfter.getActions().get(0).getClass());

        sequenceAfter.execute(context);
    }
}
