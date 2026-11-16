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

package org.citrusframework.spring.boot;

import org.citrusframework.TestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.endpoint.direct.DirectEndpointBuilder;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.MessageQueue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

@CitrusSpringSupport
@SpringBootTest(classes = CitrusSpringBootIT.TestApplication.class)
class CitrusSpringBootIT implements TestActionSupport {

    @Autowired
    private DirectEndpoint direct;

    @Test
    @CitrusTest
    void autoConfiguresCitrusTest(@CitrusResource TestActionRunner actions) {
        actions.$(send().endpoint(direct)
                .message()
                .body("Hello from Spring Boot!"));

        actions.$(receive().endpoint(direct)
                .message()
                .body("Hello from Spring Boot!"));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {

        @Bean
        MessageQueue directQueue() {
            return new DefaultMessageQueue("directQueue");
        }

        @Bean
        DirectEndpoint direct(MessageQueue directQueue) {
            return new DirectEndpointBuilder()
                    .queue(directQueue)
                    .build();
        }
    }
}
