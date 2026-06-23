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

import org.citrusframework.message.Message;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestActionResultTest {

    @Test
    public void toJsonMinimalResult() {
        var fixture = new TestActionResult("echo", "actions.0.echo");

        Assert.assertEquals(fixture.toJson(),
                "{ \"name\": \"echo\", \"path\": \"actions.0.echo\" }");
    }

    @Test
    public void toJsonWithError() {
        var fixture = new TestActionResult("send", "actions.1.send");
        fixture.setError("Connection refused");

        Assert.assertEquals(fixture.toJson(),
                "{ \"name\": \"send\", \"path\": \"actions.1.send\", \"error\": \"Connection refused\" }");
    }

    @Test
    public void toJsonShouldEscapeQuotesInError() {
        var fixture = new TestActionResult("send", "actions.0.send");
        fixture.setError("Expected \"hello\" but was \"world\"");

        Assert.assertEquals(fixture.toJson(),
                "{ \"name\": \"send\", \"path\": \"actions.0.send\", \"error\": \"Expected \\\"hello\\\" but was \\\"world\\\"\" }");
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

        Assert.assertEquals(fixture.toJson(),
                "{ \"name\": \"send\", \"path\": \"actions.1.send\", " +
                "\"message\": { " +
                "\"headers\": [ { \"name\": \"Content-Type\", \"value\": \"application/json\" } ], " +
                "\"headerData\": [ \"%s\" ], ".formatted(headerDataBase64) +
                "\"payload\": \"%s\"".formatted(payloadBase64) +
                " } }");
    }

    @Test
    public void toJsonWithEmptyPayload() {
        var message = mock(Message.class);
        when(message.getHeaders()).thenReturn(Collections.emptyMap());
        when(message.getHeaderData()).thenReturn(Collections.emptyList());
        when(message.getPayload(byte[].class)).thenReturn(null);

        var fixture = new TestActionResult("receive", "actions.2.receive");
        fixture.setMessage(message);

        Assert.assertEquals(fixture.toJson(),
                "{ \"name\": \"receive\", \"path\": \"actions.2.receive\", " +
                "\"message\": { \"headers\": [  ], \"headerData\": [], \"payload\": \"\" } }");
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

        Assert.assertEquals(fixture.toJson(),
                "{ \"name\": \"send\", \"path\": \"actions.0.send\", " +
                "\"message\": { " +
                "\"headers\": [ { \"name\": \"operation\", \"value\": \"greet\" } ], " +
                "\"headerData\": [ \"%s\" ], ".formatted(headerDataBase64) +
                "\"payload\": \"\"" +
                " } }");
    }

    @Test
    public void toJsonWithChildActions() {
        var fixture = new TestActionResult("sequence", "actions.0.sequence");
        fixture.addAction(new TestActionResult("echo", "actions.0.sequence.actions.0.echo"));
        fixture.addAction(new TestActionResult("send", "actions.0.sequence.actions.1.send"));

        Assert.assertEquals(fixture.toJson(),
                "{ \"name\": \"sequence\", \"path\": \"actions.0.sequence\", " +
                "\"actions\": [ " +
                "{ \"name\": \"echo\", \"path\": \"actions.0.sequence.actions.0.echo\" }," +
                "{ \"name\": \"send\", \"path\": \"actions.0.sequence.actions.1.send\" }" +
                " ] }");
    }

    @Test
    public void toJsonWithIterations() {
        var fixture = new TestActionResult("iterate", "actions.0.iterate");
        fixture.addIteration(new TestActionResult("0", "actions.0.iterate"));
        fixture.addIteration(new TestActionResult("1", "actions.0.iterate"));

        Assert.assertEquals(fixture.toJson(),
                "{ \"name\": \"iterate\", \"path\": \"actions.0.iterate\", " +
                "\"iterations\": [ " +
                "{ \"name\": \"0\", \"path\": \"actions.0.iterate\" }," +
                "{ \"name\": \"1\", \"path\": \"actions.0.iterate\" }" +
                " ] }");
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

        Assert.assertTrue(json.contains("\"name\": \"sequence\""));
        Assert.assertTrue(json.contains("\"path\": \"actions.0.sequence\""));
        Assert.assertTrue(json.contains("\"error\": \"validation failed\""));
        Assert.assertTrue(json.contains("\"message\": {"));
        Assert.assertTrue(json.contains("\"payload\": \"%s\"".formatted(payloadBase64)));
        Assert.assertTrue(json.contains("\"actions\": ["));
        Assert.assertTrue(json.contains("\"iterations\": ["));
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

        Assert.assertTrue(json.contains("{ \"name\": \"key1\", \"value\": \"value1\" }"));
        Assert.assertTrue(json.contains("{ \"name\": \"key2\", \"value\": \"value2\" }"));
    }

    @Test
    public void toYamlMinimalResult() {
        var fixture = new TestActionResult("echo", "actions.0.echo");

        Assert.assertEquals(fixture.toYaml(),
                "name: \"echo\"\n" +
                "path: \"actions.0.echo\"\n");
    }

    @Test
    public void toYamlWithError() {
        var fixture = new TestActionResult("send", "actions.1.send");
        fixture.setError("Connection refused");

        Assert.assertEquals(fixture.toYaml(),
                "name: \"send\"\n" +
                "path: \"actions.1.send\"\n" +
                "error: |\n" +
                "  Connection refused\n");
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

        Assert.assertTrue(yaml.contains("name: \"send\""));
        Assert.assertTrue(yaml.contains("path: \"actions.1.send\""));
        Assert.assertTrue(yaml.contains("message:\n"));
        Assert.assertTrue(yaml.contains("headers:\n"));
        Assert.assertTrue(yaml.contains("- name: \"Content-Type\""));
        Assert.assertTrue(yaml.contains("value: \"application/json\""));
        Assert.assertTrue(yaml.contains("headerData: [ '%s' ]".formatted(headerDataBase64)));
        Assert.assertTrue(yaml.contains("payload: |\n"));
        Assert.assertTrue(yaml.contains("Hello"));
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

        Assert.assertTrue(yaml.contains("name: \"receive\""));
        Assert.assertTrue(yaml.contains("message:\n"));
        Assert.assertTrue(yaml.contains("headerData: []"));
        Assert.assertTrue(yaml.contains("payload: |\n"));
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

        Assert.assertTrue(yaml.contains("- name: \"operation\""));
        Assert.assertTrue(yaml.contains("value: \"greet\""));
        Assert.assertTrue(yaml.contains("headerData: [ '%s' ]".formatted(headerDataBase64)));
        Assert.assertTrue(yaml.contains("payload: |\n"));
    }

    @Test
    public void toYamlWithChildActions() {
        var fixture = new TestActionResult("sequence", "actions.0.sequence");
        fixture.addAction(new TestActionResult("echo", "actions.0.sequence.actions.0.echo"));
        fixture.addAction(new TestActionResult("send", "actions.0.sequence.actions.1.send"));

        String yaml = fixture.toYaml();

        Assert.assertTrue(yaml.contains("name: \"sequence\""));
        Assert.assertTrue(yaml.contains("actions:\n"));
        Assert.assertTrue(yaml.contains("name: \"echo\""));
        Assert.assertTrue(yaml.contains("path: \"actions.0.sequence.actions.0.echo\""));
        Assert.assertTrue(yaml.contains("name: \"send\""));
        Assert.assertTrue(yaml.contains("path: \"actions.0.sequence.actions.1.send\""));
    }

    @Test
    public void toYamlWithIterations() {
        var fixture = new TestActionResult("iterate", "actions.0.iterate");
        fixture.addIteration(new TestActionResult("0", "actions.0.iterate"));
        fixture.addIteration(new TestActionResult("1", "actions.0.iterate"));

        String yaml = fixture.toYaml();

        Assert.assertTrue(yaml.contains("name: \"iterate\""));
        Assert.assertTrue(yaml.contains("iterations:\n"));
        Assert.assertTrue(yaml.contains("name: \"0\""));
        Assert.assertTrue(yaml.contains("name: \"1\""));
        Assert.assertTrue(yaml.contains("path: \"actions.0.iterate\""));
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

        Assert.assertTrue(yaml.contains("- name: \"key1\""));
        Assert.assertTrue(yaml.contains("value: \"value1\""));
        Assert.assertTrue(yaml.contains("- name: \"key2\""));
        Assert.assertTrue(yaml.contains("value: \"value2\""));
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

        Assert.assertTrue(yaml.contains("name: \"sequence\""));
        Assert.assertTrue(yaml.contains("path: \"actions.0.sequence\""));
        Assert.assertTrue(yaml.contains("error: |"));
        Assert.assertTrue(yaml.contains("validation failed"));
        Assert.assertTrue(yaml.contains("message:\n"));
        Assert.assertTrue(yaml.contains("payload: |"));
        Assert.assertTrue(yaml.contains("actions:\n"));
        Assert.assertTrue(yaml.contains("iterations:\n"));
    }
}
