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

package org.citrusframework.base.message;

import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.message.MessageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MessageTypeTest extends UnitTestSupport {

    @Test
    public void testKnowsMessageType() {
        Assertions.assertEquals(true, MessageType.knows("xml"));
        Assertions.assertEquals(true, MessageType.knows("XML"));

        Assertions.assertEquals(true, MessageType.knows("PLAINTEXT"));
        Assertions.assertEquals(true, MessageType.knows("plaintext"));
        Assertions.assertEquals(true, MessageType.knows("json"));
        Assertions.assertEquals(true, MessageType.knows("csv"));

        Assertions.assertEquals(false, MessageType.knows("msexcel"));
        Assertions.assertEquals(false, MessageType.knows(""));
    }
}
