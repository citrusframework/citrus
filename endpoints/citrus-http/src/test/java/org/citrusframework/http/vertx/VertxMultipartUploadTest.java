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
import java.util.Map;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class VertxMultipartUploadTest {
    public static byte[] fixture() {
        return ("--boundary\r\nContent-Disposition: form-data; name=\"tag\"\r\n\r\none\r\n"
                + "--boundary\r\nContent-Disposition: form-data; name=\"tag\"\r\n\r\n\r\n"
                + "--boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"a.bin\"\r\n"
                + "Content-Type: application/octet-stream\r\nX-Part: first\r\nX-Part: second\r\n\r\n"
                + "\u0000\u00ffembedded--boundary-not-a-delimiter\r\n"
                + "--boundary--\r\n").getBytes(StandardCharsets.ISO_8859_1);
    }
    static VertxMultipartUpload parse(byte[] body, String contentType, VertxMultipartUploadConverter converter) {
        var snapshot = new VertxHttpMessageMapper.RequestSnapshot("POST", "/", "/", null, "HTTP/1.1",
                Map.of("Content-Type", List.of(contentType)), body, "upload", 1);
        return (VertxMultipartUpload) VertxHttpMessageMapper.toHttpMessage(snapshot, new HttpEndpointConfiguration(),
                List.of(new VertxMessageBodyConverter<>(VertxMultipartUpload.class, converter))).getPayload();
    }
    @Test public void preservesOrderedFieldsBinaryAndHeaders() {
        var upload = parse(fixture(), "multipart/form-data; boundary=\"boundary\"", new VertxMultipartUploadConverter());
        assertEquals(upload.parts().size(), 3);
        assertEquals(upload.parts("tag").stream().map(VertxMultipartUpload.UploadPart::text).toList(), List.of("one", ""));
        var file = upload.parts("file").get(0);
        assertEquals(file.filename(), "a.bin");
        assertEquals(file.headers().get("x-part"), List.of("first", "second"));
        assertEquals(file.bytes(), "\u0000\u00ffembedded--boundary-not-a-delimiter".getBytes(StandardCharsets.ISO_8859_1));
        byte[] copy = file.bytes(); copy[0] = 55; assertEquals(file.bytes()[0], 0);
        expectThrows(UnsupportedOperationException.class, () -> file.headers().add("X-New", "value"));
        expectThrows(UnsupportedOperationException.class, () -> upload.parts().clear());
    }
    @Test public void rejectsMalformedAndBoundedInputs() {
        for (String contentType : List.of("multipart/form-data", "multipart/form-data; boundary=wrong"))
            expectThrows(RuntimeException.class, () -> parse(fixture(), contentType, new VertxMultipartUploadConverter()));
        byte[] truncated = java.util.Arrays.copyOf(fixture(), fixture().length - 20);
        expectThrows(RuntimeException.class, () -> parse(truncated, "multipart/form-data; boundary=boundary", new VertxMultipartUploadConverter()));
        for (var converter : List.of(new VertxMultipartUploadConverter(20, 128, 8192),
                new VertxMultipartUploadConverter(4096, 2, 8192), new VertxMultipartUploadConverter(4096, 128, 16)))
            expectThrows(RuntimeException.class, () -> parse(fixture(), "multipart/form-data; boundary=boundary", converter));
    }
    @Test public void preservesEmptyFilesAndUsesPartCharset() {
        byte[] body = ("--b\r\nContent-Disposition: form-data; name=\"empty\"; filename=\"\"\r\n\r\n\r\n"
                + "--b\r\nContent-Disposition: form-data; name=\"text\"\r\nContent-Type: text/plain; charset=ISO-8859-1\r\n\r\nGrüße\r\n--b--\r\n")
                .getBytes(StandardCharsets.ISO_8859_1);
        var upload = parse(body, "multipart/form-data; boundary=b", new VertxMultipartUploadConverter());
        assertEquals(upload.parts().get(0).filename(), "");
        assertEquals(upload.parts().get(0).bytes().length, 0);
        assertEquals(upload.parts("text").get(0).text(), "Grüße");
    }
    @Test public void acceptsExactByteLimitAndRejectsMissingDispositionName() {
        assertEquals(parse(fixture(), "multipart/form-data; boundary=boundary",
                new VertxMultipartUploadConverter(fixture().length, 3, 1024)).parts().size(), 3);
        byte[] invalid = "--b\r\nContent-Disposition: form-data\r\n\r\nvalue\r\n--b--\r\n".getBytes(StandardCharsets.UTF_8);
        expectThrows(RuntimeException.class, () -> parse(invalid, "multipart/form-data; boundary=b", new VertxMultipartUploadConverter()));
    }

    @Test public void leavesRequestsWithoutMultipartContentTypeOnTheDefaultPath() {
        var converter = new VertxMultipartUploadConverter();
        assertFalse(converter.canRead(VertxMultipartUpload.class, null));
        assertFalse(converter.canRead(VertxMultipartUpload.class, org.springframework.http.MediaType.TEXT_PLAIN));
        assertFalse(converter.canWrite(VertxMultipartUpload.class, org.springframework.http.MediaType.MULTIPART_FORM_DATA));
        var snapshot = new VertxHttpMessageMapper.RequestSnapshot("GET", "/", "/", null, "HTTP/1.1",
                Map.of(), new byte[0], "ordinary", 1);
        assertEquals(VertxHttpMessageMapper.toHttpMessage(snapshot, new HttpEndpointConfiguration(),
                List.of(new VertxMessageBodyConverter<>(VertxMultipartUpload.class, converter))).getPayload(), "");
    }

}
