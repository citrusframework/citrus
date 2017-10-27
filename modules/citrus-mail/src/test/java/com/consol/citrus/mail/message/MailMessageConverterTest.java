package com.consol.citrus.mail.message;

import com.consol.citrus.Citrus;
import org.testng.annotations.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

import static com.consol.citrus.mail.message.MailMessageConverter.parseCharsetFromContentType;
import static org.testng.Assert.assertEquals;

/**
 * @author Christian Guggenmos
 * @since 2017-10-27
 */
public class MailMessageConverterTest {
    @Test
    public void testParseCharsetFromContentType() throws Exception {
        assertEquals(parseCharsetFromContentType("text/plain; charset=UTF-8"), StandardCharsets.UTF_8);
        assertEquals(parseCharsetFromContentType("text/plain;charset=UTF-8"), StandardCharsets.UTF_8);
        assertEquals(parseCharsetFromContentType("text/*; charset=ISO-8859-1"), StandardCharsets.ISO_8859_1);
        assertEquals(parseCharsetFromContentType("*/*;     charset=ISO-8859-1"), StandardCharsets.ISO_8859_1);
        assertEquals(parseCharsetFromContentType("text/plain"), Charset.forName(Citrus.CITRUS_FILE_ENCODING));
        assertEquals(parseCharsetFromContentType("text/plain   ;    "), Charset.forName(Citrus.CITRUS_FILE_ENCODING));
    }

    @Test(expectedExceptions = {UnsupportedCharsetException.class})
    public void testParseCharsetFromContentTypeException() {
        parseCharsetFromContentType("text/plain;charset=1234");
    }

}