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

package org.citrusframework.groovy.config.xml;

import org.citrusframework.spring.config.CitrusNamespaceParserRegistry;
import org.citrusframework.groovy.actions.GroovyAction;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GroovyActionParserTest extends AbstractActionParserTest<GroovyAction> {

    @Test
    public void testActionParser() {
        assertActionCount(4);
        assertActionClassAndName(GroovyAction.class, "groovy");

        GroovyAction action = getNextTestActionFromTest();
        Assertions.assertNull(action.getScriptResourcePath());
        Assertions.assertEquals("classpath:org/citrusframework/script/script-template.groovy", action.getScriptTemplatePath());
        Assertions.assertEquals("println 'Hello Citrus'", action.getScript().trim());

        action = getNextTestActionFromTest();
        Assertions.assertNull(action.getScriptResourcePath());
        Assertions.assertNotNull(action.getScript());
        Assertions.assertFalse(action.isUseScriptTemplate());

        action = getNextTestActionFromTest();
        Assertions.assertNull(action.getScriptResourcePath());
        Assertions.assertEquals("classpath:org/citrusframework/script/custom-script-template.groovy", action.getScriptTemplatePath());
        Assertions.assertNotNull(action.getScript());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getScriptResourcePath());
        Assertions.assertEquals("classpath:org/citrusframework/script/example.groovy", action.getScriptResourcePath());
        Assertions.assertNull(action.getScript());
    }

    @Test
    public void shouldLookupTestActionParser() {
        Assertions.assertTrue(CitrusNamespaceParserRegistry.lookupBeanParser().containsKey("groovy"));
        Assertions.assertEquals(GroovyActionParser.class, CitrusNamespaceParserRegistry.lookupBeanParser().get("groovy").getClass());

        Assertions.assertEquals(GroovyActionParser.class, CitrusNamespaceParserRegistry.getBeanParser("groovy").getClass());
    }
}
