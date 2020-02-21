package com.consol.citrus.mail.endpoint.builder;

import com.consol.citrus.endpoint.builder.ClientServerEndpointBuilder;
import com.consol.citrus.mail.client.MailClientBuilder;
import com.consol.citrus.mail.server.MailServerBuilder;

/**
 * @author Christoph Deppisch
 */
public final class MailEndpoints extends ClientServerEndpointBuilder<MailClientBuilder, MailServerBuilder> {
    /**
     * Private constructor setting the client and server builder implementation.
     */
    private MailEndpoints() {
        super(new MailClientBuilder(), new MailServerBuilder());
    }

    /**
     * Static entry method for mail endpoints.
     * @return
     */
    public static MailEndpoints mail() {
        return new MailEndpoints();
    }
}
