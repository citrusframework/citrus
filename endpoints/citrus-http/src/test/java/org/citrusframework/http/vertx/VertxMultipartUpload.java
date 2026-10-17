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

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.HttpHeaders;

/** Ordered, detached multipart values; filenames are metadata, never filesystem paths. */
public record VertxMultipartUpload(List<UploadPart> parts) {
    public VertxMultipartUpload { parts = List.copyOf(parts); }

    public List<UploadPart> parts(String name) {
        return parts.stream().filter(part -> part.name().equals(name)).toList();
    }

    public record UploadPart(String name, String filename, HttpHeaders headers, byte[] bytes) {
        public UploadPart {
            HttpHeaders copy = new HttpHeaders();
            headers.headerSet().forEach(entry -> copy.put(entry.getKey(), List.copyOf(entry.getValue())));
            headers = HttpHeaders.readOnlyHttpHeaders(copy);
            bytes = bytes.clone();
        }
        @Override public byte[] bytes() { return bytes.clone(); }
        public String text() {
            var type = headers.getContentType();
            return new String(bytes, type != null && type.getCharset() != null
                    ? type.getCharset() : StandardCharsets.UTF_8);
        }
    }
}
