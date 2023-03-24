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
import org.citrusframework.container.SequenceBeforeTest;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class SequenceBeforeTestParserTest extends AbstractBeanDefinitionParserTest {

    @BeforeClass
    @Override
    protected void parseBeanDefinitions() {
    }

    @Test
    public void testSequenceBeforeParser() throws Exception {
        beanDefinitionContext = createApplicationContext("context");
        Map<String, SequenceBeforeTest> container = beanDefinitionContext.getBeansOfType(SequenceBeforeTest.class);

        Assert.assertEquals(container.size(), 5L);

        SequenceBeforeTest sequenceBefore = container.get("beforeTest");
        Assert.assertEquals(sequenceBefore.getName(), "beforeTest");
        Assert.assertNull(sequenceBefore.getNamePattern());
        Assert.assertNull(sequenceBefore.getPackageNamePattern());
        Assert.assertEquals(sequenceBefore.getTestGroups().size(), 0L);
        Assert.assertEquals(sequenceBefore.getActionCount(), 3L);

        Assert.assertEquals(sequenceBefore.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(sequenceBefore.getActions().get(1).getClass(), CustomTestAction.class);
        Assert.assertEquals(sequenceBefore.getActions().get(2).getClass(), EchoAction.class);

        Assert.assertTrue(sequenceBefore.shouldExecute("Database_OK_Test", "org.citrusframework", null));
        Assert.assertTrue(sequenceBefore.shouldExecute("Database_OK_Test", "org.citrusframework.database", null));
        Assert.assertTrue(sequenceBefore.shouldExecute("Interface_OK_Test", "org.citrusframework", new String[]{"unit"}));
        Assert.assertTrue(sequenceBefore.shouldExecute("Database_Failed_Test", "org.citrusframework.database", null));

        sequenceBefore = container.get("beforeTest2");
        Assert.assertEquals(sequenceBefore.getName(), "beforeTest2");

        Assert.assertEquals(sequenceBefore.getNamePattern(), "*OK_Test");
        Assert.assertNull(sequenceBefore.getPackageNamePattern());
        Assert.assertEquals(sequenceBefore.getTestGroups().size(), 0L);
        Assert.assertEquals(sequenceBefore.getActionCount(), 1L);

        Assert.assertEquals(sequenceBefore.getActions().get(0).getClass(), EchoAction.class);

        Assert.assertTrue(sequenceBefore.shouldExecute("Database_OK_Test", "org.citrusframework", null));
        Assert.assertTrue(sequenceBefore.shouldExecute("Database_OK_Test", "org.citrusframework.database", null));
        Assert.assertTrue(sequenceBefore.shouldExecute("Interface_OK_Test", "org.citrusframework", new String[]{"unit"}));
        Assert.assertFalse(sequenceBefore.shouldExecute("Database_Failed_Test", "org.citrusframework.database", null));

        sequenceBefore = container.get("beforeTest3");
        Assert.assertEquals(sequenceBefore.getName(), "beforeTest3");
        Assert.assertNull(sequenceBefore.getNamePattern());
        Assert.assertEquals(sequenceBefore.getPackageNamePattern(), "org.citrusframework.database");
        Assert.assertEquals(sequenceBefore.getTestGroups().size(), 0L);
        Assert.assertEquals(sequenceBefore.getActionCount(), 1L);

        Assert.assertEquals(sequenceBefore.getActions().get(0).getClass(), EchoAction.class);

        Assert.assertFalse(sequenceBefore.shouldExecute("Database_OK_Test", "org.citrusframework", null));
        Assert.assertTrue(sequenceBefore.shouldExecute("Database_OK_Test", "org.citrusframework.database", null));
        Assert.assertTrue(sequenceBefore.shouldExecute("Interface_OK_Test", "org.citrusframework.database", new String[]{"unit"}));
        Assert.assertTrue(sequenceBefore.shouldExecute("Database_Failed_Test", "org.citrusframework.database", null));

        sequenceBefore.execute(context);

        sequenceBefore = container.get("beforeTest4");
        Assert.assertEquals(sequenceBefore.getName(), "beforeTest4");
        Assert.assertEquals(sequenceBefore.getNamePattern(), "*OK_Test");
        Assert.assertEquals(sequenceBefore.getPackageNamePattern(), "org.citrusframework.database");
        Assert.assertEquals(sequenceBefore.getTestGroups().size(), 2L);
        Assert.assertEquals(sequenceBefore.getActionCount(), 1L);

        Assert.assertEquals(sequenceBefore.getActions().get(0).getClass(), EchoAction.class);

        Assert.assertFalse(sequenceBefore.shouldExecute("Database_OK_Test", "org.citrusframework", null));
        Assert.assertFalse(sequenceBefore.shouldExecute("Database_Failed_Test", "org.citrusframework.database", null));
        Assert.assertFalse(sequenceBefore.shouldExecute("Database_OK_Test", "org.citrusframework.database", null));
        Assert.assertFalse(sequenceBefore.shouldExecute("Database_OK_Test", "org.citrusframework.database", new String[]{}));
        Assert.assertFalse(sequenceBefore.shouldExecute("Database_OK_Test", "org.citrusframework", new String[]{"unit"}));
        Assert.assertFalse(sequenceBefore.shouldExecute("Database_Failed_Test", "org.citrusframework.database", new String[]{"unit"}));
        Assert.assertTrue(sequenceBefore.shouldExecute("Database_OK_Test", "org.citrusframework.database", new String[]{"unit"}));
        Assert.assertTrue(sequenceBefore.shouldExecute("Interface_OK_Test", "org.citrusframework.database", new String[]{"e2e"}));
        Assert.assertTrue(sequenceBefore.shouldExecute("OK_Test", "org.citrusframework.database", new String[]{"unit"}));
        Assert.assertTrue(sequenceBefore.shouldExecute("Foo_OK_Test", "org.citrusframework.database", new String[]{"other", "unit", "e2e"}));
        Assert.assertTrue(sequenceBefore.shouldExecute("Foo_OK_Test", "org.citrusframework.database", new String[]{"e2e"}));
        Assert.assertTrue(sequenceBefore.shouldExecute("Foo_OK_Test", "org.citrusframework.database", new String[]{"other", "unit", "e2e"}));

        sequenceBefore.execute(context);

        sequenceBefore = container.get("beforeTest5");
        Assert.assertEquals(sequenceBefore.getName(), "beforeTest5");
        Assert.assertNull(sequenceBefore.getNamePattern());
        Assert.assertNull(sequenceBefore.getPackageNamePattern());
        Assert.assertEquals(sequenceBefore.getTestGroups().size(), 0L);
        Assert.assertEquals(sequenceBefore.getEnv().size(), 0L);
        Assert.assertEquals(sequenceBefore.getSystemProperties().size(), 1L);
        Assert.assertEquals(sequenceBefore.getActionCount(), 1L);

        Assert.assertEquals(sequenceBefore.getActions().get(0).getClass(), EchoAction.class);

        Assert.assertFalse(sequenceBefore.shouldExecute("Foo_OK_Test", "org.citrusframework", null));
        System.setProperty("before-test", "false");
        Assert.assertFalse(sequenceBefore.shouldExecute("Foo_OK_Test", "org.citrusframework", null));
        System.setProperty("before-test", "true");
        Assert.assertTrue(sequenceBefore.shouldExecute("Foo_OK_Test", "org.citrusframework", null));

        sequenceBefore.execute(context);
    }
}
