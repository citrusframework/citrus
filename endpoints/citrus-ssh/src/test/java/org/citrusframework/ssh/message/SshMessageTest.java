package org.citrusframework.ssh.message;

import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.ssh.model.SshMarshaller;
import org.citrusframework.ssh.model.SshRequest;
import org.citrusframework.ssh.model.SshResponse;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class SshMessageTest {

    @Test
    public void shouldWrapExistingMessageUsingMessageConstructor() {
        Message original = new DefaultMessage().setPayload(new SshRequest("echo", "wrapped"));

        SshMessage message = new SshMessage(original);

        SshRequest request = message.getPayload(SshRequest.class);
        assertEquals(request.getCommand(), "echo");
        assertEquals(request.getStdin(), "wrapped");
    }

    @Test
    public void shouldCreateRequestAndMarshalToXmlStringPayload() {
        SshMessage message = SshMessage.request("echo hello", "in");

        String xml = message.getPayload(String.class);

        assertNotNull(xml);
        assertTrue(xml.contains("<ssh-request"));
        assertTrue(xml.contains("echo hello"));
        assertTrue(xml.contains("in"));

        SshRequest request = message.getPayload(SshRequest.class);
        assertEquals(request.getCommand(), "echo hello");
        assertEquals(request.getStdin(), "in");
    }

    @Test
    public void shouldCreateRequestWithoutStdinAndMarshalToXmlStringPayload() {
        SshMessage message = SshMessage.request("echo hello");

        String xml = message.getPayload(String.class);

        assertNotNull(xml);
        assertTrue(xml.contains("<ssh-request"));
        assertTrue(xml.contains("echo hello"));
        assertTrue(xml.contains("<stdin></stdin>"));

        SshRequest request = message.getPayload(SshRequest.class);
        assertEquals(request.getCommand(), "echo hello");
        assertEquals(request.getStdin(), "");
    }

    @Test
    public void shouldCreateResponseAndMarshalToXmlStringPayload() {
        SshMessage message = SshMessage.response("out", "err", 0);

        String xml = message.getPayload(String.class);

        assertNotNull(xml);
        assertTrue(xml.contains("<ssh-response"));
        assertTrue(xml.contains("out"));
        assertTrue(xml.contains("err"));
        assertTrue(xml.contains("0"));

        SshResponse response = message.getPayload(SshResponse.class);
        assertEquals(response.getStdout(), "out");
        assertEquals(response.getStderr(), "err");
        assertEquals(response.getExit(), 0);
    }

    @Test
    public void shouldSupportFluentRequestRebuildWithoutSetters() {
        SshMessage message = SshMessage.request("ls", "a");

        message.command("pwd").stdin("b");

        SshRequest request = message.getPayload(SshRequest.class);
        assertEquals(request.getCommand(), "pwd");
        assertEquals(request.getStdin(), "b");

        String xml = message.getPayload(String.class);
        assertTrue(xml.contains("<ssh-request"));
        assertTrue(xml.contains("pwd"));
        assertTrue(xml.contains("b"));
    }

    @Test
    public void shouldSupportFluentResponseRebuildWithoutSetters() {
        SshMessage message = SshMessage.response("a", "b", 1);

        message.stdout("x").stderr("y").exit(2);

        SshResponse response = message.getPayload(SshResponse.class);
        assertEquals(response.getStdout(), "x");
        assertEquals(response.getStderr(), "y");
        assertEquals(response.getExit(), 2);

        String xml = message.getPayload(String.class);
        assertTrue(xml.contains("<ssh-response"));
        assertTrue(xml.contains("x"));
        assertTrue(xml.contains("y"));
        assertTrue(xml.contains("2"));
    }

    @Test
    public void shouldUnmarshalRequestFromStringPayload() {
        String xml = SshMessage.request("whoami", "in").getPayload(String.class);

        SshMessage message = new SshMessage();
        message.setPayload(xml);

        SshRequest request = message.getPayload(SshRequest.class);
        assertEquals(request.getCommand(), "whoami");
        assertEquals(request.getStdin(), "in");
    }

    @Test
    public void shouldUnmarshalResponseFromStringPayload() {
        String xml = SshMessage.response("out", "err", 0).getPayload(String.class);

        SshMessage message = new SshMessage();
        message.setPayload(xml);

        SshResponse request = message.getPayload(SshResponse.class);
        assertEquals(request.getStdout(), "out");
        assertEquals(request.getStderr(), "err");
        assertEquals(request.getExit(), 0);
    }

    @Test
    public void shouldUseDefaultObjectsWhenPayloadIsNeitherModelNorStringInRequest() {
        SshMessage message = new SshMessage();
        message.setPayload(42);

        SshRequest req = message.getRequest();
        assertEquals(req.getCommand(), "");
        assertEquals(req.getStdin(), "");
    }

    @Test
    public void shouldUseDefaultObjectsWhenPayloadIsModelNorStringInResponse() {
        SshMessage message = new SshMessage();
        message.setPayload(42);

        SshResponse resp = message.getResponse();
        assertEquals(resp.getStdout(), "");
        assertEquals(resp.getStderr(), "");
        assertEquals(resp.getExit(), 0);
    }

    @Test
    public void shouldReturnFalseForIsRequestWhenPayloadIsNotSshRequest() {
        SshMessage message = new SshMessage();
        message.setPayload(new SshResponse("out", "err", 0));

        assertFalse(message.isRequest());
    }

    @Test
    public void shouldReturnFalseForIsResponseWhenPayloadIsNotSshResponse() {
        SshMessage message = new SshMessage();
        message.setPayload(new SshRequest("echo", "in"));

        assertFalse(message.isResponse());
    }

    @Test
    public void shouldDefaultNullValuesToEmptyStringsToAvoidMarshallingIssues() {
        SshMessage request = SshMessage.request(null, null);
        SshMessage response = SshMessage.response(null, null, 0);

        String requestXml = request.getPayload(String.class);
        String responseXml = response.getPayload(String.class);

        assertNotNull(requestXml);
        assertTrue(requestXml.contains("<ssh-request"));

        SshRequest req = request.getPayload(SshRequest.class);
        assertEquals(req.getCommand(), "");
        assertEquals(req.getStdin(), "");

        assertNotNull(responseXml);
        assertTrue(responseXml.contains("<ssh-response"));

        SshResponse resp = response.getPayload(SshResponse.class);
        assertEquals(resp.getStdout(), "");
        assertEquals(resp.getStderr(), "");
        assertEquals(resp.getExit(), 0);
    }

    @Test
    public void shouldFallbackToSuperGetPayloadWhenNotRequestOrResponse() {
        SshMessage message = new SshMessage();
        message.setPayload(42);

        Object payload = message.getPayload();
        assertEquals(payload, 42);
    }

    @Test
    public void shouldFallbackToSuperTypedGetPayloadWhenNotRequestOrResponse() {
        SshMessage message = new SshMessage();
        message.setPayload(42);

        Integer payload = message.getPayload(Integer.class);
        assertEquals(payload, Integer.valueOf(42));
    }

    @Test
    public void shouldReturnSshRequestWhenPayloadIsSshRequest() {
        SshMessage message = new SshMessage();
        message.setPayload(new SshRequest("echo", "Hello Citrus"));

        SshRequest request = message.getPayload(SshRequest.class);
        assertEquals(request.getCommand(), "echo");
        assertEquals(request.getStdin(), "Hello Citrus");
    }

    @Test
    public void shouldReturnSshResponseWhenPayloadIsSshResponse() {
        SshMessage message = new SshMessage();
        message.setPayload(new SshResponse("out", "err", 0));

        SshResponse request = message.getPayload(SshResponse.class);
        assertEquals(request.getStdout(), "out");
        assertEquals(request.getStderr(), "err");
        assertEquals(request.getExit(), 0);
    }

    @Test
    public void shouldUseCustomMarshallerWhenProvided() {
        SshMarshaller custom = Mockito.mock(SshMarshaller.class);

        SshRequest expected = new SshRequest("custom-cmd", "custom-stdin");
        Mockito.when(custom.unmarshal(Mockito.any())).thenReturn(expected);

        SshMessage message = new SshMessage().marshaller(custom);
        message.setPayload("<ssh-request><command>x</command><stdin>y</stdin></ssh-request>");

        SshRequest actual = message.getPayload(SshRequest.class);

        assertSame(actual, expected);
        Mockito.verify(custom).unmarshal(Mockito.any());
    }
}
