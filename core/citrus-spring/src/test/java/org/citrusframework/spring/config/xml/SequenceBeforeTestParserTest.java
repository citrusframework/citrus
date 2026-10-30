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
import org.citrusframework.container.SequenceBeforeTest;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @since 2.0
 */
public class SequenceBeforeTestParserTest extends AbstractBeanDefinitionParserTest {

    @BeforeAll
    @Override
    protected void parseBeanDefinitions() {
    }

    @Test
    public void testSequenceBeforeParser() {
        beanDefinitionContext = createApplicationContext("context");
        Map<String, SequenceBeforeTest> container = beanDefinitionContext.getBeansOfType(SequenceBeforeTest.class);

        Assertions.assertEquals(5L, container.size());

        SequenceBeforeTest sequenceBefore = container.get("beforeTest");
        Assertions.assertEquals("beforeTest", sequenceBefore.getName());
        Assertions.assertNull(sequenceBefore.getNamePattern());
        Assertions.assertNull(sequenceBefore.getPackageNamePattern());
        Assertions.assertEquals(0L, sequenceBefore.getTestGroups().size());
        Assertions.assertEquals(3L, sequenceBefore.getActionCount());

        Assertions.assertEquals(EchoAction.class, sequenceBefore.getActions().get(0).getClass());
        Assertions.assertEquals(CustomTestAction.class, sequenceBefore.getActions().get(1).getClass());
        Assertions.assertEquals(EchoAction.class, sequenceBefore.getActions().get(2).getClass());

        Assertions.assertTrue(sequenceBefore.shouldExecute("Database_OK_Test", "org.citrusframework", null));
        Assertions.assertTrue(sequenceBefore.shouldExecute("Database_OK_Test", "org.citrusframework.database", null));
        Assertions.assertTrue(sequenceBefore.shouldExecute("Interface_OK_Test", "org.citrusframework", new String[]{"unit"}));
        Assertions.assertTrue(sequenceBefore.shouldExecute("Database_Failed_Test", "org.citrusframework.database", null));

        sequenceBefore = container.get("beforeTest2");
        Assertions.assertEquals("beforeTest2", sequenceBefore.getName());

        Assertions.assertEquals("*OK_Test", sequenceBefore.getNamePattern());
        Assertions.assertNull(sequenceBefore.getPackageNamePattern());
        Assertions.assertEquals(0L, sequenceBefore.getTestGroups().size());
        Assertions.assertEquals(1L, sequenceBefore.getActionCount());

        Assertions.assertEquals(EchoAction.class, sequenceBefore.getActions().get(0).getClass());

        Assertions.assertTrue(sequenceBefore.shouldExecute("Database_OK_Test", "org.citrusframework", null));
        Assertions.assertTrue(sequenceBefore.shouldExecute("Database_OK_Test", "org.citrusframework.database", null));
        Assertions.assertTrue(sequenceBefore.shouldExecute("Interface_OK_Test", "org.citrusframework", new String[]{"unit"}));
        Assertions.assertFalse(sequenceBefore.shouldExecute("Database_Failed_Test", "org.citrusframework.database", null));

        sequenceBefore = container.get("beforeTest3");
        Assertions.assertEquals("beforeTest3", sequenceBefore.getName());
        Assertions.assertNull(sequenceBefore.getNamePattern());
        Assertions.assertEquals("org.citrusframework.database", sequenceBefore.getPackageNamePattern());
        Assertions.assertEquals(0L, sequenceBefore.getTestGroups().size());
        Assertions.assertEquals(1L, sequenceBefore.getActionCount());

        Assertions.assertEquals(EchoAction.class, sequenceBefore.getActions().get(0).getClass());

        Assertions.assertFalse(sequenceBefore.shouldExecute("Database_OK_Test", "org.citrusframework", null));
        Assertions.assertTrue(sequenceBefore.shouldExecute("Database_OK_Test", "org.citrusframework.database", null));
        Assertions.assertTrue(sequenceBefore.shouldExecute("Interface_OK_Test", "org.citrusframework.database", new String[]{"unit"}));
        Assertions.assertTrue(sequenceBefore.shouldExecute("Database_Failed_Test", "org.citrusframework.database", null));

        sequenceBefore.execute(context);

        sequenceBefore = container.get("beforeTest4");
        Assertions.assertEquals("beforeTest4", sequenceBefore.getName());
        Assertions.assertEquals("*OK_Test", sequenceBefore.getNamePattern());
        Assertions.assertEquals("org.citrusframework.database", sequenceBefore.getPackageNamePattern());
        Assertions.assertEquals(2L, sequenceBefore.getTestGroups().size());
        Assertions.assertEquals(1L, sequenceBefore.getActionCount());

        Assertions.assertEquals(EchoAction.class, sequenceBefore.getActions().get(0).getClass());

        Assertions.assertFalse(sequenceBefore.shouldExecute("Database_OK_Test", "org.citrusframework", null));
        Assertions.assertFalse(sequenceBefore.shouldExecute("Database_Failed_Test", "org.citrusframework.database", null));
        Assertions.assertFalse(sequenceBefore.shouldExecute("Database_OK_Test", "org.citrusframework.database", null));
        Assertions.assertFalse(sequenceBefore.shouldExecute("Database_OK_Test", "org.citrusframework.database", new String[]{}));
        Assertions.assertFalse(sequenceBefore.shouldExecute("Database_OK_Test", "org.citrusframework", new String[]{"unit"}));
        Assertions.assertFalse(sequenceBefore.shouldExecute("Database_Failed_Test", "org.citrusframework.database", new String[]{"unit"}));
        Assertions.assertTrue(sequenceBefore.shouldExecute("Database_OK_Test", "org.citrusframework.database", new String[]{"unit"}));
        Assertions.assertTrue(sequenceBefore.shouldExecute("Interface_OK_Test", "org.citrusframework.database", new String[]{"e2e"}));
        Assertions.assertTrue(sequenceBefore.shouldExecute("OK_Test", "org.citrusframework.database", new String[]{"unit"}));
        Assertions.assertTrue(sequenceBefore.shouldExecute("Foo_OK_Test", "org.citrusframework.database", new String[]{"other", "unit", "e2e"}));
        Assertions.assertTrue(sequenceBefore.shouldExecute("Foo_OK_Test", "org.citrusframework.database", new String[]{"e2e"}));
        Assertions.assertTrue(sequenceBefore.shouldExecute("Foo_OK_Test", "org.citrusframework.database", new String[]{"other", "unit", "e2e"}));

        sequenceBefore.execute(context);

        sequenceBefore = container.get("beforeTest5");
        Assertions.assertEquals("beforeTest5", sequenceBefore.getName());
        Assertions.assertNull(sequenceBefore.getNamePattern());
        Assertions.assertNull(sequenceBefore.getPackageNamePattern());
        Assertions.assertEquals(0L, sequenceBefore.getTestGroups().size());
        Assertions.assertEquals(0L, sequenceBefore.getEnv().size());
        Assertions.assertEquals(1L, sequenceBefore.getSystemProperties().size());
        Assertions.assertEquals(1L, sequenceBefore.getActionCount());

        Assertions.assertEquals(EchoAction.class, sequenceBefore.getActions().get(0).getClass());

        Assertions.assertFalse(sequenceBefore.shouldExecute("Foo_OK_Test", "org.citrusframework", null));
        System.setProperty("before-test", "false");
        Assertions.assertFalse(sequenceBefore.shouldExecute("Foo_OK_Test", "org.citrusframework", null));
        System.setProperty("before-test", "true");
        Assertions.assertTrue(sequenceBefore.shouldExecute("Foo_OK_Test", "org.citrusframework", null));

        sequenceBefore.execute(context);
    }
}
