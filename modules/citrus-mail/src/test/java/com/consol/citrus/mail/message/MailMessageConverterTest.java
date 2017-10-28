package com.consol.citrus.mail.message;

import com.consol.citrus.Citrus;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

import static com.consol.citrus.mail.message.MailMessageConverter.parseCharsetFromContentType;
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
        assertEquals(parseCharsetFromContentType("text/plain"), Citrus.CITRUS_FILE_ENCODING);
        assertEquals(parseCharsetFromContentType("text/plain   ;    "), Citrus.CITRUS_FILE_ENCODING);
    }
}