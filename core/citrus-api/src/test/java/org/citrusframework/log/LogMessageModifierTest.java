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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

public class LogMessageModifierTest {

    @Mock
    private Message message;

    @BeforeTest
    public void setupMocks() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testMaskBody() {
        when(message.getPayload(String.class)).thenReturn("foo");
        Assert.assertEquals(new MockLogModifier("bar").maskBody(message), "bar");
    }

    @Test
    public void testMaskHeaders() {
        when(message.getHeaders()).thenReturn(Collections.singletonMap("key", "value"));

        MockLogModifier modifier = new MockLogModifier("key=value");
        Assert.assertEquals(modifier.maskHeaders(message), Collections.singletonMap("key", "value"));

        modifier = new MockLogModifier("key=masked");
        Assert.assertEquals(modifier.maskHeaders(message), Collections.singletonMap("key", CitrusLogSettings.getLogMaskValue()));
    }

    private static class MockLogModifier implements LogMessageModifier {

        private final String result;

        private MockLogModifier(String result) {
            this.result = result;
        }

        @Override
        public String mask(String statement) {
            return result;
        }
    }
}
