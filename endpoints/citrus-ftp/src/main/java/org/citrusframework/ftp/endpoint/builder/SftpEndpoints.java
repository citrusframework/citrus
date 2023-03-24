package org.citrusframework.ftp.endpoint.builder;

import org.citrusframework.endpoint.builder.ClientServerEndpointBuilder;
import org.citrusframework.ftp.client.SftpClientBuilder;
import org.citrusframework.ftp.server.SftpServerBuilder;

/**
 * @author Christoph Deppisch
 */
public final class SftpEndpoints extends ClientServerEndpointBuilder<SftpClientBuilder, SftpServerBuilder> {
    /**
     * Private constructor setting the client and server builder implementation.
     */
    private SftpEndpoints() {
        super(new SftpClientBuilder(), new SftpServerBuilder());
    }

    /**
     * Static entry method for sftp endpoints.
     * @return
     */
    public static SftpEndpoints sftp() {
        return new SftpEndpoints();
    }
}
