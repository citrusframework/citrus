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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.citrusframework.http.message.HttpMessage;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class VertxMultipartUploadIT {
    @Test public void parsesGzipUploadsAndRejectsInvalidUploadsBeforeAdapter() throws Exception {
        var server = new VertxHttpServer(); server.setPort(0);
        server.setMessageBodyConverters(List.of(new VertxMessageBodyConverter<>(VertxMultipartUpload.class,
                new VertxMultipartUploadConverter(2048, 3, 1024))));
        var calls = new AtomicInteger();
        server.setEndpointAdapter(VertxPrototypeAdapters.delegate(message -> {
            var upload = (VertxMultipartUpload) message.getPayload();
            assertEquals(upload.parts("file").get(0).bytes()[1], (byte) 255);
            calls.incrementAndGet(); return new HttpMessage(upload.parts("tag").get(0).text());
        }));
        server.initialize(); server.start();
        try {
            var valid = VertxHttpGzipIT.send(server.getActualPort(), VertxHttpGzipIT.gzip(VertxMultipartUploadTest.fixture()),
                    "multipart/form-data; boundary=boundary", "gzip", null);
            assertEquals(valid.statusCode(), 200);
            assertEquals(new String(valid.body(), java.nio.charset.StandardCharsets.UTF_8), "one");
            for (byte[] body : List.of(new byte[0], new byte[2049], java.util.Arrays.copyOf(VertxMultipartUploadTest.fixture(), 100)))
                assertEquals(VertxHttpGzipIT.send(server.getActualPort(), body,
                        "multipart/form-data; boundary=boundary", null, null).statusCode(), 400);
            assertEquals(calls.get(), 1);
        } finally { server.stop(); server.destroy(); }
    }
}
