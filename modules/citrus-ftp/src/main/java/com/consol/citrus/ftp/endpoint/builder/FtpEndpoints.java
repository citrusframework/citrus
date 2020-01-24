package com.consol.citrus.ftp.endpoint.builder;

import com.consol.citrus.endpoint.builder.ClientServerEndpointBuilder;
import com.consol.citrus.ftp.client.FtpClientBuilder;
import com.consol.citrus.ftp.server.FtpServerBuilder;

/**
 * @author Christoph Deppisch
 */
public final class FtpEndpoints extends ClientServerEndpointBuilder<FtpClientBuilder, FtpServerBuilder> {
    /**
     * Private constructor setting the client and server builder implementation.
     */
    private FtpEndpoints() {
        super(new FtpClientBuilder(), new FtpServerBuilder());
    }

    /**
     * Static entry method for ftp endpoints.
     * @return
     */
    public static FtpEndpoints ftp() {
        return new FtpEndpoints();
    }
}
