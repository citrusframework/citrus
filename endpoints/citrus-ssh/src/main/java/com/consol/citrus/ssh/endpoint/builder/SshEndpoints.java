package com.consol.citrus.ssh.endpoint.builder;

import com.consol.citrus.endpoint.builder.ClientServerEndpointBuilder;
import com.consol.citrus.ssh.client.SshClientBuilder;
import com.consol.citrus.ssh.server.SshServerBuilder;

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

    /**
     * Static entry method for scp endpoints.
     * @return
     */
    public static SshEndpoints scp() {
        return new SshEndpoints();
    }
}
