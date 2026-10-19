/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.grpc.tls;

import java.io.IOException;
import java.io.InputStream;

import io.grpc.ChannelCredentials;
import io.grpc.ServerCredentials;
import io.grpc.TlsChannelCredentials;
import io.grpc.TlsServerCredentials;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;

/**
 * Creates TLS credentials for gRPC clients and servers from certificate resources. The client
 * trusts a configured certificate, the server serves a configured certificate chain and private
 * key.
 *
 * @since 5.1
 */
public final class GrpcTlsSupport {

    private GrpcTlsSupport() {
        // prevent instantiation
    }

    /**
     * Creates client channel credentials trusting the given certificate resource.
     *
     * @param trustCertificate the trust certificate resource
     * @return the channel credentials
     */
    public static ChannelCredentials clientCredentials(String trustCertificate) {
        Resource resource = resource(trustCertificate);

        try (InputStream stream = resource.getInputStream()) {
            return TlsChannelCredentials.newBuilder()
                    .trustManager(stream)
                    .build();
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read TLS trust certificate '%s'".formatted(trustCertificate), e);
        }
    }

    /**
     * Creates server credentials from the given certificate chain and private key resources.
     *
     * @param certificateChain the certificate chain resource
     * @param privateKey the private key resource
     * @return the server credentials
     */
    public static ServerCredentials serverCredentials(String certificateChain, String privateKey) {
        Resource chainResource = resource(certificateChain);
        Resource keyResource = resource(privateKey);

        try (InputStream chainStream = chainResource.getInputStream();
                InputStream keyStream = keyResource.getInputStream()) {
            return TlsServerCredentials.newBuilder()
                    .keyManager(chainStream, keyStream)
                    .build();
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read TLS server credentials", e);
        }
    }

    private static Resource resource(String path) {
        Resource resource = Resources.create(path);
        if (!resource.exists()) {
            throw new CitrusRuntimeException("Failed to find TLS resource '%s'".formatted(path));
        }

        return resource;
    }
}
