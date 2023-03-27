package org.citrusframework.citrus.mail.endpoint.builder;

import org.citrusframework.citrus.endpoint.builder.ClientServerEndpointBuilder;
import org.citrusframework.citrus.mail.client.MailClientBuilder;
import org.citrusframework.citrus.mail.server.MailServerBuilder;

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
