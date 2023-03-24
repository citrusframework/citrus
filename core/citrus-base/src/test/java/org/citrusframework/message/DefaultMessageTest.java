/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.message;

import org.citrusframework.UnitTestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class DefaultMessageTest extends UnitTestSupport {

    @Test
    public void testPrint() {
        DefaultMessage message = new DefaultMessage("<credentials><password>foo</password></credentials>");

        message.setHeader("operation", "getCredentials");
        message.setHeader("password", "foo");

        String output = message.print();
        Assert.assertEquals(output, String.format("DEFAULTMESSAGE [" +
                    "id: %s, " +
                    "payload: <credentials>%n  <password>foo</password>%n</credentials>%n" +
                "][headers: {" +
                    "citrus_message_id=%s, citrus_message_timestamp=%s, operation=getCredentials, password=foo" +
                "}]", message.getId(), message.getId(), message.getTimestamp()));
    }

    @Test
    public void testPrintMaskKeyValue() {
        DefaultMessage message = new DefaultMessage("password=foo,secret=bar");

        message.setHeader("operation", "getCredentials");
        message.setHeader("password", "foo");
        message.setHeader("secret", "bar");

        String output = message.print(context);
        Assert.assertEquals(output, String.format("DEFAULTMESSAGE [" +
                    "id: %s, " +
                    "payload: password=****,secret=****" +
                "][headers: {" +
                    "citrus_message_id=%s, citrus_message_timestamp=%s, operation=getCredentials, password=****, secret=****" +
                "}]", message.getId(), message.getId(), message.getTimestamp()));
    }

    @Test
    public void testPrintMaskFormUrlEncoded() {
        DefaultMessage message = new DefaultMessage("password=foo&secret=bar");

        message.setHeader("operation", "getCredentials");
        message.setHeader("password", "foo");
        message.setHeader("secret", "bar");

        String output = message.print(context);
        Assert.assertEquals(output, String.format("DEFAULTMESSAGE [" +
                    "id: %s, " +
                    "payload: password=****&secret=****" +
                "][headers: {" +
                    "citrus_message_id=%s, citrus_message_timestamp=%s, operation=getCredentials, password=****, secret=****" +
                "}]", message.getId(), message.getId(), message.getTimestamp()));
    }

    @Test
    public void testPrintMaskXml() {
        DefaultMessage message = new DefaultMessage("<credentials><password>foo</password></credentials>");

        message.setHeader("operation", "getCredentials");
        message.setHeader("password", "foo");

        String output = message.print(context);
        Assert.assertEquals(output, String.format("DEFAULTMESSAGE [" +
                    "id: %s, " +
                    "payload: <credentials>%n  <password>****</password>%n</credentials>%n" +
                "][headers: {" +
                    "citrus_message_id=%s, citrus_message_timestamp=%s, operation=getCredentials, password=****" +
                "}]", message.getId(), message.getId(), message.getTimestamp()));
    }

    @Test
    public void testPrintMaskJson() {
        DefaultMessage message = new DefaultMessage("{ \"credentials\": { \"password\": \"foo\", \"secretKey\": \"bar\" }}");

        message.setHeader("operation", "getCredentials");
        message.setHeader("password", "foo");
        message.setHeader("secretKey", "bar");

        String output = message.print(context);
        Assert.assertEquals(output, String.format("DEFAULTMESSAGE [" +
                    "id: %s, " +
                    "payload: {%n  \"credentials\": {%n    \"password\": \"****\",%n    \"secretKey\": \"****\"%n  }%n}" +
                "][headers: {" +
                    "citrus_message_id=%s, citrus_message_timestamp=%s, operation=getCredentials, password=****, secretKey=****" +
                "}]", message.getId(), message.getId(), message.getTimestamp()));
    }
}
