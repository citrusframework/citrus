package org.citrusframework.citrus.ftp.endpoint.builder;

import org.citrusframework.citrus.endpoint.builder.ClientServerEndpointBuilder;
import org.citrusframework.citrus.ftp.client.ScpClientBuilder;
import org.citrusframework.citrus.ftp.server.SftpServerBuilder;

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
