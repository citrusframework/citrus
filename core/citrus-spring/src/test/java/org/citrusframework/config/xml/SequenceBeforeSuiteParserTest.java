/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.config.xml;

import java.util.Map;

import org.citrusframework.actions.CustomTestAction;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.actions.SleepAction;
import org.citrusframework.container.SequenceBeforeSuite;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class SequenceBeforeSuiteParserTest extends AbstractBeanDefinitionParserTest {

    @BeforeClass
    @Override
    protected void parseBeanDefinitions() {
    }

    @Test
    public void testSequenceBeforeParser() throws Exception {
        beanDefinitionContext = createApplicationContext("context");
        Map<String, SequenceBeforeSuite> container = beanDefinitionContext.getBeansOfType(SequenceBeforeSuite.class);

        Assert.assertEquals(container.size(), 3L);

        SequenceBeforeSuite sequenceBefore = container.get("beforeSuite");
        Assert.assertEquals(sequenceBefore.getName(), "beforeSuite");
        Assert.assertEquals(sequenceBefore.getSuiteNames().size(), 0L);
        Assert.assertEquals(sequenceBefore.getTestGroups().size(), 0L);
        Assert.assertEquals(sequenceBefore.getActionCount(), 3L);

        Assert.assertEquals(sequenceBefore.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(sequenceBefore.getActions().get(1).getClass(), CustomTestAction.class);
        Assert.assertEquals(sequenceBefore.getActions().get(2).getClass(), EchoAction.class);

        Assert.assertTrue(sequenceBefore.shouldExecute("", null));
        Assert.assertTrue(sequenceBefore.shouldExecute("suiteA", null));
        Assert.assertTrue(sequenceBefore.shouldExecute("suiteB", null));
        Assert.assertTrue(sequenceBefore.shouldExecute("suiteZ", new String[] {"unit"}));

        sequenceBefore.execute(context);

        sequenceBefore = container.get("beforeSuite2");
        Assert.assertEquals(sequenceBefore.getName(), "beforeSuite2");
        Assert.assertEquals(sequenceBefore.getSuiteNames().size(), 2L);
        Assert.assertEquals(sequenceBefore.getTestGroups().size(), 2L);
        Assert.assertEquals(sequenceBefore.getActionCount(), 1L);

        Assert.assertFalse(sequenceBefore.shouldExecute("", null));
        Assert.assertFalse(sequenceBefore.shouldExecute("suiteA", null));
        Assert.assertFalse(sequenceBefore.shouldExecute("suiteB", null));
        Assert.assertFalse(sequenceBefore.shouldExecute("suiteZ", null));
        Assert.assertFalse(sequenceBefore.shouldExecute("suiteZ", new String[]{}));
        Assert.assertFalse(sequenceBefore.shouldExecute("suiteZ", new String[]{"unit"}));
        Assert.assertFalse(sequenceBefore.shouldExecute("suiteZ", new String[]{"e2e"}));
        Assert.assertFalse(sequenceBefore.shouldExecute("suiteA", new String[]{"other"}));
        Assert.assertTrue(sequenceBefore.shouldExecute("suiteA", new String[]{"unit"}));
        Assert.assertTrue(sequenceBefore.shouldExecute("suiteB", new String[]{"other", "unit", "e2e"}));
        Assert.assertTrue(sequenceBefore.shouldExecute("suiteA", new String[] {"e2e"}));
        Assert.assertTrue(sequenceBefore.shouldExecute("suiteB", new String[] {"other", "unit", "e2e"}));

        Assert.assertEquals(sequenceBefore.getActions().get(0).getClass(), SleepAction.class);

        sequenceBefore.execute(context);

        sequenceBefore = container.get("beforeSuite3");
        Assert.assertEquals(sequenceBefore.getName(), "beforeSuite3");
        Assert.assertEquals(sequenceBefore.getSuiteNames().size(), 0L);
        Assert.assertEquals(sequenceBefore.getTestGroups().size(), 0L);
        Assert.assertEquals(sequenceBefore.getEnv().size(), 0L);
        Assert.assertEquals(sequenceBefore.getSystemProperties().size(), 1L);
        Assert.assertEquals(sequenceBefore.getActionCount(), 1L);

        Assert.assertFalse(sequenceBefore.shouldExecute("suiteA", null));
        System.setProperty("before-suite", "false");
        Assert.assertFalse(sequenceBefore.shouldExecute("suiteA", null));
        System.setProperty("before-suite", "true");
        Assert.assertTrue(sequenceBefore.shouldExecute("suiteA", null));
        Assert.assertEquals(sequenceBefore.getActions().get(0).getClass(), SleepAction.class);

        sequenceBefore.execute(context);
    }
}
