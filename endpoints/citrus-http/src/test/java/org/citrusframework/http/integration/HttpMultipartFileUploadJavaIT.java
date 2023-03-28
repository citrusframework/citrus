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
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testng.annotations.Test;

import static org.citrusframework.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
@Test
public class HttpMultipartFileUploadJavaIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    public void httpMultipartFileUpload() {
        MultiValueMap<String, Object> files = new LinkedMultiValueMap<>();
        files.add("file", new ClassPathResource("org/citrusframework/ws/soapAttachment.txt"));

        given(http().client("echoHttpClient")
            .send()
            .post()
            .fork(true)
            .message()
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .body(files));

        when(http().server("echoHttpServer")
            .receive()
            .post()
            .message()
            .type(MessageType.PLAINTEXT)
            .contentType("@startsWith(multipart/form-data)@")
            .body("@contains(This is an attachment!)@"));

       then(http().server("echoHttpServer")
           .send()
           .response(HttpStatus.OK));

       then(http().client("echoHttpClient")
            .receive()
            .response(HttpStatus.OK));
    }
}
