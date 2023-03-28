package org.citrusframework.ssh.endpoint.builder;

import org.citrusframework.endpoint.builder.ClientServerEndpointBuilder;
import org.citrusframework.ssh.client.SshClientBuilder;
import org.citrusframework.ssh.server.SshServerBuilder;

/**
 * @author Christoph Deppisch
 */
public final class SshEndpoints extends ClientServerEndpointBuilder<SshClientBuilder, SshServerBuilder> {
    /**
     * Private constructor setting the client and server builder implementation.
     */
    private SshEndpoints() {
        super(new SshClientBuilder(), new SshServerBuilder());
    }

    /**
     * Static entry method for ssh endpoints.
     * @return
     */
    public static SshEndpoints ssh() {
        return new SshEndpoints();
    }

}
