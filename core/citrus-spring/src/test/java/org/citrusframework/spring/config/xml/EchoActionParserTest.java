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

import org.citrusframework.actions.EchoAction;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EchoActionParserTest extends AbstractActionParserTest<EchoAction> {

    @Test
    public void testEchoActionParser() {
        assertActionCount(1);
        assertActionClassAndName(EchoAction.class, "echo");

        EchoAction action = getNextTestActionFromTest();
        Assertions.assertEquals("This action prints messages to console logger", action.getDescription());
        Assertions.assertEquals("This is a test!", action.getMessage());
    }
}
