package org.citrusframework.ftp.endpoint.builder;

import org.citrusframework.endpoint.builder.ClientServerEndpointBuilder;
import org.citrusframework.ftp.client.FtpClientBuilder;
import org.citrusframework.ftp.server.FtpServerBuilder;

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
