package org.citrusframework.ws.endpoint.builder;

import org.citrusframework.endpoint.builder.ClientServerEndpointBuilder;
import org.citrusframework.ws.client.WebServiceClientBuilder;
import org.citrusframework.ws.server.WebServiceServerBuilder;

/**
 * @author Christoph Deppisch
 */
public final class WebServiceEndpoints extends ClientServerEndpointBuilder<WebServiceClientBuilder, WebServiceServerBuilder>  {


    /**
     * Private constructor setting the client and server builder implementation.
     */
    private WebServiceEndpoints() {
        super(new WebServiceClientBuilder(), new WebServiceServerBuilder());
    }

    /**
     * Static entry method for Soap client and server endpoints.
     * @return
     */
    public static WebServiceEndpoints soap() {
        return new WebServiceEndpoints();
    }
}
