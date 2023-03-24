package org.citrusframework.mail.endpoint.builder;

import org.citrusframework.endpoint.builder.ClientServerEndpointBuilder;
import org.citrusframework.mail.client.MailClientBuilder;
import org.citrusframework.mail.server.MailServerBuilder;

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
