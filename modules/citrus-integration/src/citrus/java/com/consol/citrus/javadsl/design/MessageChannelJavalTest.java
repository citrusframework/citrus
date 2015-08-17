/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.javadsl.design;

import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.message.MessageType;
import org.testng.annotations.Test;

import java.util.*;

/**
 * @author Christoph Deppisch
 */
@Test
public class MessageChannelJavalTest extends TestNGCitrusTestDesigner {
    
    @CitrusTest
    public void messageChannels() {
        send("channelRequestSender")
            .payload("Hello Citrus")
            .header("Operation", "sayHello");
        
        send("channelRequestSender")
            .payload("Goodbye Citrus")
            .header("Operation", "sayGoodBye");
        
        receive("channelResponseReceiver")
            .selector(Collections.singletonMap("Operation", "sayGoodBye"))
            .messageType(MessageType.PLAINTEXT)
            .payload("Goodbye Citrus")
            .header("Operation", "sayGoodBye");
        
        receive("channelResponseReceiver")
            .selector(Collections.singletonMap("Operation", "sayHello"))
            .messageType(MessageType.PLAINTEXT)
            .payload("Hello Citrus")
            .header("Operation", "sayHello");
        
        echo("Test root qname message selector");
        
        send("channelRequestSender")
            .payload("<HelloMessage xmlns=\"http://citrusframework.org/schema\">Hello Citrus</HelloMessage>")
            .header("Operation", "sayHello");
        
        send("channelRequestSender")
            .payload("<GoodbyeMessage xmlns=\"http://citrusframework.org/schema\">Goodbye Citrus</GoodbyeMessage>")
            .header("Operation", "sayGoodBye");
        
        receive("channelResponseReceiver")
            .selector(Collections.singletonMap("root-qname", "GoodbyeMessage"))
            .schemaValidation(false)
            .payload("<GoodbyeMessage xmlns=\"http://citrusframework.org/schema\">Goodbye Citrus</GoodbyeMessage>")
            .header("Operation", "sayGoodBye");
        
        receive("channelResponseReceiver")
            .selector(Collections.singletonMap("root-qname", "HelloMessage"))
            .schemaValidation(false)
            .payload("<HelloMessage xmlns=\"http://citrusframework.org/schema\">Hello Citrus</HelloMessage>")
            .header("Operation", "sayHello");
        
        echo("Test root qname message selector with namespaces");
        
        send("channelRequestSender")
            .payload("<HelloMessage xmlns=\"http://citrusframework.org/helloschema\">Hello Citrus</HelloMessage>")
            .header("Operation", "sayHello");
        
        send("channelRequestSender")
            .payload("<GoodbyeMessage xmlns=\"http://citrusframework.org/goodbyeschema\">Goodbye Citrus</GoodbyeMessage>")
            .header("Operation", "sayGoodBye");
        
        receive("channelResponseReceiver")
            .selector(Collections.singletonMap("root-qname", "{http://citrusframework.org/goodbyeschema}GoodbyeMessage"))
            .schemaValidation(false)
            .payload("<GoodbyeMessage xmlns=\"http://citrusframework.org/goodbyeschema\">Goodbye Citrus</GoodbyeMessage>")
            .header("Operation", "sayGoodBye");
        
        receive("channelResponseReceiver")
            .selector(Collections.singletonMap("root-qname", "{http://citrusframework.org/helloschema}HelloMessage"))
            .schemaValidation(false)
            .payload("<HelloMessage xmlns=\"http://citrusframework.org/helloschema\">Hello Citrus</HelloMessage>")
            .header("Operation", "sayHello");
        
        echo("Test xpath message selector");
        
        send("channelRequestSender")
            .payload("<ns:HelloMessage xmlns:ns=\"http://citrusframework.org/helloschema\">" +
                            "<ns:text language=\"eng\">" +
                                "<ns:value>Hello Citrus</ns:value>" +
                            "</ns:text>" +
                            "<ns:text language=\"de\">" +
                                "<ns:value>Hallo Citrus</ns:value>" +
                            "</ns:text>" +
                            "<ns:text language=\"esp\">" +
                                "<ns:value>Hola Citrus</ns:value>" +
                            "</ns:text>" +
                        "</ns:HelloMessage>")
            .header("Operation", "sayHello");
        
        send("channelRequestSender")
            .payload("<ns:GoodbyeMessage xmlns:ns=\"http://citrusframework.org/goodbyeschema\">" +
                            "<ns:text language=\"eng\">" +
                                "<ns:value>Goodbye Citrus</ns:value>" +
                            "</ns:text>" +
                            "<ns:text language=\"de\">" +
                                "<ns:value>Auf Wiedersehen Citrus</ns:value>" +
                            "</ns:text>" +
                            "<ns:text language=\"esp\">" +
                                "<ns:value>Adios Citrus</ns:value>" +
                            "</ns:text>" +
                        "</ns:GoodbyeMessage>")
            .header("Operation", "sayGoodBye");
        
        receive("channelResponseReceiver")
            .selector(Collections.singletonMap("xpath://ns:GoodbyeMessage/ns:text[@language='eng']/ns:value", 
                    "Goodbye Citrus"))
            .schemaValidation(false)
            .payload("<ns:GoodbyeMessage xmlns:ns=\"http://citrusframework.org/goodbyeschema\">" +
                            "<ns:text language=\"eng\">" +
                            "<ns:value>Goodbye Citrus</ns:value>" +
                        "</ns:text>" +
                        "<ns:text language=\"de\">" +
                            "<ns:value>Auf Wiedersehen Citrus</ns:value>" +
                        "</ns:text>" +
                        "<ns:text language=\"esp\">" +
                            "<ns:value>Adios Citrus</ns:value>" +
                        "</ns:text>" +
                    "</ns:GoodbyeMessage>")
            .header("Operation", "sayGoodBye");
        
        Map<String, String> selectorMap = new HashMap<String, String>();
        selectorMap.put("xpath://ns:HelloMessage/ns:text[@language='eng']/ns:value", "Hello Citrus");
        selectorMap.put("xpath://ns:HelloMessage/ns:text[@language='de']/ns:value", "Hallo Citrus");
        selectorMap.put("xpath://ns:HelloMessage/ns:text[@language='esp']/ns:value", "Hola Citrus");
        
        receive("channelResponseReceiver")
            .selector(selectorMap)
            .schemaValidation(false)
            .payload("<ns:HelloMessage xmlns:ns=\"http://citrusframework.org/helloschema\">" +
                        "<ns:text language=\"eng\">" +
                        "<ns:value>Hello Citrus</ns:value>" +
                    "</ns:text>" +
                    "<ns:text language=\"de\">" +
                        "<ns:value>Hallo Citrus</ns:value>" +
                    "</ns:text>" +
                    "<ns:text language=\"esp\">" +
                        "<ns:value>Hola Citrus</ns:value>" +
                    "</ns:text>" +
                "</ns:HelloMessage>")
            .header("Operation", "sayHello");
        
        send("channelRequestSender")
            .payload("<ns:HelloMessage xmlns:ns=\"http://citrusframework.org/helloschema\">" +
                            "<ns:text language=\"eng\">" +
                                "<ns:value>Hello Citrus</ns:value>" +
                            "</ns:text>" +
                            "<ns:text language=\"de\">" +
                                "<ns:value>Hallo Citrus</ns:value>" +
                            "</ns:text>" +
                            "<ns:text language=\"esp\">" +
                                "<ns:value>Hola Citrus</ns:value>" +
                            "</ns:text>" +
                        "</ns:HelloMessage>")
            .header("Operation", "sayHello");
        
        send("channelRequestSender")
            .payload("<ns:GoodbyeMessage xmlns:ns=\"http://citrusframework.org/goodbyeschema\">" +
                            "<ns:text language=\"eng\">" +
                                "<ns:value>Goodbye Citrus</ns:value>" +
                            "</ns:text>" +
                            "<ns:text language=\"de\">" +
                                "<ns:value>Auf Wiedersehen Citrus</ns:value>" +
                            "</ns:text>" +
                            "<ns:text language=\"esp\">" +
                                "<ns:value>Adios Citrus</ns:value>" +
                            "</ns:text>" +
                        "</ns:GoodbyeMessage>")
            .header("Operation", "sayGoodBye");
        
        receive("channelResponseReceiver")
            .selector("xpath://ns:GoodbyeMessage/ns:text[@language='eng']/ns:value='Goodbye Citrus'")
            .schemaValidation(false)
            .payload("<ns:GoodbyeMessage xmlns:ns=\"http://citrusframework.org/goodbyeschema\">" +
                            "<ns:text language=\"eng\">" +
                            "<ns:value>Goodbye Citrus</ns:value>" +
                        "</ns:text>" +
                        "<ns:text language=\"de\">" +
                            "<ns:value>Auf Wiedersehen Citrus</ns:value>" +
                        "</ns:text>" +
                        "<ns:text language=\"esp\">" +
                            "<ns:value>Adios Citrus</ns:value>" +
                        "</ns:text>" +
                    "</ns:GoodbyeMessage>")
            .header("Operation", "sayGoodBye");
        
        receive("channelResponseReceiver")
            .selector("xpath://ns:HelloMessage/ns:text[@language='eng']/ns:value='Hello Citrus'")
            .schemaValidation(false)
            .payload("<ns:HelloMessage xmlns:ns=\"http://citrusframework.org/helloschema\">" +
                        "<ns:text language=\"eng\">" +
                        "<ns:value>Hello Citrus</ns:value>" +
                    "</ns:text>" +
                    "<ns:text language=\"de\">" +
                        "<ns:value>Hallo Citrus</ns:value>" +
                    "</ns:text>" +
                    "<ns:text language=\"esp\">" +
                        "<ns:value>Hola Citrus</ns:value>" +
                    "</ns:text>" +
                "</ns:HelloMessage>")
            .header("Operation", "sayHello");
    }
}