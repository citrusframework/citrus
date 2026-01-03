package org.citrusframework.ssh.message;

import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.ssh.model.SshMarshaller;
import org.citrusframework.ssh.model.SshRequest;
import org.citrusframework.ssh.model.SshResponse;
import org.citrusframework.xml.StringResult;
import org.citrusframework.xml.StringSource;

public class SshMessage extends DefaultMessage {

    private SshRequest request;
    private SshResponse response;

    private SshMarshaller marshaller = new SshMarshaller();

    public SshMessage() {
        super();
    }

    public SshMessage(Message message) {
        super(message);
    }

    private SshMessage(SshRequest request) {
        super(request);
        this.request = request;
    }

    private SshMessage(SshResponse response) {
        super(response);
        this.response = response;
    }

    public static SshMessage request(String command) {
        return request(command, "");
    }

    public static SshMessage request(String command, String stdin) {
        return new SshMessage(new SshRequest(nvl(command), nvl(stdin)));
    }

    public static SshMessage response(String stdout, String stderr, int exit) {
        return new SshMessage(new SshResponse(nvl(stdout), nvl(stderr), exit));
    }

    public SshMessage command(String command) {
        SshRequest r = getRequest();
        this.request = new SshRequest(nvl(command), nvl(r.getStdin()));
        this.response = null;
        super.setPayload(this.request);
        return this;
    }

    public SshMessage stdin(String stdin) {
        SshRequest r = getRequest();
        this.request = new SshRequest(nvl(r.getCommand()), nvl(stdin));
        this.response = null;
        super.setPayload(this.request);
        return this;
    }

    public SshMessage stdout(String stdout) {
        SshResponse r = getResponse();
        this.response = new SshResponse(nvl(stdout), nvl(r.getStderr()), r.getExit());
        this.request = null;
        super.setPayload(this.response);
        return this;
    }

    public SshMessage stderr(String stderr) {
        SshResponse r = getResponse();
        this.response = new SshResponse(nvl(r.getStdout()), nvl(stderr), r.getExit());
        this.request = null;
        super.setPayload(this.response);
        return this;
    }

    public SshMessage exit(int exit) {
        SshResponse r = getResponse();
        this.response = new SshResponse(nvl(r.getStdout()), nvl(r.getStderr()), exit);
        this.request = null;
        super.setPayload(this.response);
        return this;
    }

    public SshRequest getRequest() {
        if (request == null) {
            Object payload = super.getPayload();
            if (payload instanceof SshRequest sshRequest) {
                request = sshRequest;
            } else if (payload instanceof String payloadString) {
                request = (SshRequest) marshaller.unmarshal(new StringSource(payloadString));
            } else {
                request = new SshRequest("", "");
            }
            response = null;
        }
        return request;
    }

    public SshResponse getResponse() {
        if (response == null) {
            Object payload = super.getPayload();
            if (payload instanceof SshResponse sshResponse) {
                response = sshResponse;
            } else if (payload instanceof String payloadString) {
                response = (SshResponse) marshaller.unmarshal(new StringSource(payloadString));
            } else {
                response = new SshResponse("", "", 0);
            }
            request = null;
        }
        return response;
    }

    public boolean isRequest() {
        return request != null || super.getPayload() instanceof SshRequest;
    }

    public boolean isResponse() {
        return response != null || super.getPayload() instanceof SshResponse;
    }

    @Override
    public Object getPayload() {
        StringResult result = new StringResult();

        if (isRequest()) {
            marshaller.marshal(getRequest(), result);
            return result.toString();
        }

        if (isResponse()) {
            marshaller.marshal(getResponse(), result);
            return result.toString();
        }

        return super.getPayload();
    }

    @Override
    public <T> T getPayload(Class<T> type) {
        if (SshRequest.class.isAssignableFrom(type)) {
            return type.cast(getRequest());
        }

        if (SshResponse.class.isAssignableFrom(type)) {
            return type.cast(getResponse());
        }

        if (String.class.equals(type)) {
            return type.cast(getPayload());
        }

        return super.getPayload(type);
    }

    public SshMessage marshaller(SshMarshaller marshaller) {
        this.marshaller = marshaller;
        return this;
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
