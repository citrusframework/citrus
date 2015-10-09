/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus;

import com.consol.citrus.dsl.design.DefaultTestDesigner;
import com.consol.citrus.http.message.HttpMessage;
import com.consol.citrus.jms.message.JmsMessage;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;
import com.consol.citrus.ws.message.SoapMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.2
 */
public class CitrusStandaloneIT extends AbstractTestNGCitrusTest {

    private Citrus citrus;

    @BeforeClass
    public void before() {
        citrus = Citrus.newInstance(applicationContext);
    }

    @Test
    public void echoTest() {
        DefaultTestDesigner test = new DefaultTestDesigner();

        test.name("EchoIT");

        test.echo("Hello Citrus!");

        citrus.run(test.getTestCase());
    }

    @Test
    public void jmsTest() {
        DefaultTestDesigner test = new DefaultTestDesigner();

        test.name("JmsIT");

        test.echo("Send JMS request!");

        test.send("jms:my.queue")
                .messageType(MessageType.PLAINTEXT)
                .message(new JmsMessage("Hello Citrus!"));

        test.echo("Receive JMS request!");

        test.receive("jms:my.queue")
                .messageType(MessageType.PLAINTEXT)
                .message(new JmsMessage("Hello Citrus!"));

        citrus.run(test.getTestCase());
    }

    @Test
    public void httpTest() {
        DefaultTestDesigner test = new DefaultTestDesigner();

        test.name("HttpIT");

        test.echo("Send Http request!");

        test.send("http://localhost:8073/test")
                .message(new HttpMessage("<testRequestMessage><text>Hello HttpServer</text></testRequestMessage>")
                        .method(HttpMethod.POST));

        test.echo("Receive Http request!");

        test.receive("http://localhost:8073/test")
                .message(new HttpMessage("<testResponseMessage><text>Hello TestFramework</text></testResponseMessage>")
                        .status(HttpStatus.OK));

        citrus.run(test.getTestCase());
    }

    @Test
    public void soapTest() {
        DefaultTestDesigner test = new DefaultTestDesigner();

        test.name("SoapIT");

        test.echo("Send SOAP request!");

        test.send("soap://localhost:8071/hello")
                .message(new SoapMessage("<ns0:HelloStandaloneRequest xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                            "<ns0:MessageId>123456789</ns0:MessageId>" +
                            "<ns0:CorrelationId>CORR123456789</ns0:CorrelationId>" +
                            "<ns0:User>User</ns0:User>" +
                            "<ns0:Text>Hello WebServer</ns0:Text>" +
                        "</ns0:HelloStandaloneRequest>")
                        .soapAction("sayHello"));

        test.echo("Receive SOAP request!");

        test.receive("soap://localhost:8071/hello")
                .message(new SoapMessage("<ns0:HelloStandaloneResponse xmlns:ns0=\"http://www.consol.de/schemas/samples/sayHello.xsd\">" +
                            "<ns0:MessageId>123456789</ns0:MessageId>" +
                            "<ns0:CorrelationId>CORR123456789</ns0:CorrelationId>" +
                            "<ns0:User>WebServer</ns0:User>" +
                            "<ns0:Text>Hello User</ns0:Text>" +
                        "</ns0:HelloStandaloneResponse>")).schemaValidation(false);

        citrus.run(test.getTestCase());
    }
}
