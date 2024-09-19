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

package org.citrusframework.knative;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;

import io.fabric8.knative.client.DefaultKnativeClient;
import io.fabric8.knative.client.KnativeClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.citrusframework.Citrus;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kubernetes.KubernetesVariableNames;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

public final class KnativeSupport {

    private KnativeSupport() {
        // prevent instantiation of utility class
    }

    public static KnativeClient getKnativeClient(Citrus citrus) {
        if (citrus.getCitrusContext().getReferenceResolver().resolveAll(KnativeClient.class).size() == 1L) {
            return citrus.getCitrusContext().getReferenceResolver().resolve(KnativeClient.class);
        } else {
            return new DefaultKnativeClient();
        }
    }

    public static CustomResourceDefinitionContext knativeCRDContext(String knativeComponent, String kind, String version) {
        return new CustomResourceDefinitionContext.Builder()
                .withName(String.format("%s.%s.knative.dev", kind, knativeComponent))
                .withGroup(String.format("%s.knative.dev", knativeComponent))
                .withVersion(version)
                .withPlural(kind)
                .withScope("Namespaced")
                .build();
    }

    public static String knativeApiVersion() {
        return KnativeSettings.getApiVersion();
    }

    public static String knativeMessagingGroup() {
        return KnativeSettings.getKnativeMessagingGroup();
    }

    public static String knativeEventingGroup() {
        return KnativeSettings.getKnativeEventingGroup();
    }

    /**
     * Retrieve current namespace set as test variable.
     * In case no suitable test variable is available use namespace loaded from Kubernetes settings via environment settings.
     * @param context potentially holding the namespace variable.
     * @return
     */
    public static String getNamespace(TestContext context) {
        if (context.getVariables().containsKey(KnativeVariableNames.NAMESPACE.value())) {
            return context.getVariable(KnativeVariableNames.NAMESPACE.value());
        }

        if (context.getVariables().containsKey(KubernetesVariableNames.NAMESPACE.value())) {
            return context.getVariable(KubernetesVariableNames.NAMESPACE.value());
        }

        return KnativeSettings.getNamespace();
    }

    /**
     * Get secure request factory.
     * @return
     */
    public static HttpComponentsClientHttpRequestFactory sslRequestFactory() {
        return new HttpComponentsClientHttpRequestFactory(sslClient());
    }

    /**
     * Get secure http client implementation with trust all strategy and noop host name verifier.
     * @return
     */
    private static HttpClient sslClient() {
        try {
            SSLContext sslcontext = SSLContexts
                    .custom()
                    .loadTrustMaterial(TrustAllStrategy.INSTANCE)
                    .build();

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslcontext, NoopHostnameVerifier.INSTANCE);

            PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(sslSocketFactory)
                    .build();

            return HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new CitrusRuntimeException("Failed to create http client for ssl connection", e);
        }
    }
}
