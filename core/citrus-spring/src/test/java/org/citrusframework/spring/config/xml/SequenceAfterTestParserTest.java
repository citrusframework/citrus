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
import org.citrusframework.container.SequenceAfterTest;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @since 2.0
 */
public class SequenceAfterTestParserTest extends AbstractBeanDefinitionParserTest {

    @BeforeAll
    @Override
    protected void parseBeanDefinitions() {
    }

    @Test
    public void testSequenceAfterParser() {
        beanDefinitionContext = createApplicationContext("context");
        Map<String, SequenceAfterTest> container = beanDefinitionContext.getBeansOfType(SequenceAfterTest.class);

        Assertions.assertEquals(5L, container.size());

        SequenceAfterTest sequenceAfter = container.get("afterTest");
        Assertions.assertEquals("afterTest", sequenceAfter.getName());
        Assertions.assertNull(sequenceAfter.getNamePattern());
        Assertions.assertNull(sequenceAfter.getPackageNamePattern());
        Assertions.assertEquals(0L, sequenceAfter.getTestGroups().size());
        Assertions.assertEquals(3L, sequenceAfter.getActionCount());

        Assertions.assertEquals(EchoAction.class, sequenceAfter.getActions().get(0).getClass());
        Assertions.assertEquals(CustomTestAction.class, sequenceAfter.getActions().get(1).getClass());
        Assertions.assertEquals(EchoAction.class, sequenceAfter.getActions().get(2).getClass());

        Assertions.assertTrue(sequenceAfter.shouldExecute("Database_OK_Test", "org.citrusframework", null));
        Assertions.assertTrue(sequenceAfter.shouldExecute("Database_OK_Test", "org.citrusframework.database", null));
        Assertions.assertTrue(sequenceAfter.shouldExecute("Interface_OK_Test", "org.citrusframework", new String[]{"unit"}));
        Assertions.assertTrue(sequenceAfter.shouldExecute("Database_Failed_Test", "org.citrusframework.database", null));

        sequenceAfter = container.get("afterTest2");
        Assertions.assertEquals("afterTest2", sequenceAfter.getName());

        Assertions.assertEquals("*OK_Test", sequenceAfter.getNamePattern());
        Assertions.assertNull(sequenceAfter.getPackageNamePattern());
        Assertions.assertEquals(0L, sequenceAfter.getTestGroups().size());
        Assertions.assertEquals(1L, sequenceAfter.getActionCount());

        Assertions.assertEquals(EchoAction.class, sequenceAfter.getActions().get(0).getClass());

        Assertions.assertTrue(sequenceAfter.shouldExecute("Database_OK_Test", "org.citrusframework", null));
        Assertions.assertTrue(sequenceAfter.shouldExecute("Database_OK_Test", "org.citrusframework.database", null));
        Assertions.assertTrue(sequenceAfter.shouldExecute("Interface_OK_Test", "org.citrusframework", new String[]{"unit"}));
        Assertions.assertFalse(sequenceAfter.shouldExecute("Database_Failed_Test", "org.citrusframework.database", null));

        sequenceAfter = container.get("afterTest3");
        Assertions.assertEquals("afterTest3", sequenceAfter.getName());
        Assertions.assertNull(sequenceAfter.getNamePattern());
        Assertions.assertEquals("org.citrusframework.database", sequenceAfter.getPackageNamePattern());
        Assertions.assertEquals(0L, sequenceAfter.getTestGroups().size());
        Assertions.assertEquals(1L, sequenceAfter.getActionCount());

        Assertions.assertEquals(EchoAction.class, sequenceAfter.getActions().get(0).getClass());

        Assertions.assertFalse(sequenceAfter.shouldExecute("Database_OK_Test", "org.citrusframework", null));
        Assertions.assertTrue(sequenceAfter.shouldExecute("Database_OK_Test", "org.citrusframework.database", null));
        Assertions.assertTrue(sequenceAfter.shouldExecute("Interface_OK_Test", "org.citrusframework.database", new String[] {"unit"}));
        Assertions.assertTrue(sequenceAfter.shouldExecute("Database_Failed_Test", "org.citrusframework.database", null));

        sequenceAfter.execute(context);

        sequenceAfter = container.get("afterTest4");
        Assertions.assertEquals("afterTest4", sequenceAfter.getName());
        Assertions.assertEquals("*OK_Test", sequenceAfter.getNamePattern());
        Assertions.assertEquals("org.citrusframework.database", sequenceAfter.getPackageNamePattern());
        Assertions.assertEquals(2L, sequenceAfter.getTestGroups().size());
        Assertions.assertEquals(1L, sequenceAfter.getActionCount());

        Assertions.assertEquals(EchoAction.class, sequenceAfter.getActions().get(0).getClass());

        Assertions.assertFalse(sequenceAfter.shouldExecute("Database_OK_Test", "org.citrusframework", null));
        Assertions.assertFalse(sequenceAfter.shouldExecute("Database_Failed_Test", "org.citrusframework.database", null));
        Assertions.assertFalse(sequenceAfter.shouldExecute("Database_OK_Test", "org.citrusframework.database", null));
        Assertions.assertFalse(sequenceAfter.shouldExecute("Database_OK_Test", "org.citrusframework.database", new String[]{}));
        Assertions.assertFalse(sequenceAfter.shouldExecute("Database_OK_Test", "org.citrusframework", new String[]{"unit"}));
        Assertions.assertFalse(sequenceAfter.shouldExecute("Database_Failed_Test", "org.citrusframework.database", new String[]{"unit"}));
        Assertions.assertTrue(sequenceAfter.shouldExecute("Database_OK_Test", "org.citrusframework.database", new String[]{"unit"}));
        Assertions.assertTrue(sequenceAfter.shouldExecute("Interface_OK_Test", "org.citrusframework.database", new String[]{"e2e"}));
        Assertions.assertTrue(sequenceAfter.shouldExecute("OK_Test", "org.citrusframework.database", new String[]{"unit"}));
        Assertions.assertTrue(sequenceAfter.shouldExecute("Foo_OK_Test", "org.citrusframework.database", new String[]{"other", "unit", "e2e"}));
        Assertions.assertTrue(sequenceAfter.shouldExecute("Foo_OK_Test", "org.citrusframework.database", new String[] {"e2e"}));
        Assertions.assertTrue(sequenceAfter.shouldExecute("Foo_OK_Test", "org.citrusframework.database", new String[] {"other", "unit", "e2e"}));

        sequenceAfter.execute(context);

        sequenceAfter = container.get("afterTest5");
        Assertions.assertEquals("afterTest5", sequenceAfter.getName());
        Assertions.assertNull(sequenceAfter.getNamePattern());
        Assertions.assertNull(sequenceAfter.getPackageNamePattern());
        Assertions.assertEquals(0L, sequenceAfter.getTestGroups().size());
        Assertions.assertEquals(0L, sequenceAfter.getEnv().size());
        Assertions.assertEquals(1L, sequenceAfter.getSystemProperties().size());
        Assertions.assertEquals(1L, sequenceAfter.getActionCount());

        Assertions.assertEquals(EchoAction.class, sequenceAfter.getActions().get(0).getClass());

        Assertions.assertFalse(sequenceAfter.shouldExecute("Foo_OK_Test", "org.citrusframework", null));
        System.setProperty("after-test", "false");
        Assertions.assertFalse(sequenceAfter.shouldExecute("Foo_OK_Test", "org.citrusframework", null));
        System.setProperty("after-test", "true");
        Assertions.assertTrue(sequenceAfter.shouldExecute("Foo_OK_Test", "org.citrusframework", null));

        sequenceAfter.execute(context);
    }
}
