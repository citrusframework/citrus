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

import org.citrusframework.actions.InputAction;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InputActionParserTest extends AbstractActionParserTest<InputAction> {

    @Test
    public void testInputActionParser() {
        assertActionCount(4);
        assertActionClassAndName(InputAction.class, "input");

        InputAction action = getNextTestActionFromTest();
        Assertions.assertEquals("Press return key to continue ...", action.getMessage());
        Assertions.assertNull(action.getValidAnswers());
        Assertions.assertEquals("userinput", action.getVariable());

        action = getNextTestActionFromTest();
        Assertions.assertEquals("Do you want to continue?", action.getMessage());
        Assertions.assertNull(action.getValidAnswers());
        Assertions.assertEquals("userinput", action.getVariable());

        action = getNextTestActionFromTest();
        Assertions.assertEquals("Do you want to continue?", action.getMessage());
        Assertions.assertEquals("yes/no", action.getValidAnswers());
        Assertions.assertEquals("userinput", action.getVariable());

        action = getNextTestActionFromTest();
        Assertions.assertEquals("Do you want to continue?", action.getMessage());
        Assertions.assertEquals("y/n", action.getValidAnswers());
        Assertions.assertEquals("inputVar", action.getVariable());
    }
}
