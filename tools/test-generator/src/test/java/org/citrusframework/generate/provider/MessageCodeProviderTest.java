/*
 * Copyright 2006-2019 the original author or authors.
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

package org.citrusframework.generate.provider;

import org.citrusframework.message.DefaultMessage;
import com.squareup.javapoet.CodeBlock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageCodeProviderTest {

    private final MessageCodeProvider messageCodeProvider = new MessageCodeProvider();

    private final DefaultMessage message = new DefaultMessage();
    private final CodeBlock.Builder code = CodeBlock.builder();

    @Test
    void testHeaderCodeIsGenerated(){

        //GIVEN
        message.setHeader("foo","bar");
        final String expectedCode = ".message()\n.header(\"foo\", \"bar\")\n";

        //WHEN
        messageCodeProvider.provideHeaderAndPayload(code, message);

        //THEN
        assertEquals(expectedCode, code.build().toString());
    }

    @Test
    void testPayloadCodeIsGenerated(){

        //GIVEN
        message.setPayload("foo");
        final String expectedCode = ".message()\n.body(\"foo\")\n";

        //WHEN
        messageCodeProvider.provideHeaderAndPayload(code, message);

        //THEN
        assertEquals(expectedCode, code.build().toString());
    }

}
