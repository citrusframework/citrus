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
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.citrusframework.validation.assertj.AssertJ.conditionOf;
import static org.citrusframework.validation.assertj.AssertJ.satisfies;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssertJJavaDslIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    @BindToRegistry
    public MessageQueue assertjQueue = new DefaultMessageQueue("assertjQueue");

    @CitrusEndpoint
    @DirectEndpointConfig(queue = "assertjQueue")
    public DirectEndpoint direct;

    @Test
    @CitrusTest
    public void shouldValidateJsonPathValuesAndHeaders() {
        $(send(direct)
                .message()
                .type(MessageType.JSON)
                .body("{ \"qty\": 7, \"status\": \"OK-1\", \"note\": null }")
                .header("count", "42")
                .header("region", "eu"));

        $(receive(direct)
                .message()
                .type(MessageType.JSON)
                .header("count", satisfies(Integer.class, c -> assertThat(c).isBetween(40, 50)))
                .header("region", satisfies(r -> assertEquals("eu", r)))
                .validate(validation().jsonPath()
                        .expression("$.qty", satisfies(Integer.class, q -> assertThat(q).isGreaterThan(5)))
                        .expression("$.status", new Condition<String>(s -> s.startsWith("OK"), "starts with OK"))
                        .expression("$.note", satisfies(n -> assertThat(n).isNull()))));
    }

    @Test
    @CitrusTest
    public void shouldValidateXpathValues() {
        $(send(direct)
                .message()
                .body("<Order><Status>OK-123</Status><Qty>7</Qty></Order>"));

        $(receive(direct)
                .message()
                .validate(validation().xpath()
                        .expression("/Order/Status", satisfies(s -> assertThat(s).asString().startsWith("OK").endsWith("123")))
                        .expression("/Order/Qty", satisfies(Integer.class, q -> assertThat(q).isBetween(1, 10)))));
    }

    @Test
    @CitrusTest
    public void shouldRunManualMigrationExamples() {
        $(send(direct)
                .message()
                .body("<TestRequest><Error/><Status>success</Status><OrderType>a</OrderType><OrderType>b</OrderType><OrderType>c</OrderType></TestRequest>"));

        $(receive(direct)
                .message()
                .validate(validation().xpath()
                        .expression("/TestRequest/Error", satisfies(e -> assertThat(e).satisfiesAnyOf(v -> assertThat(v).isNull(), v -> assertThat(v).asString().isEmpty())))
                        .expression("number:count(/TestRequest/Status[.='success'])", satisfies(Double.class, s -> assertThat(s).isGreaterThan(0.0)))
                        .expression("node-set:/TestRequest/OrderType", satisfies(t -> assertThat(t).asList().hasSize(3)))));
    }

    @Test
    @CitrusTest
    public void shouldReportAssertJDescriptionOnFailure() {
        $(send(direct)
                .message()
                .type(MessageType.JSON)
                .body("{ \"qty\": 3 }"));

        $(assertException()
                .exception(ValidationException.class)
                .message("@assertj(contains('AssertJ validation failed').contains('to be greater than'))@")
                .when(receive(direct)
                        .message()
                        .type(MessageType.JSON)
                        .validate(validation().jsonPath()
                                .expression("$.qty", satisfies(Integer.class, q -> assertThat(q).isGreaterThan(5))))));
    }

    @Test(timeOut = 30000L)
    @CitrusTest
    public void shouldUseAssertJConditionsInContainers() {
        variable("count", "0");

        $(iterate()
                .condition(conditionOf(i -> assertThat(i).isLessThan(3)))
                .index("i")
                .actions(echo("iteration ${i}")));

        $(repeat()
                .until(conditionOf(Integer.class, "${count}", c -> assertThat(c).isEqualTo(3)))
                .index("r")
                .actions(context -> context.setVariable("count", String.valueOf(Integer.parseInt(context.getVariable("count")) + 1))));

        $(waitFor()
                .condition(conditionOf(Integer.class, "${count}", c -> assertThat(c).isEqualTo(3)))
                .milliseconds(1000L)
                .interval(50L));

        $(echo("count is ${count}"));
    }
}
