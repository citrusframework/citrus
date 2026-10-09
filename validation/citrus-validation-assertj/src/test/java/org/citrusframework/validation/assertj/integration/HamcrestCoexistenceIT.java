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

package org.citrusframework.validation.assertj.integration;

import org.assertj.core.api.Condition;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfig;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.citrusframework.validation.assertj.AssertJ.satisfies;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.startsWith;

/**
 * Hamcrest and AssertJ validation on the same classpath, in the same receive action.
 * JSONPath reads numbers as {@link Long}, hence the long and {@link Number} typed checks.
 */
public class HamcrestCoexistenceIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    private static final String BODY = "{ \"a\": 7, \"b\": 7, \"c\": \"OK-1\", \"d\": \"OK-1\" }";

    @BindToRegistry
    public MessageQueue coexistenceQueue = new DefaultMessageQueue("coexistenceQueue");

    @CitrusEndpoint
    @DirectEndpointConfig(queue = "coexistenceQueue")
    public DirectEndpoint direct;

    @Test
    @CitrusTest
    public void shouldUseBothLibrariesInOneReceive() {
        sendOrder();

        $(receive(direct)
                .message()
                .type(MessageType.JSON)
                .header("hamcrestHeader", startsWith("OK"))
                .header("assertjHeader", satisfies(h -> assertThat(h).asString().startsWith("OK")))
                .header("hamcrestMatcherHeader", "@assertThat(startsWith(OK))@")
                .header("assertjMatcherHeader", "@assertj(startsWith('OK'))@")
                .validate(validation().jsonPath()
                        .expression("$.a", greaterThan(5L))
                        .expression("$.b", new Condition<Number>(b -> b.longValue() > 5, "greater than 5"))
                        .expression("$.c", "@assertThat(startsWith(OK))@")
                        .expression("$.d", "@assertj(startsWith('OK'))@")));
    }

    @Test(dataProvider = "failingChecks")
    @CitrusTest
    public void shouldConsultEachLibrary(String path, Object expected) {
        sendOrder();

        $(assertException()
                .exception(ValidationException.class)
                .when(receive(direct)
                        .message()
                        .type(MessageType.JSON)
                        .validate(validation().jsonPath().expression(path, expected))));
    }

    @DataProvider
    public Object[][] failingChecks() {
        return new Object[][] {
            { "$.a", greaterThan(10L) },
            { "$.b", new Condition<Number>(b -> b.longValue() > 10, "greater than 10") },
            { "$.c", "@assertThat(startsWith(FAILED))@" },
            { "$.d", "@assertj(startsWith('FAILED'))@" }
        };
    }

    private void sendOrder() {
        $(send(direct)
                .message()
                .type(MessageType.JSON)
                .body(BODY)
                .header("hamcrestHeader", "OK-1")
                .header("assertjHeader", "OK-1")
                .header("hamcrestMatcherHeader", "OK-1")
                .header("assertjMatcherHeader", "OK-1"));
    }
}
