package com.consol.citrus.kubernetes.endpoint.builder;

import com.consol.citrus.endpoint.builder.ClientServerEndpointBuilder;
import com.consol.citrus.kubernetes.client.KubernetesClientBuilder;

/**
 * @author Christoph Deppisch
 */
public final class KubernetesEndpoints extends ClientServerEndpointBuilder<KubernetesClientBuilder, KubernetesClientBuilder> {
    /**
     * Private constructor setting the client and server builder implementation.
     */
    private KubernetesEndpoints() {
        super(new KubernetesClientBuilder(), new KubernetesClientBuilder());
    }

    @Override
    public KubernetesClientBuilder server() {
        throw new UnsupportedOperationException("Citrus Kubernetes stack has no support for server implementation");
    }

    /**
     * Static entry method for docker endpoints.
     * @return
     */
    public static KubernetesEndpoints kubernetes() {
        return new KubernetesEndpoints();
    }
}
