/*
 * Copyright 2006-2017 the original author or authors.
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

import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.xml.StringResult;
import org.citrusframework.xml.StringSource;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class MailMarshallerTest {

    private final MailMarshaller mailMarshaller = new MailMarshaller();

    @Test(dataProvider = "mailSourceProvider")
    public void testUnmarshalMail(String requestSource, String responseSource) throws Exception {
        MailRequest request = (MailRequest) mailMarshaller.unmarshal(new StringSource(FileUtils.readToString(Resources.fromClasspath(requestSource))));
        MailResponse response = (MailResponse) mailMarshaller.unmarshal(new StringSource(FileUtils.readToString(Resources.fromClasspath(responseSource))));

        Assert.assertEquals(request.getFrom(), "foo@mail.com");
        Assert.assertEquals(request.getTo(), "bar@mail.com,copy@mail.com");

        Assert.assertEquals(response.getCode(), 250);
        Assert.assertEquals(response.getMessage(), "OK");
    }

    @Test(dataProvider = "acceptSourceProvider")
    public void testUnmarshalAccept(String requestSource, String responseSource) throws Exception {
        AcceptRequest request = (AcceptRequest) mailMarshaller.unmarshal(new StringSource(FileUtils.readToString(Resources.fromClasspath(requestSource))));
        AcceptResponse response = (AcceptResponse) mailMarshaller.unmarshal(new StringSource(FileUtils.readToString(Resources.fromClasspath(responseSource))));

        Assert.assertEquals(request.getFrom(), "foo@mail.com");

        Assert.assertTrue(response.isAccept());
    }

    @Test
    public void testMarshal() throws Exception {
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
        Assert.assertEquals(result.getFrom(), "foo@mail.com");
        Assert.assertEquals(result.getTo(), "bar@mail.com,copy@mail.com");
    }

    @DataProvider
    public Object[][] mailSourceProvider() {
        return new Object[][] {
            new Object[] { "org/citrusframework/mail/server/text_mail.xml", "org/citrusframework/mail/server/mail_response.xml" },
            new Object[] { "org/citrusframework/mail/server/text_mail.json", "org/citrusframework/mail/server/mail_response.json" }
        };
    }

    @DataProvider
    public Object[][] acceptSourceProvider() {
        return new Object[][] {
            new Object[] { "org/citrusframework/mail/server/accept-request.xml", "org/citrusframework/mail/server/accept-response.xml" },
            new Object[] { "org/citrusframework/mail/server/accept-request.json", "org/citrusframework/mail/server/accept-response.json" }
        };
    }

}
