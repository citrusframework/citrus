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

package org.citrusframework.http.integration;

import java.util.List;
import java.util.Map;

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.message.MessageType;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.util.LinkedMultiValueMap;
import org.junit.jupiter.api.Test;

@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
@Isolated("HTTP ITs share singleton endpoints and Citrus Spring context state under parallel classes")
public class HttpMultipartFileUploadJavaIT implements TestActionSupport {

    @Test
    @CitrusTest
    public void httpMultipartFileUpload(@CitrusResource GherkinTestActionRunner runner) {
        Map<String, List<Object>> files = new LinkedMultiValueMap<>();
        files.put("file", List.of(new ClassPathResource("org/citrusframework/ws/soapAttachment.txt")));

        runner.given(http().client("echoHttpClient")
            .send()
            .post()
            .fork(true)
            .message()
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .body(files));

        runner.when(http().server("echoHttpServer")
            .receive()
            .post()
            .message()
            .type(MessageType.PLAINTEXT)
            .contentType("@startsWith(multipart/form-data)@")
            .body("@contains(This is an attachment!)@"));

       runner.then(http().server("echoHttpServer")
           .send()
           .response(HttpStatus.OK));

       runner.then(http().client("echoHttpClient")
            .receive()
            .response(HttpStatus.OK));
    }
}
