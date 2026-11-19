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

package org.citrusframework.mail.xml;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.mail.client.MailClient;
import org.citrusframework.mail.server.MailServer;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.xml.XmlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MailEndpointsTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadMailEndpointsWithMarshallerType() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/mail/xml/mail-endpoints.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("MailEndpointsTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(1L, result.getActionCount());
        Assertions.assertTrue(result.getTestResult().isSuccess());

        MailServer mailServer = context.getReferenceResolver().resolve("myMailServer", MailServer.class);
        Assertions.assertEquals("JSON", mailServer.getMarshaller().getType());

        MailClient mailClient = context.getReferenceResolver().resolve("myMailClient", MailClient.class);
        Assertions.assertEquals("json", mailClient.getEndpointConfiguration().getMarshaller().getType());
    }

    @Test
    public void shouldValidateMailEndpointsAgainstTestCaseSchema() throws Exception {
        Resource schemaResource = Resources.create(
                "classpath:org/citrusframework/schema/xml/testcase/citrus-testcase.xsd");
        Resource testResource = Resources.create("classpath:org/citrusframework/mail/xml/mail-endpoints.citrus.it.xml");

        javax.xml.validation.Schema schema = javax.xml.validation.SchemaFactory
                .newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI)
                .newSchema(new org.citrusframework.api.xml.StringSource(FileUtils.readToString(schemaResource)));

        schema.newValidator()
                .validate(new javax.xml.transform.stream.StreamSource(
                        new java.io.StringReader(FileUtils.readToString(testResource))));
    }
}
