package com.consol.citrus.docker.endpoint.builder;

import com.consol.citrus.docker.client.DockerClientBuilder;
import com.consol.citrus.endpoint.builder.ClientServerEndpointBuilder;

/**
 * @author Christoph Deppisch
 */
public final class DockerEndpoints extends ClientServerEndpointBuilder<DockerClientBuilder, DockerClientBuilder> {
    /**
     * Private constructor setting the client and server builder implementation.
     */
    private DockerEndpoints() {
        super(new DockerClientBuilder(), new DockerClientBuilder());
    }

    @Override
    public DockerClientBuilder server() {
        throw new UnsupportedOperationException("Citrus Docker stack has no support for server implementation");
    }

    /**
     * Static entry method for docker endpoints.
     * @return
     */
    public static DockerEndpoints docker() {
        return new DockerEndpoints();
    }
}
