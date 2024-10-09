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
import org.testng.annotations.Test;

import static org.citrusframework.mail.message.MailMessageConverter.parseCharsetFromContentType;
import static org.testng.Assert.assertEquals;

/**
 * @since 2.7.3
 */
public class MailMessageConverterTest {
    @Test
    public void testParseCharsetFromContentType() throws Exception {
        assertEquals(parseCharsetFromContentType("text/plain; charset=UTF-8"), StandardCharsets.UTF_8.name());
        assertEquals(parseCharsetFromContentType("text/plain;charset=UTF-8"), StandardCharsets.UTF_8.name());
        assertEquals(parseCharsetFromContentType("text/*; charset=ISO-8859-1"), StandardCharsets.ISO_8859_1.name());
        assertEquals(parseCharsetFromContentType("*/*;     charset=ISO-8859-1"), StandardCharsets.ISO_8859_1.name());
        assertEquals(parseCharsetFromContentType("text/plain"), CitrusSettings.CITRUS_FILE_ENCODING);
        assertEquals(parseCharsetFromContentType("text/plain   ;    "), CitrusSettings.CITRUS_FILE_ENCODING);
    }
}
