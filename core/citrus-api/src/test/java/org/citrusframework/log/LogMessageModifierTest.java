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

package org.citrusframework.log;

import java.util.Collections;

import org.citrusframework.message.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LogMessageModifierTest {

    @Mock
    private Message message;

    @Test
    public void testMaskBody() {
        when(message.getPayload(String.class)).thenReturn("foo");
        Assertions.assertEquals("bar", new MockLogModifier("bar").maskBody(message));
    }

    @Test
    public void testMaskHeaders() {
        when(message.getHeaders()).thenReturn(Collections.singletonMap("key", "value"));

        MockLogModifier modifier = new MockLogModifier("key=value");
        Assertions.assertEquals(Collections.singletonMap("key", "value"), modifier.maskHeaders(message));

        modifier = new MockLogModifier("key=masked");
        Assertions.assertEquals(Collections.singletonMap("key", CitrusLogSettings.getLogMaskValue()), modifier.maskHeaders(message));
    }

    private record MockLogModifier(String result) implements LogMessageModifier {

        @Override
            public String mask(String statement) {
                return result;
            }
        }
}
