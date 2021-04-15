/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.integration;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.EchoAction.Builder.echo;
import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;
import static com.consol.citrus.container.Assert.Builder.assertException;
import static com.consol.citrus.dsl.XmlSupport.xml;

/**
 * @author Christoph Deppisch
 */
@Test
public class ValidateNamespacesJavaIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    public void validateNamespaces() {
        $(echo("Test: Success with single namespace validation"));

        $(send("direct:test")
            .message()
            .body("<trq:TestRequest xmlns:trq=\"http://www.consol.de/schemas/test\">" +
                            "<Message>Hello</Message>" +
                        "</trq:TestRequest>"));

        $(receive("direct:test")
            .message()
            .body("<trq:TestRequest xmlns:trq=\"http://www.consol.de/schemas/test\">" +
                    "<Message>Hello</Message>" +
                "</trq:TestRequest>")
            .validate(xml()
                    .schemaValidation(false)
                    .namespace("trq", "http://www.consol.de/schemas/test"))
            .timeout(5000));

        $(echo("Test: Success with multiple namespace validations"));

        $(send("direct:test")
            .message()
            .body("<trq:TestRequest xmlns:trq=\"http://www.consol.de/schemas/test\" xmlns:msg=\"http://www.consol.de/schemas/message\">" +
                            "<msg:Message>Hello</msg:Message>" +
                        "</trq:TestRequest>"));

        $(receive("direct:test")
            .message()
            .body("<trq:TestRequest xmlns:trq=\"http://www.consol.de/schemas/test\" xmlns:msg=\"http://www.consol.de/schemas/message\">" +
                    "<msg:Message>Hello</msg:Message>" +
                "</trq:TestRequest>")
            .validate(xml()
                    .schemaValidation(false)
                    .namespace("trq", "http://www.consol.de/schemas/test")
                    .namespace("msg", "http://www.consol.de/schemas/message"))
            .timeout(5000));

        $(echo("Test: Success with multiple nested namespace validations"));

        $(send("direct:test")
            .message()
            .body("<trq:TestRequest xmlns:trq=\"http://www.consol.de/schemas/test\">" +
                            "<msg:Message xmlns:msg=\"http://www.consol.de/schemas/message\">Hello</msg:Message>" +
                        "</trq:TestRequest>"));

        $(receive("direct:test")
            .message()
            .body("<trq:TestRequest xmlns:trq=\"http://www.consol.de/schemas/test\">" +
                    "<msg:Message xmlns:msg=\"http://www.consol.de/schemas/message\">Hello</msg:Message>" +
                "</trq:TestRequest>")
            .validate(xml()
                        .schemaValidation(false)
                        .namespace("trq", "http://www.consol.de/schemas/test")
                        .namespace("msg", "http://www.consol.de/schemas/message"))
            .timeout(5000));

        $(echo("Test: Failure because of missing namespace"));

        $(send("direct:test")
            .message()
            .body("<trq:TestRequest xmlns:trq=\"http://www.consol.de/schemas/test\">" +
                            "<Message>Hello</Message>" +
                        "</trq:TestRequest>"));

        $(assertException()
            .exception(ValidationException.class)
            .when(receive("direct:test")
                .message()
                .body("<trq:TestRequest xmlns:trq=\"http://www.consol.de/schemas/test\">" +
                        "<Message>Hello</Message>" +
                    "</trq:TestRequest>")
                .validate(xml()
                            .schemaValidation(false)
                            .namespace("trq", "http://www.consol.de/schemas/test")
                            .namespace("missing", "http://www.consol.de/schemas/missing"))
                .timeout(5000)
        ));

        echo("Test: Failure because of wrong namespace prefix");

        send("direct:test")
            .message()
            .body("<wrong:TestRequest xmlns:wrong=\"http://www.consol.de/schemas/test\">" +
                            "<Message>Hello</Message>" +
                        "</wrong:TestRequest>");

        assertException()
            .exception(ValidationException.class)
            .when(receive("direct:test")
                .message()
                .body("<trq:TestRequest xmlns:trq=\"http://www.consol.de/schemas/test\">" +
                        "<Message>Hello</Message>" +
                    "</trq:TestRequest>")
                .validate(xml()
                        .schemaValidation(false)
                        .namespace("trq", "http://www.consol.de/schemas/test"))
                .timeout(5000)
        );

        echo("Test: Failure because of wrong namespace uri");

        send("direct:test")
            .message()
            .body("<trq:TestRequest xmlns:trq=\"http://www.consol.de/schemas/wrong\">" +
                            "<Message>Hello</Message>" +
                        "</trq:TestRequest>");

        assertException()
            .exception(ValidationException.class)
            .when(receive("direct:test")
                .message()
                .body("<trq:TestRequest xmlns:trq=\"http://www.consol.de/schemas/test\">" +
                        "<Message>Hello</Message>" +
                    "</trq:TestRequest>")
                .validate(xml()
                        .schemaValidation(false)
                        .namespace("trq", "http://www.consol.de/schemas/test"))
                .timeout(5000)
        );
    }
}
