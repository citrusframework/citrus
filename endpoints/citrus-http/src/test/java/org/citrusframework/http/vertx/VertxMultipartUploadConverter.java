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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.http.codec.multipart.DefaultPartHttpMessageReader;
import org.springframework.http.codec.multipart.Part;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;

/** Opt-in request-only multipart parsing for the prototype. */
public final class VertxMultipartUploadConverter extends AbstractHttpMessageConverter<VertxMultipartUpload> {
    private final int maxBytes;
    private final int maxParts;
    private final int maxHeaderBytes;

    public VertxMultipartUploadConverter() { this(1024 * 1024, 128, 8192); }
    public VertxMultipartUploadConverter(int maxBytes, int maxParts, int maxHeaderBytes) {
        super(MediaType.MULTIPART_FORM_DATA);
        if (maxBytes <= 0 || maxBytes == Integer.MAX_VALUE || maxParts <= 0 || maxHeaderBytes <= 0)
            throw new IllegalArgumentException("Multipart limits must be positive and byte limit below Integer.MAX_VALUE");
        this.maxBytes = maxBytes; this.maxParts = maxParts; this.maxHeaderBytes = maxHeaderBytes;
    }
    @Override protected boolean supports(Class<?> type) { return type == VertxMultipartUpload.class; }
    @Override public boolean canRead(Class<?> type, MediaType mediaType) {
        return mediaType != null && super.canRead(type, mediaType);
    }
    @Override public boolean canWrite(Class<?> type, MediaType mediaType) { return false; }
    @Override protected void writeInternal(VertxMultipartUpload upload, HttpOutputMessage output) {
        throw new UnsupportedOperationException("Use form/multipart map conversion for responses");
    }
    @Override protected VertxMultipartUpload readInternal(Class<? extends VertxMultipartUpload> type,
                                                          HttpInputMessage input) throws IOException {
        byte[] body = input.getBody().readNBytes(maxBytes + 1);
        if (body.length > maxBytes) throw new IllegalArgumentException("Multipart input exceeds byte limit");
        var reader = new DefaultPartHttpMessageReader();
        // Total input is bounded above, so no individual part can reach the disk spill threshold.
        reader.setMaxInMemorySize(maxBytes);
        reader.setMaxDiskUsagePerPart(0);
        reader.setMaxHeadersSize(maxHeaderBytes);
        reader.setMaxParts(maxParts);
        reader.setHeadersCharset(StandardCharsets.UTF_8);
        var message = new ReactiveHttpInputMessage() {
            @Override public HttpHeaders getHeaders() { return input.getHeaders(); }
            @Override public Flux<DataBuffer> getBody() {
                return Flux.defer(() -> Flux.fromIterable(parserBuffers(body, input.getHeaders())));
            }
        };
        var parts = reader.read(ResolvableType.forClass(Part.class), message, Map.of())
                .concatMap(part -> DataBufferUtils.join(part.content(), maxBytes)
                        .switchIfEmpty(Mono.fromSupplier(() -> DefaultDataBufferFactory.sharedInstance.wrap(new byte[0])))
                        .map(buffer -> {
                            try {
                                byte[] bytes = new byte[buffer.readableByteCount()];
                                buffer.read(bytes);
                                var disposition = part.headers().getContentDisposition();
                                if (!"form-data".equalsIgnoreCase(disposition.getType()) || disposition.getName() == null)
                                    throw new IllegalArgumentException("Each multipart part requires a form-data name");
                                return new VertxMultipartUpload.UploadPart(disposition.getName(),
                                        disposition.getFilename(), part.headers(), bytes);
                            } finally { DataBufferUtils.release(buffer); }
                        }))
                .collectList().block();
        return new VertxMultipartUpload(parts);
    }

    private List<DataBuffer> parserBuffers(byte[] body, HttpHeaders headers) {
        String boundary = headers.getContentType().getParameter("boundary");
        if (boundary == null || boundary.isEmpty()) throw new IllegalArgumentException("Missing multipart boundary");
        if (boundary.startsWith("\"") && boundary.endsWith("\"")) boundary = boundary.substring(1, boundary.length() - 1);
        if (boundary.isEmpty() || boundary.length() > 70 || boundary.indexOf('\r') >= 0 || boundary.indexOf('\n') >= 0)
            throw new IllegalArgumentException("Invalid multipart boundary");
        String wire = new String(body, StandardCharsets.ISO_8859_1);
        String marker = "\r\n--" + boundary;
        List<DataBuffer> buffers = new ArrayList<>();
        int start = 0;
        // Spring 7.0.9 loses empty parts when their delimiter is wholly in one buffer.
        // Split inside each delimiter so the parser emits its empty final body token.
        for (int at = wire.indexOf(marker); at >= 0; at = wire.indexOf(marker, at + marker.length())) {
            if (buffers.size() >= maxParts) throw new IllegalArgumentException("Multipart input exceeds part limit");
            int end = at + 1;
            buffers.add(DefaultDataBufferFactory.sharedInstance.wrap(java.nio.ByteBuffer.wrap(body, start, end - start)));
            start = end;
        }
        buffers.add(DefaultDataBufferFactory.sharedInstance.wrap(java.nio.ByteBuffer.wrap(body, start, body.length - start)));
        return buffers;
    }

}
