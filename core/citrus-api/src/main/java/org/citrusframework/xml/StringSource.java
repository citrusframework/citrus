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
