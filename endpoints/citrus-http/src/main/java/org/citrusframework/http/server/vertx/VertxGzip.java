/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citrusframework.http.server.vertx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Provides the gzip content coding for the Vert.x based Http server. Request bodies that are marked
 * as gzip encoded are decoded, response bodies are compressed when the client accepts gzip. This
 * mirrors the behaviour of the gzip servlet filter of the Jetty based server.
 */
class VertxGzip {

    private VertxGzip() {
    }

    /**
     * Checks if the given content encoding header value announces a gzip encoded payload.
     *
     * @param contentEncoding the value of the content encoding header, may be {@code null}.
     * @return {@code true} if the payload is gzip encoded.
     */
    static boolean isGzipEncoding(String contentEncoding) {
        return contentEncoding != null && contentEncoding.contains("gzip");
    }

    /**
     * Decodes the given gzip compressed payload.
     *
     * @param body the compressed payload.
     * @return the plain payload.
     * @throws IOException when the payload is no valid gzip stream.
     */
    static byte[] decode(byte[] body) throws IOException {
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(body))) {
            return gzipInputStream.readAllBytes();
        }
    }

    /**
     * Compresses the given payload with gzip.
     *
     * @param body the plain payload.
     * @return the gzip compressed payload.
     * @throws IOException when the payload could not be compressed.
     */
    static byte[] encode(byte[] body) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)) {
            gzipOutputStream.write(body);
        }

        return outputStream.toByteArray();
    }
}
