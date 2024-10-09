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

package org.citrusframework.message;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Map;
import org.citrusframework.UnitTestSupport;
import org.testng.annotations.Test;

public class DefaultMessageTest extends UnitTestSupport {

    @Test
    public void testPrint() {
        DefaultMessage message = new DefaultMessage("<credentials><password>foo</password></credentials>");

        message.setHeader("operation", "getCredentials");
        message.setHeader("password", "foo");

        String output = message.print();
        assertEquals(output, String.format("DEFAULTMESSAGE [" +
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
        assertEquals(output, String.format("DEFAULTMESSAGE [" +
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
        assertEquals(output, String.format("DEFAULTMESSAGE [" +
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
        assertEquals(output, String.format("DEFAULTMESSAGE [" +
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
        assertEquals(output, String.format("DEFAULTMESSAGE [" +
                    "id: %s, " +
                    "payload: {%n  \"credentials\": {%n    \"password\": \"****\",%n    \"secretKey\": \"****\"%n  }%n}" +
                "][headers: {" +
                    "citrus_message_id=%s, citrus_message_timestamp=%s, operation=getCredentials, password=****, secretKey=****" +
                "}]", message.getId(), message.getId(), message.getTimestamp()));
    }

    @Test
    public void testCopyConstructorPreservesIdAndTimestamp() {

        // Given
        DefaultMessage originalMessage = new DefaultMessage("myPayload", Map.of("k1","v1"));

        // When
        DefaultMessage copiedMessage = new DefaultMessage(originalMessage);

        // Then
        assertEquals(originalMessage.getHeader(MessageHeaders.ID), copiedMessage.getHeader(MessageHeaders.ID));
        assertEquals(originalMessage.getHeader(MessageHeaders.TIMESTAMP), copiedMessage.getHeader(MessageHeaders.TIMESTAMP));
        assertEquals(originalMessage.getHeader("k1"), copiedMessage.getHeader("k1"));
        assertEquals(originalMessage.getPayload(), copiedMessage.getPayload());

    }

    @Test
    public void testCopyConstructorWithCitrusOverwriteDoesNotPreserveIdAndTimestamp() {

        // Given
        DefaultMessage originalMessage = new DefaultMessage("myPayload", Map.of("k1","v1"));
        originalMessage.setHeader(MessageHeaders.TIMESTAMP, System.currentTimeMillis() - 1);

        // When
        DefaultMessage copiedMessage = new DefaultMessage(originalMessage, true);

        // Then
        assertNotEquals(originalMessage.getHeader(MessageHeaders.ID), copiedMessage.getHeader(MessageHeaders.ID));
        assertNotEquals(originalMessage.getHeader(MessageHeaders.TIMESTAMP), copiedMessage.getHeader(MessageHeaders.TIMESTAMP));
        assertEquals(originalMessage.getHeader("k1"), copiedMessage.getHeader("k1"));
        assertEquals(originalMessage.getPayload(), copiedMessage.getPayload());

    }
}
