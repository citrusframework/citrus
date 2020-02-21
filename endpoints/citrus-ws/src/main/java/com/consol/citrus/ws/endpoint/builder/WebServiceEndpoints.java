package com.consol.citrus.ws.endpoint.builder;

import com.consol.citrus.endpoint.builder.ClientServerEndpointBuilder;
import com.consol.citrus.ws.client.WebServiceClientBuilder;
import com.consol.citrus.ws.server.WebServiceServerBuilder;

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
