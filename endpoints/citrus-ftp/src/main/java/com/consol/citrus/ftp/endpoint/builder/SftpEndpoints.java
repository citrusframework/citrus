package com.consol.citrus.ftp.endpoint.builder;

import com.consol.citrus.endpoint.builder.ClientServerEndpointBuilder;
import com.consol.citrus.ftp.client.SftpClientBuilder;
import com.consol.citrus.ftp.server.SftpServerBuilder;

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
