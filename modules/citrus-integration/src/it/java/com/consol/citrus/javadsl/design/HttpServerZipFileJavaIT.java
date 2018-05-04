/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.javadsl.design;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.message.ZipMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class HttpServerZipFileJavaIT extends TestNGCitrusTestDesigner {
    
    @CitrusTest
    public void httpServerZipFile() {
        ZipMessage zipMessage = new ZipMessage().addEntry(new ClassPathResource("com/consol/citrus/schema"));

        http().client("echoHttpClient")
                .send()
                .post()
                .fork(true)
                .messageType(MessageType.BINARY)
                .message(zipMessage)
                .contentType("application/zip")
                .accept("application/zip");

        http().server("echoHttpServer")
                    .receive()
                    .post("/test")
                    .messageType(MessageType.BINARY)
                    .message(zipMessage)
                    .contentType("application/zip")
                    .accept("application/zip");

        http().server("echoHttpServer")
                .send()
                .response(HttpStatus.OK)
                .messageType(MessageType.BINARY)
                .message(zipMessage)
                .contentType("application/zip");

        http().client("echoHttpClient")
                .receive()
                .response(HttpStatus.OK)
                .messageType(MessageType.BINARY)
                .message(zipMessage)
                .contentType("application/zip");
    }
}