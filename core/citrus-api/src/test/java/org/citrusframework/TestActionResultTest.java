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

package org.citrusframework;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestActionResultTest {

    @Test
    public void toJsonMinimalResult() {
        var fixture = new TestActionResult("echo", "actions.0.echo");

        Assertions.assertEquals("{ \"name\": \"echo\", \"path\": \"actions.0.echo\" }",
                fixture.toJson());
    }

    @Test
    public void toJsonWithError() {
        var fixture = new TestActionResult("send", "actions.1.send");
        fixture.setError("Connection refused");

        Assertions.assertEquals("{ \"name\": \"send\", \"path\": \"actions.1.send\", \"error\": \"Connection refused\" }",
                fixture.toJson());
    }

    @Test
    public void toJsonShouldEscapeQuotesInError() {
        var fixture = new TestActionResult("send", "actions.0.send");
        fixture.setError("Expected \"hello\" but was \"world\"");

        Assertions.assertEquals("{ \"name\": \"send\", \"path\": \"actions.0.send\", \"error\": \"Expected \\\"hello\\\" but was \\\"world\\\"\" }",
                fixture.toJson());
    }

    @Test
    public void toJsonWithMessage() {
        var message = mock(Message.class);
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json");
        when(message.getHeaders()).thenReturn(headers);
        when(message.getHeaderData()).thenReturn(List.of("<SOAP-ENV:Header/>"));
        when(message.getPayload(byte[].class)).thenReturn("Hello".getBytes(StandardCharsets.UTF_8));

        var fixture = new TestActionResult("send", "actions.1.send");
        fixture.setMessage(message);

        String headerDataBase64 = Base64.getEncoder().encodeToString("<SOAP-ENV:Header/>".getBytes());
        String payloadBase64 = Base64.getEncoder().encodeToString("Hello".getBytes(StandardCharsets.UTF_8));

        Assertions.assertEquals("{ \"name\": \"send\", \"path\": \"actions.1.send\", " +
                "\"message\": { " +
                "\"headers\": [ { \"name\": \"Content-Type\", \"value\": \"application/json\" } ], " +
                "\"headerData\": [ \"%s\" ], ".formatted(headerDataBase64) +
                "\"payload\": \"%s\"".formatted(payloadBase64) +
                " } }",
                fixture.toJson());
    }

    @Test
    public void toJsonWithEmptyPayload() {
        var message = mock(Message.class);
        when(message.getHeaders()).thenReturn(Collections.emptyMap());
        when(message.getHeaderData()).thenReturn(Collections.emptyList());
        when(message.getPayload(byte[].class)).thenReturn(null);

        var fixture = new TestActionResult("receive", "actions.2.receive");
        fixture.setMessage(message);

        Assertions.assertEquals("{ \"name\": \"receive\", \"path\": \"actions.2.receive\", " +
                "\"message\": { \"headers\": [  ], \"headerData\": [], \"payload\": \"\" } }",
                fixture.toJson());
    }

    @Test
    public void toJsonWithNullPayload() {
        var message = mock(Message.class);
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("operation", "greet");
        when(message.getHeaders()).thenReturn(headers);
        when(message.getHeaderData()).thenReturn(List.of("<Header/>"));
        when(message.getPayload(byte[].class)).thenReturn(null);

        var fixture = new TestActionResult("send", "actions.0.send");
        fixture.setMessage(message);

        String headerDataBase64 = Base64.getEncoder().encodeToString("<Header/>".getBytes());

        Assertions.assertEquals("{ \"name\": \"send\", \"path\": \"actions.0.send\", " +
                "\"message\": { " +
                "\"headers\": [ { \"name\": \"operation\", \"value\": \"greet\" } ], " +
                "\"headerData\": [ \"%s\" ], ".formatted(headerDataBase64) +
                "\"payload\": \"\"" +
                " } }",
                fixture.toJson());
    }

    @Test
    public void toJsonWithChildActions() {
        var fixture = new TestActionResult("sequence", "actions.0.sequence");
        fixture.addAction(new TestActionResult("echo", "actions.0.sequence.actions.0.echo"));
        fixture.addAction(new TestActionResult("send", "actions.0.sequence.actions.1.send"));

        Assertions.assertEquals("{ \"name\": \"sequence\", \"path\": \"actions.0.sequence\", " +
                "\"actions\": [ " +
                "{ \"name\": \"echo\", \"path\": \"actions.0.sequence.actions.0.echo\" }," +
                "{ \"name\": \"send\", \"path\": \"actions.0.sequence.actions.1.send\" }" +
                " ] }",
                fixture.toJson());
    }

    @Test
    public void toJsonWithIterations() {
        var fixture = new TestActionResult("iterate", "actions.0.iterate");
        fixture.addIteration(new TestActionResult("0", "actions.0.iterate"));
        fixture.addIteration(new TestActionResult("1", "actions.0.iterate"));

        Assertions.assertEquals("{ \"name\": \"iterate\", \"path\": \"actions.0.iterate\", " +
                "\"iterations\": [ " +
                "{ \"name\": \"0\", \"path\": \"actions.0.iterate\" }," +
                "{ \"name\": \"1\", \"path\": \"actions.0.iterate\" }" +
                " ] }",
                fixture.toJson());
    }

    @Test
    public void toJsonWithAllFields() {
        var message = mock(Message.class);
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("operation", "test");
        when(message.getHeaders()).thenReturn(headers);
        when(message.getHeaderData()).thenReturn(Collections.emptyList());
        when(message.getPayload(byte[].class)).thenReturn("payload".getBytes(StandardCharsets.UTF_8));

        var fixture = new TestActionResult("sequence", "actions.0.sequence");
        fixture.setError("validation failed");
        fixture.setMessage(message);
        fixture.addAction(new TestActionResult("send", "actions.0.sequence.actions.0.send"));
        fixture.addIteration(new TestActionResult("0", "actions.0.sequence"));

        String json = fixture.toJson();

        String payloadBase64 = Base64.getEncoder().encodeToString("payload".getBytes(StandardCharsets.UTF_8));

        Assertions.assertTrue(json.contains("\"name\": \"sequence\""));
        Assertions.assertTrue(json.contains("\"path\": \"actions.0.sequence\""));
        Assertions.assertTrue(json.contains("\"error\": \"validation failed\""));
        Assertions.assertTrue(json.contains("\"message\": {"));
        Assertions.assertTrue(json.contains("\"payload\": \"%s\"".formatted(payloadBase64)));
        Assertions.assertTrue(json.contains("\"actions\": ["));
        Assertions.assertTrue(json.contains("\"iterations\": ["));
    }

    @Test
    public void toJsonWithMultipleHeaders() {
        var message = mock(Message.class);
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("key1", "value1");
        headers.put("key2", "value2");
        when(message.getHeaders()).thenReturn(headers);
        when(message.getHeaderData()).thenReturn(Collections.emptyList());
        when(message.getPayload(byte[].class)).thenReturn(null);

        var fixture = new TestActionResult("send", "actions.1.send");
        fixture.setMessage(message);

        String json = fixture.toJson();

        Assertions.assertTrue(json.contains("{ \"name\": \"key1\", \"value\": \"value1\" }"));
        Assertions.assertTrue(json.contains("{ \"name\": \"key2\", \"value\": \"value2\" }"));
    }

    @Test
    public void toJsonShouldFallbackWhenPayloadNotConvertibleToBytes() {
        var message = mock(Message.class);
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        when(message.getHeaders()).thenReturn(headers);
        when(message.getHeaderData()).thenReturn(Collections.emptyList());
        when(message.getPayload(byte[].class)).thenThrow(
                new CitrusRuntimeException("Unable to convert object 'org.springframework.util.LinkedMultiValueMap' to target type 'class [B'"));
        when(message.getPayload(String.class)).thenReturn("{foo=[bar]}");

        var fixture = new TestActionResult("send", "actions.0.send");
        fixture.setMessage(message);

        String json = fixture.toJson();
        String payloadBase64 = Base64.getEncoder().encodeToString("{foo=[bar]}".getBytes(StandardCharsets.UTF_8));

        Assertions.assertTrue(json.contains("\"payload\": \"%s\"".formatted(payloadBase64)));
    }

    @Test
    public void toYamlMinimalResult() {
        var fixture = new TestActionResult("echo", "actions.0.echo");

        Assertions.assertEquals("name: \"echo\"\n" +
                "path: \"actions.0.echo\"\n",
                fixture.toYaml());
    }

    @Test
    public void toYamlWithError() {
        var fixture = new TestActionResult("send", "actions.1.send");
        fixture.setError("Connection refused");

        Assertions.assertEquals("name: \"send\"\n" +
                "path: \"actions.1.send\"\n" +
                "error: |\n" +
                "  Connection refused\n",
                fixture.toYaml());
    }

    @Test
    public void toYamlWithMessage() {
        var message = mock(Message.class);
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json");
        when(message.getHeaders()).thenReturn(headers);
        when(message.getHeaderData()).thenReturn(List.of("<SOAP-ENV:Header/>"));
        when(message.getPayload(String.class)).thenReturn("Hello");

        var fixture = new TestActionResult("send", "actions.1.send");
        fixture.setMessage(message);

        String headerDataBase64 = Base64.getEncoder().encodeToString("<SOAP-ENV:Header/>".getBytes());

        String yaml = fixture.toYaml();

        Assertions.assertTrue(yaml.contains("name: \"send\""));
        Assertions.assertTrue(yaml.contains("path: \"actions.1.send\""));
        Assertions.assertTrue(yaml.contains("message:\n"));
        Assertions.assertTrue(yaml.contains("headers:\n"));
        Assertions.assertTrue(yaml.contains("- name: \"Content-Type\""));
        Assertions.assertTrue(yaml.contains("value: \"application/json\""));
        Assertions.assertTrue(yaml.contains("headerData: [ '%s' ]".formatted(headerDataBase64)));
        Assertions.assertTrue(yaml.contains("payload: |\n"));
        Assertions.assertTrue(yaml.contains("Hello"));
    }

    @Test
    public void toYamlWithEmptyPayload() {
        var message = mock(Message.class);
        when(message.getHeaders()).thenReturn(Collections.emptyMap());
        when(message.getHeaderData()).thenReturn(Collections.emptyList());
        when(message.getPayload(String.class)).thenReturn(null);

        var fixture = new TestActionResult("receive", "actions.2.receive");
        fixture.setMessage(message);

        String yaml = fixture.toYaml();

        Assertions.assertTrue(yaml.contains("name: \"receive\""));
        Assertions.assertTrue(yaml.contains("message:\n"));
        Assertions.assertTrue(yaml.contains("headerData: []"));
        Assertions.assertTrue(yaml.contains("payload: |\n"));
    }

    @Test
    public void toYamlWithNullPayload() {
        var message = mock(Message.class);
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("operation", "greet");
        when(message.getHeaders()).thenReturn(headers);
        when(message.getHeaderData()).thenReturn(List.of("<Header/>"));
        when(message.getPayload(String.class)).thenReturn(null);

        var fixture = new TestActionResult("send", "actions.0.send");
        fixture.setMessage(message);

        String headerDataBase64 = Base64.getEncoder().encodeToString("<Header/>".getBytes());

        String yaml = fixture.toYaml();

        Assertions.assertTrue(yaml.contains("- name: \"operation\""));
        Assertions.assertTrue(yaml.contains("value: \"greet\""));
        Assertions.assertTrue(yaml.contains("headerData: [ '%s' ]".formatted(headerDataBase64)));
        Assertions.assertTrue(yaml.contains("payload: |\n"));
    }

    @Test
    public void toYamlWithChildActions() {
        var fixture = new TestActionResult("sequence", "actions.0.sequence");
        fixture.addAction(new TestActionResult("echo", "actions.0.sequence.actions.0.echo"));
        fixture.addAction(new TestActionResult("send", "actions.0.sequence.actions.1.send"));

        String yaml = fixture.toYaml();

        Assertions.assertTrue(yaml.contains("name: \"sequence\""));
        Assertions.assertTrue(yaml.contains("actions:\n"));
        Assertions.assertTrue(yaml.contains("name: \"echo\""));
        Assertions.assertTrue(yaml.contains("path: \"actions.0.sequence.actions.0.echo\""));
        Assertions.assertTrue(yaml.contains("name: \"send\""));
        Assertions.assertTrue(yaml.contains("path: \"actions.0.sequence.actions.1.send\""));
    }

    @Test
    public void toYamlWithIterations() {
        var fixture = new TestActionResult("iterate", "actions.0.iterate");
        fixture.addIteration(new TestActionResult("0", "actions.0.iterate"));
        fixture.addIteration(new TestActionResult("1", "actions.0.iterate"));

        String yaml = fixture.toYaml();

        Assertions.assertTrue(yaml.contains("name: \"iterate\""));
        Assertions.assertTrue(yaml.contains("iterations:\n"));
        Assertions.assertTrue(yaml.contains("name: \"0\""));
        Assertions.assertTrue(yaml.contains("name: \"1\""));
        Assertions.assertTrue(yaml.contains("path: \"actions.0.iterate\""));
    }

    @Test
    public void toYamlWithMultipleHeaders() {
        var message = mock(Message.class);
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("key1", "value1");
        headers.put("key2", "value2");
        when(message.getHeaders()).thenReturn(headers);
        when(message.getHeaderData()).thenReturn(Collections.emptyList());
        when(message.getPayload(String.class)).thenReturn(null);

        var fixture = new TestActionResult("send", "actions.1.send");
        fixture.setMessage(message);

        String yaml = fixture.toYaml();

        Assertions.assertTrue(yaml.contains("- name: \"key1\""));
        Assertions.assertTrue(yaml.contains("value: \"value1\""));
        Assertions.assertTrue(yaml.contains("- name: \"key2\""));
        Assertions.assertTrue(yaml.contains("value: \"value2\""));
    }

    @Test
    public void toYamlWithAllFields() {
        var message = mock(Message.class);
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("operation", "test");
        when(message.getHeaders()).thenReturn(headers);
        when(message.getHeaderData()).thenReturn(Collections.emptyList());
        when(message.getPayload(String.class)).thenReturn("payload");

        var fixture = new TestActionResult("sequence", "actions.0.sequence");
        fixture.setError("validation failed");
        fixture.setMessage(message);
        fixture.addAction(new TestActionResult("send", "actions.0.sequence.actions.0.send"));
        fixture.addIteration(new TestActionResult("0", "actions.0.sequence"));

        String yaml = fixture.toYaml();

        Assertions.assertTrue(yaml.contains("name: \"sequence\""));
        Assertions.assertTrue(yaml.contains("path: \"actions.0.sequence\""));
        Assertions.assertTrue(yaml.contains("error: |"));
        Assertions.assertTrue(yaml.contains("validation failed"));
        Assertions.assertTrue(yaml.contains("message:\n"));
        Assertions.assertTrue(yaml.contains("payload: |"));
        Assertions.assertTrue(yaml.contains("actions:\n"));
        Assertions.assertTrue(yaml.contains("iterations:\n"));
    }
}
