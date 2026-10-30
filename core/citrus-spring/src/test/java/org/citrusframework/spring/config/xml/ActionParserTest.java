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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;

import org.citrusframework.TestAction;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;

public class ActionParserTest extends AbstractActionParserTest<TestAction> {

    @Test
    public void testActionParser() {
        assertActionCount(1);

        Assertions.assertEquals(EchoAction.class, getTestCase().getActions().get(0).getClass());
        Assertions.assertEquals("action:echoAction", getTestCase().getActions().get(0).getName());
    }

    @Test
    public void testActionParserBrokenReference() {
        try {
            createApplicationContext("failed");
            Assertions.fail("Missing bean creation exception due to broken bean reference");
        } catch (BeanCreationException e) {
            Assertions.assertTrue(e.getMessage().contains("Could not resolve parent bean definition 'brokenRef'"));
        }
    }

    @Test
    public void testActionParserMissingReference() {
        try {
            createApplicationContext("missing-ref");
            Assertions.fail("Missing bean creation exception due to missing bean reference");
        } catch (BeanDefinitionStoreException e) {
            Assertions.assertEquals("Must specify proper reference attribute to bean", e.getCause().getMessage());
        }
    }
}
