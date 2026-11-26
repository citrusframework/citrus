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

package org.citrusframework.http.vertx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.util.MultiValueMap;

/** Spring form conversion without a Servlet request or response. */
final class VertxHttpFormConverter {
    private VertxHttpFormConverter() { }

    static boolean isForm(String contentType) {
        return contentType != null && MediaType.APPLICATION_FORM_URLENCODED
                .isCompatibleWith(MediaType.parseMediaType(contentType));
    }

    static boolean isWritable(String contentType) {
        if (contentType == null) return false;
        MediaType type = MediaType.parseMediaType(contentType);
        return MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(type)
                || MediaType.MULTIPART_FORM_DATA.isCompatibleWith(type)
                || MediaType.MULTIPART_MIXED.isCompatibleWith(type);
    }

    static MultiValueMap<String, String> read(byte[] bytes, String contentType, Charset charset) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        FormHttpMessageConverter converter = new FormHttpMessageConverter();
        converter.setCharset(charset);
        try {
            return converter.read(null, new HttpInputMessage() {
                @Override public InputStream getBody() { return new ByteArrayInputStream(bytes); }
                @Override public HttpHeaders getHeaders() { return headers; }
            });
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read form body", e);
        }
    }

    static byte[] write(MultiValueMap<String, ?> fields, Map<String, List<String>> wireHeaders,
                        String contentType, Charset charset) {
        HttpHeaders headers = new HttpHeaders();
        wireHeaders.forEach((name, values) -> headers.put(name, new ArrayList<>(values)));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        FormHttpMessageConverter converter = new FormHttpMessageConverter();
        converter.setCharset(charset);
        try {
            converter.write(fields, MediaType.parseMediaType(contentType), new HttpOutputMessage() {
                @Override public OutputStream getBody() { return bytes; }
                @Override public HttpHeaders getHeaders() { return headers; }
            });
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write form body", e);
        }
        // The converter chooses the multipart boundary; its Content-Type must reach the wire.
        wireHeaders.clear();
        headers.headerSet().forEach(entry -> wireHeaders.put(entry.getKey(), new ArrayList<>(entry.getValue())));
        return bytes.toByteArray();
    }
}
