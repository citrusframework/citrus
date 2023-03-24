package org.citrusframework.xml;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import org.citrusframework.CitrusSettings;

/**
 * A simple JAXB stream source representation of a static String content. Can be read many times and uses default encoding
 * set via Citrus settings.
 *
 * @author Christoph Deppisch
 */
public class StringSource extends StreamSource {

    private final String content;
    private final String encoding;

    /**
     * Constructor using source content as String.
     * @param content the content
     */
    public StringSource(String content) {
        this(content, CitrusSettings.CITRUS_FILE_ENCODING);
    }

    /**
     * Constructor using source content as String and encoding.
     * @param content the content
     */
    public StringSource(String content, String encoding) {
        this.content = content;
        this.encoding = encoding;
    }

    @Override
    public Reader getReader() {
        return new StringReader(content);
    }

    @Override
    public InputStream getInputStream() {
        try {
            return new ByteArrayInputStream(content.getBytes(encoding));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return content;
    }

    /**
     * Obtains the content.
     * @return
     */
    public String getContent() {
        return content;
    }

    /**
     * Obtains the encoding.
     * @return
     */
    public String getEncoding() {
        return encoding;
    }
}
