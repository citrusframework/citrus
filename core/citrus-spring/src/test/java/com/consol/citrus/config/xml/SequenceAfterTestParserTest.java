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

package com.consol.citrus.config.xml;

import java.util.Map;

import com.consol.citrus.actions.CustomTestAction;
import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class SequenceAfterTestParserTest extends AbstractBeanDefinitionParserTest {

    @BeforeClass
    @Override
    protected void parseBeanDefinitions() {
    }

    @Test
    public void testSequenceAfterParser() throws Exception {
        beanDefinitionContext = createApplicationContext("context");
        Map<String, SequenceAfterTest> container = beanDefinitionContext.getBeansOfType(SequenceAfterTest.class);

        Assert.assertEquals(container.size(), 5L);

        SequenceAfterTest sequenceAfter = container.get("afterTest");
        Assert.assertEquals(sequenceAfter.getName(), "afterTest");
        Assert.assertNull(sequenceAfter.getNamePattern());
        Assert.assertNull(sequenceAfter.getPackageNamePattern());
        Assert.assertEquals(sequenceAfter.getTestGroups().size(), 0L);
        Assert.assertEquals(sequenceAfter.getActionCount(), 3L);

        Assert.assertEquals(sequenceAfter.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(sequenceAfter.getActions().get(1).getClass(), CustomTestAction.class);
        Assert.assertEquals(sequenceAfter.getActions().get(2).getClass(), EchoAction.class);

        Assert.assertTrue(sequenceAfter.shouldExecute("Database_OK_Test", "com.consol.citrus", null));
        Assert.assertTrue(sequenceAfter.shouldExecute("Database_OK_Test", "com.consol.citrus.database", null));
        Assert.assertTrue(sequenceAfter.shouldExecute("Interface_OK_Test", "com.consol.citrus", new String[]{"unit"}));
        Assert.assertTrue(sequenceAfter.shouldExecute("Database_Failed_Test", "com.consol.citrus.database", null));

        sequenceAfter = container.get("afterTest2");
        Assert.assertEquals(sequenceAfter.getName(), "afterTest2");

        Assert.assertEquals(sequenceAfter.getNamePattern(), "*OK_Test");
        Assert.assertNull(sequenceAfter.getPackageNamePattern());
        Assert.assertEquals(sequenceAfter.getTestGroups().size(), 0L);
        Assert.assertEquals(sequenceAfter.getActionCount(), 1L);

        Assert.assertEquals(sequenceAfter.getActions().get(0).getClass(), EchoAction.class);

        Assert.assertTrue(sequenceAfter.shouldExecute("Database_OK_Test", "com.consol.citrus", null));
        Assert.assertTrue(sequenceAfter.shouldExecute("Database_OK_Test", "com.consol.citrus.database", null));
        Assert.assertTrue(sequenceAfter.shouldExecute("Interface_OK_Test", "com.consol.citrus", new String[]{"unit"}));
        Assert.assertFalse(sequenceAfter.shouldExecute("Database_Failed_Test", "com.consol.citrus.database", null));

        sequenceAfter = container.get("afterTest3");
        Assert.assertEquals(sequenceAfter.getName(), "afterTest3");
        Assert.assertNull(sequenceAfter.getNamePattern());
        Assert.assertEquals(sequenceAfter.getPackageNamePattern(), "com.consol.citrus.database");
        Assert.assertEquals(sequenceAfter.getTestGroups().size(), 0L);
        Assert.assertEquals(sequenceAfter.getActionCount(), 1L);

        Assert.assertEquals(sequenceAfter.getActions().get(0).getClass(), EchoAction.class);

        Assert.assertFalse(sequenceAfter.shouldExecute("Database_OK_Test", "com.consol.citrus", null));
        Assert.assertTrue(sequenceAfter.shouldExecute("Database_OK_Test", "com.consol.citrus.database", null));
        Assert.assertTrue(sequenceAfter.shouldExecute("Interface_OK_Test", "com.consol.citrus.database", new String[] {"unit"}));
        Assert.assertTrue(sequenceAfter.shouldExecute("Database_Failed_Test", "com.consol.citrus.database", null));

        sequenceAfter.execute(context);

        sequenceAfter = container.get("afterTest4");
        Assert.assertEquals(sequenceAfter.getName(), "afterTest4");
        Assert.assertEquals(sequenceAfter.getNamePattern(), "*OK_Test");
        Assert.assertEquals(sequenceAfter.getPackageNamePattern(), "com.consol.citrus.database");
        Assert.assertEquals(sequenceAfter.getTestGroups().size(), 2L);
        Assert.assertEquals(sequenceAfter.getActionCount(), 1L);

        Assert.assertEquals(sequenceAfter.getActions().get(0).getClass(), EchoAction.class);

        Assert.assertFalse(sequenceAfter.shouldExecute("Database_OK_Test", "com.consol.citrus", null));
        Assert.assertFalse(sequenceAfter.shouldExecute("Database_Failed_Test", "com.consol.citrus.database", null));
        Assert.assertFalse(sequenceAfter.shouldExecute("Database_OK_Test", "com.consol.citrus.database", null));
        Assert.assertFalse(sequenceAfter.shouldExecute("Database_OK_Test", "com.consol.citrus.database", new String[]{}));
        Assert.assertFalse(sequenceAfter.shouldExecute("Database_OK_Test", "com.consol.citrus", new String[]{"unit"}));
        Assert.assertFalse(sequenceAfter.shouldExecute("Database_Failed_Test", "com.consol.citrus.database", new String[]{"unit"}));
        Assert.assertTrue(sequenceAfter.shouldExecute("Database_OK_Test", "com.consol.citrus.database", new String[]{"unit"}));
        Assert.assertTrue(sequenceAfter.shouldExecute("Interface_OK_Test", "com.consol.citrus.database", new String[]{"e2e"}));
        Assert.assertTrue(sequenceAfter.shouldExecute("OK_Test", "com.consol.citrus.database", new String[]{"unit"}));
        Assert.assertTrue(sequenceAfter.shouldExecute("Foo_OK_Test", "com.consol.citrus.database", new String[]{"other", "unit", "e2e"}));
        Assert.assertTrue(sequenceAfter.shouldExecute("Foo_OK_Test", "com.consol.citrus.database", new String[] {"e2e"}));
        Assert.assertTrue(sequenceAfter.shouldExecute("Foo_OK_Test", "com.consol.citrus.database", new String[] {"other", "unit", "e2e"}));

        sequenceAfter.execute(context);

        sequenceAfter = container.get("afterTest5");
        Assert.assertEquals(sequenceAfter.getName(), "afterTest5");
        Assert.assertNull(sequenceAfter.getNamePattern());
        Assert.assertNull(sequenceAfter.getPackageNamePattern());
        Assert.assertEquals(sequenceAfter.getTestGroups().size(), 0L);
        Assert.assertEquals(sequenceAfter.getEnv().size(), 0L);
        Assert.assertEquals(sequenceAfter.getSystemProperties().size(), 1L);
        Assert.assertEquals(sequenceAfter.getActionCount(), 1L);

        Assert.assertEquals(sequenceAfter.getActions().get(0).getClass(), EchoAction.class);

        Assert.assertFalse(sequenceAfter.shouldExecute("Foo_OK_Test", "com.consol.citrus", null));
        System.setProperty("after-test", "false");
        Assert.assertFalse(sequenceAfter.shouldExecute("Foo_OK_Test", "com.consol.citrus", null));
        System.setProperty("after-test", "true");
        Assert.assertTrue(sequenceAfter.shouldExecute("Foo_OK_Test", "com.consol.citrus", null));

        sequenceAfter.execute(context);
    }
}
