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

package org.citrusframework.base.functions.core;

import java.util.Collections;

import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @since 2.6.2
 */
public class LoadMessageFunctionTest extends UnitTestSupport {

    private final LoadMessageFunction function = new LoadMessageFunction();

    private final Message message = new DefaultMessage("This is a sample message")
            .setHeader("operation", "sampleOperation");

    @Test
    public void testLoadMessagePayload() {
        context.getMessageStore().storeMessage("request", message);
        assertEquals("This is a sample message", function.execute(Collections.singletonList("request"), context));
        assertEquals("This is a sample message", function.execute(Collections.singletonList("request.body()"), context));
    }

    @Test
    public void testLoadMessageHeader() {
        context.getMessageStore().storeMessage("request", message);
        assertEquals("sampleOperation", function.execute(Collections.singletonList("request.header(operation)"), context));
        assertEquals("sampleOperation", function.execute(Collections.singletonList("request.header('operation')"), context));
    }

    @Test
    public void testLoadMessageHeaderEmpty() {
        context.getMessageStore().storeMessage("request", message);
        CitrusRuntimeException thrown = assertThrows(CitrusRuntimeException.class, () -> {
            function.execute(Collections.singletonList("request.header()"), context);
        });
        assertThat(thrown).hasMessageMatching("Missing header name.*");
    }

    @Test
    public void testLoadMessageHeaderUnknown() {
        context.getMessageStore().storeMessage("request", message);
        CitrusRuntimeException thrown = assertThrows(CitrusRuntimeException.class, () -> {
            function.execute(Collections.singletonList("request.header('wrong')"), context);
        });
        assertThat(thrown).hasMessageMatching("Failed to find header 'wrong'.*");
    }
}
