package org.citrusframework.mail.message;

import java.nio.charset.StandardCharsets;

import org.citrusframework.CitrusSettings;
import org.testng.annotations.Test;

import static org.citrusframework.mail.message.MailMessageConverter.parseCharsetFromContentType;
import static org.testng.Assert.assertEquals;

/**
 * @author Christian Guggenmos
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
