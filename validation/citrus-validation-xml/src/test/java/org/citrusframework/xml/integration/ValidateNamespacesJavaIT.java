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

package org.citrusframework.xml.integration;

import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

@CitrusSpringSupport
@ContextConfiguration(classes = {CitrusSpringConfig.class})
public class ValidateNamespacesJavaIT implements TestActionSupport {

    @Test
    @CitrusTest
    public void validateNamespaces(@CitrusResource TestCaseRunner runner) {
        runner.run(echo("Test: Success with single namespace validation"));

        runner.run(send("direct:test")
            .message()
            .body("<trq:TestRequest xmlns:trq=\"http://citrusframework.org/schemas/test\">" +
                            "<Message>Hello</Message>" +
                        "</trq:TestRequest>"));

        runner.run(receive("direct:test")
            .message()
            .body("<trq:TestRequest xmlns:trq=\"http://citrusframework.org/schemas/test\">" +
                    "<Message>Hello</Message>" +
                "</trq:TestRequest>")
            .validate(validation().xml()
                    .schemaValidation(false)
                    .namespace("trq", "http://citrusframework.org/schemas/test"))
            .timeout(5000));

        runner.run(echo("Test: Success with multiple namespace validations"));

        runner.run(send("direct:test")
            .message()
            .body("<trq:TestRequest xmlns:trq=\"http://citrusframework.org/schemas/test\" xmlns:msg=\"http://citrusframework.org/schemas/message\">" +
                            "<msg:Message>Hello</msg:Message>" +
                        "</trq:TestRequest>"));

        runner.run(receive("direct:test")
            .message()
            .body("<trq:TestRequest xmlns:trq=\"http://citrusframework.org/schemas/test\" xmlns:msg=\"http://citrusframework.org/schemas/message\">" +
                    "<msg:Message>Hello</msg:Message>" +
                "</trq:TestRequest>")
            .validate(validation().xml()
                    .schemaValidation(false)
                    .namespace("trq", "http://citrusframework.org/schemas/test")
                    .namespace("msg", "http://citrusframework.org/schemas/message"))
            .timeout(5000));

        runner.run(echo("Test: Success with multiple nested namespace validations"));

        runner.run(send("direct:test")
            .message()
            .body("<trq:TestRequest xmlns:trq=\"http://citrusframework.org/schemas/test\">" +
                            "<msg:Message xmlns:msg=\"http://citrusframework.org/schemas/message\">Hello</msg:Message>" +
                        "</trq:TestRequest>"));

        runner.run(receive("direct:test")
            .message()
            .body("<trq:TestRequest xmlns:trq=\"http://citrusframework.org/schemas/test\">" +
                    "<msg:Message xmlns:msg=\"http://citrusframework.org/schemas/message\">Hello</msg:Message>" +
                "</trq:TestRequest>")
            .validate(validation().xml()
                        .schemaValidation(false)
                        .namespace("trq", "http://citrusframework.org/schemas/test")
                        .namespace("msg", "http://citrusframework.org/schemas/message"))
            .timeout(5000));

        runner.run(echo("Test: Failure because of missing namespace"));

        runner.run(send("direct:test")
            .message()
            .body("<trq:TestRequest xmlns:trq=\"http://citrusframework.org/schemas/test\">" +
                            "<Message>Hello</Message>" +
                        "</trq:TestRequest>"));

        runner.run(assertException()
            .exception(ValidationException.class)
            .when(receive("direct:test")
                .message()
                .body("<trq:TestRequest xmlns:trq=\"http://citrusframework.org/schemas/test\">" +
                        "<Message>Hello</Message>" +
                    "</trq:TestRequest>")
                .validate(validation().xml()
                            .schemaValidation(false)
                            .namespace("trq", "http://citrusframework.org/schemas/test")
                            .namespace("missing", "http://citrusframework.org/schemas/missing"))
                .timeout(5000)
        ));

        echo("Test: Failure because of wrong namespace prefix");

        send("direct:test")
            .message()
            .body("<wrong:TestRequest xmlns:wrong=\"http://citrusframework.org/schemas/test\">" +
                            "<Message>Hello</Message>" +
                        "</wrong:TestRequest>");

        assertException()
            .exception(ValidationException.class)
            .when(receive("direct:test")
                .message()
                .body("<trq:TestRequest xmlns:trq=\"http://citrusframework.org/schemas/test\">" +
                        "<Message>Hello</Message>" +
                    "</trq:TestRequest>")
                .validate(validation().xml()
                        .schemaValidation(false)
                        .namespace("trq", "http://citrusframework.org/schemas/test"))
                .timeout(5000)
        );

        echo("Test: Failure because of wrong namespace uri");

        send("direct:test")
            .message()
            .body("<trq:TestRequest xmlns:trq=\"http://citrusframework.org/schemas/wrong\">" +
                            "<Message>Hello</Message>" +
                        "</trq:TestRequest>");

        assertException()
            .exception(ValidationException.class)
            .when(receive("direct:test")
                .message()
                .body("<trq:TestRequest xmlns:trq=\"http://citrusframework.org/schemas/test\">" +
                        "<Message>Hello</Message>" +
                    "</trq:TestRequest>")
                .validate(validation().xml()
                        .schemaValidation(false)
                        .namespace("trq", "http://citrusframework.org/schemas/test"))
                .timeout(5000)
        );
    }
}
