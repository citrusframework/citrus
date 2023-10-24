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

package org.citrusframework.http.integration;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.message.MessageType;
import org.citrusframework.message.ZipMessage;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import static org.citrusframework.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
@Test
public class HttpServerZipFileJavaIT extends TestNGCitrusSpringSupport {

    public static final String APPLICATION_ZIP_VALUE = "application/zip";

    @CitrusTest
    public void httpServerZipFile() {
        ZipMessage zipMessage = new ZipMessage().addEntry(Resources.fromClasspath("schemas"));

        given(http().client("echoHttpClient")
                .send()
                .post()
                .fork(true)
                .message(zipMessage)
                .type(MessageType.BINARY)
                .contentType(APPLICATION_ZIP_VALUE)
                .accept(APPLICATION_ZIP_VALUE));

        when(http().server("echoHttpServer")
                    .receive()
                    .post("/echo")
                    .message(zipMessage)
                    .type(MessageType.BINARY)
                    .contentType(APPLICATION_ZIP_VALUE)
                    .accept(APPLICATION_ZIP_VALUE));

        then(http().server("echoHttpServer")
                .send()
                .response(HttpStatus.OK)
                .message(zipMessage)
                .type(MessageType.BINARY)
                .contentType(APPLICATION_ZIP_VALUE));

        then(http().client("echoHttpClient")
                .receive()
                .response(HttpStatus.OK)
                .message(zipMessage)
                .type(MessageType.BINARY)
                .contentType(APPLICATION_ZIP_VALUE));
    }
}
