package org.citrusframework.ftp.endpoint.builder;

import org.citrusframework.endpoint.builder.ClientServerEndpointBuilder;
import org.citrusframework.ftp.client.ScpClientBuilder;
import org.citrusframework.ftp.server.SftpServerBuilder;

/**
 * @author Christoph Deppisch
 */
public final class ScpEndpoints extends ClientServerEndpointBuilder<ScpClientBuilder, SftpServerBuilder> {
    /**
     * Private constructor setting the client and server builder implementation.
     */
    private ScpEndpoints() {
        super(new ScpClientBuilder(), new SftpServerBuilder());
    }

    /**
     * Static entry method for scp endpoints.
     * @return
     */
    public static ScpEndpoints scp() {
        return new ScpEndpoints();
    }
}
