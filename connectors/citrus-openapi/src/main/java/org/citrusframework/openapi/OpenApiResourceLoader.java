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

package org.citrusframework.openapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.apicurio.datamodels.Library;
import io.apicurio.datamodels.openapi.models.OasDocument;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import static javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier;
import static javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory;
import static org.apache.hc.core5.http.HttpHeaders.ACCEPT;
import static org.apache.hc.core5.http.Method.GET;
import static org.citrusframework.util.FileUtils.getFileResource;
import static org.citrusframework.util.FileUtils.readToString;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Loads Open API specifications from different locations like file resource or web resource.
 */
public final class OpenApiResourceLoader {

    private static final RawResolver RAW_RESOLVER = new RawResolver();

    private static final OasResolver OAS_RESOLVER = new OasResolver();

    /**
     * Prevent instantiation of utility class.
     */
    private OpenApiResourceLoader() {
        super();
    }

    /**
     * Loads the specification from a file resource. Either classpath or file system resource path is supported.
     */
    public static OasDocument fromFile(String resource) {
        return fromFile(getFileResource(resource), OAS_RESOLVER);
    }

    /**
     * Loads the raw specification from a file resource. Either classpath or file system resource path is supported.
     */
    public static String rawFromFile(String resource) {
        return fromFile(getFileResource(resource),
                RAW_RESOLVER);
    }

    /**
     * Loads the specification from a resource.
     */
    public static OasDocument fromFile(Resource resource) {
        return fromFile(resource, OAS_RESOLVER);
    }

    /**
     * Loads the raw specification from a resource.
     */
    public static String rawFromFile(Resource resource) {
        return fromFile(resource, RAW_RESOLVER);
    }

    private static <T> T fromFile(Resource resource, Resolver<T> resolver) {
        try {
            return resolve(readToString(resource), resolver);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse Open API specification: " + resource, e);
        }
    }

    /**
     * Loads specification from given web URL location.
     */
    public static OasDocument fromWebResource(URL url) {
        return fromWebResource(url, OAS_RESOLVER);
    }

    /**
     * Loads raw specification from given web URL location.
     */
    public static String rawFromWebResource(URL url) {
        return fromWebResource(url, RAW_RESOLVER);
    }

    private static <T> T fromWebResource(URL url, Resolver<T> resolver) {
        URLConnection connection = null;
        try {
            connection = url.openConnection();

            if (connection instanceof HttpURLConnection httpURLConnection) {
                httpURLConnection.setRequestMethod(GET.name());
                connection.setRequestProperty(ACCEPT, APPLICATION_JSON_VALUE);

                int status = httpURLConnection.getResponseCode();
                if (status > 299) {
                    throw new IllegalStateException(
                            "Failed to retrieve Open API specification: " + url,
                            new IOException(readToString(httpURLConnection.getErrorStream())));
                }
            }

            try (InputStream inputStream = connection.getInputStream()) {
                return resolve(readToString(inputStream), resolver);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to retrieve Open API specification: " + url, e);
        } finally {
            if (connection instanceof HttpURLConnection httpURLConnection) {
                httpURLConnection.disconnect();
            }
        }
    }

    /**
     * Loads specification from given web URL location using secured Http connection.
     */
    public static OasDocument fromSecuredWebResource(URL url) {
        return fromSecuredWebResource(url, OAS_RESOLVER);
    }

    /**
     * Loads raw specification from given web URL location using secured Http connection.
     */
    public static String rawFromSecuredWebResource(URL url) {
        return fromSecuredWebResource(url, RAW_RESOLVER);
    }

    private static <T> T fromSecuredWebResource(URL url, Resolver<T> resolver) {
        Objects.requireNonNull(url);

        HttpsURLConnection connection = null;
        try {
            SSLContext sslcontext = SSLContexts
                    .custom()
                    .loadTrustMaterial(TrustAllStrategy.INSTANCE)
                    .build();

            setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
            setDefaultHostnameVerifier(NoopHostnameVerifier.INSTANCE);

            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod(GET.name());
            connection.setRequestProperty(ACCEPT, APPLICATION_JSON_VALUE);

            int status = connection.getResponseCode();
            if (status > 299) {
                throw new IllegalStateException("Failed to retrieve Open API specification: " + url,
                        new IOException(readToString(connection.getErrorStream())));
            } else {
                return resolve(readToString(connection.getInputStream()), resolver);
            }
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new IllegalStateException("Failed to create https client for ssl connection", e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to retrieve Open API specification: " + url, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static <T> T resolve(String specification, Resolver<T> resolver) {
        if (isJsonSpec(specification)) {
            return resolver.resolveFromString(specification);
        }

        final JsonNode node = OpenApiSupport.json().convertValue(OpenApiSupport.yaml().load(specification), JsonNode.class);
        return resolver.resolveFromNode(node);
    }

    private static boolean isJsonSpec(final String specification) {
        return specification.trim().startsWith("{");
    }

    private interface Resolver<T> {

        T resolveFromString(String specification);

        T resolveFromNode(JsonNode node);

    }

    /**
     * {@link Resolver} implementation, that resolves to {@link OasDocument}.
     */
    private static class OasResolver implements Resolver<OasDocument> {

        @Override
        public OasDocument resolveFromString(String specification) {
            return (OasDocument) Library.readDocumentFromJSONString(specification);
        }

        @Override
        public OasDocument resolveFromNode(JsonNode node) {
            return (OasDocument) Library.readDocument(node);
        }
    }

    /**
     * {@link Resolver} implementation, that resolves to {@link String}.
     */
    private static class RawResolver implements Resolver<String> {

        private static final ObjectMapper mapper = new ObjectMapper();

        @Override
        public String resolveFromString(String specification) {
            return specification;
        }

        @Override
        public String resolveFromNode(JsonNode node) {

            try {
                return mapper.writeValueAsString(node);
            } catch (JsonProcessingException e) {
                throw new CitrusRuntimeException("Unable to write OpenApi specification node to string!", e);
            }
        }
    }
}
