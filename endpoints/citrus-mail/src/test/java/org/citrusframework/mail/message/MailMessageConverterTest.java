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

package org.citrusframework.mail.message;

import java.nio.charset.StandardCharsets;

import org.citrusframework.CitrusSettings;
import org.junit.jupiter.api.Test;

import static org.citrusframework.mail.message.MailMessageConverter.parseCharsetFromContentType;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @since 2.7.3
 */
public class MailMessageConverterTest {
    @Test
    public void testParseCharsetFromContentType() {
        assertEquals(StandardCharsets.UTF_8.name(), parseCharsetFromContentType("text/plain; charset=UTF-8"));
        assertEquals(StandardCharsets.UTF_8.name(), parseCharsetFromContentType("text/plain;charset=UTF-8"));
        assertEquals(StandardCharsets.ISO_8859_1.name(), parseCharsetFromContentType("text/*; charset=ISO-8859-1"));
        assertEquals(StandardCharsets.ISO_8859_1.name(), parseCharsetFromContentType("*/*;     charset=ISO-8859-1"));
        assertEquals(CitrusSettings.getFileEncoding(), parseCharsetFromContentType("text/plain"));
        assertEquals(CitrusSettings.getFileEncoding(), parseCharsetFromContentType("text/plain   ;    "));
    }
}
