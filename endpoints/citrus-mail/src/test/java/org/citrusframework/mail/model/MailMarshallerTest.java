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

package org.citrusframework.mail.model;

import java.util.stream.Stream;

import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.api.xml.StringResult;
import org.citrusframework.api.xml.StringSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class MailMarshallerTest {

    private final MailMarshaller mailMarshaller = new MailMarshaller();

    @ParameterizedTest
    @MethodSource("mailSourceProvider")
    public void testUnmarshalMail(String requestSource, String responseSource) throws Exception {
        MailRequest request = (MailRequest) mailMarshaller.unmarshal(new StringSource(FileUtils.readToString(Resources.fromClasspath(requestSource))));
        MailResponse response = (MailResponse) mailMarshaller.unmarshal(new StringSource(FileUtils.readToString(Resources.fromClasspath(responseSource))));

        Assertions.assertEquals("foo@mail.com", request.getFrom());
        Assertions.assertEquals("bar@mail.com,copy@mail.com", request.getTo());

        Assertions.assertEquals(250, response.getCode());
        Assertions.assertEquals("OK", response.getMessage());
    }

    @ParameterizedTest
    @MethodSource("acceptSourceProvider")
    public void testUnmarshalAccept(String requestSource, String responseSource) throws Exception {
        AcceptRequest request = (AcceptRequest) mailMarshaller.unmarshal(new StringSource(FileUtils.readToString(Resources.fromClasspath(requestSource))));
        AcceptResponse response = (AcceptResponse) mailMarshaller.unmarshal(new StringSource(FileUtils.readToString(Resources.fromClasspath(responseSource))));

        Assertions.assertEquals("foo@mail.com", request.getFrom());

        Assertions.assertTrue(response.isAccept());
    }

    @Test
    public void testMarshal() {
        MailRequest mailRequest = new MailRequest();
        mailRequest.setFrom("foo@mail.com");
        mailRequest.setTo("bar@mail.com,copy@mail.com");
        mailRequest.setSubject("Testmail");

        BodyPart mailBodyPart = new BodyPart();
        mailBodyPart.setContentType("text/plain");
        mailBodyPart.setContent("Lorem ipsum dolor sit amet, consectetur adipisici elit, sed eiusmod tempor incidunt ut labore et dolore magna aliqua.");
        mailRequest.setBody(mailBodyPart);

        StringResult mailRequestResult = new StringResult();
        mailMarshaller.marshal(mailRequest, mailRequestResult);

        MailRequest result = (MailRequest) mailMarshaller.unmarshal(new StringSource(mailRequestResult.toString()));
        Assertions.assertEquals("foo@mail.com", result.getFrom());
        Assertions.assertEquals("bar@mail.com,copy@mail.com", result.getTo());
    }

    static Stream<Arguments> mailSourceProvider() {
        return Stream.of(
            Arguments.of("org/citrusframework/mail/server/text_mail.xml", "org/citrusframework/mail/server/mail_response.xml"),
            Arguments.of("org/citrusframework/mail/server/text_mail.json", "org/citrusframework/mail/server/mail_response.json")
        );
    }

    static Stream<Arguments> acceptSourceProvider() {
        return Stream.of(
            Arguments.of("org/citrusframework/mail/server/accept-request.xml", "org/citrusframework/mail/server/accept-response.xml"),
            Arguments.of("org/citrusframework/mail/server/accept-request.json", "org/citrusframework/mail/server/accept-response.json")
        );
    }
}
