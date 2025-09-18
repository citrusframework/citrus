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

package org.citrusframework.cucumber.steps.knative;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.stream.Collectors;
import javax.net.ssl.SSLContext;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.citrusframework.Citrus;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.knative.KnativeSettings;
import org.citrusframework.knative.ce.CloudEventSupport;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import static org.citrusframework.knative.actions.KnativeActionBuilder.knative;

public class SendEventSteps {

    @CitrusResource
    private TestCaseRunner runner;

    @CitrusFramework
    private Citrus citrus;

    private HttpClient httpClient;

    private String brokerUrl = KnativeSettings.getBrokerUrl();
    private long timeout = KnativeSettings.getEventProducerTimeout();
    private String eventData;

    @Before
    public void before(Scenario scenario) {
        if (httpClient == null) {
            if (citrus.getCitrusContext().getReferenceResolver().resolveAll(HttpClient.class).size() == 1L) {
                httpClient = citrus.getCitrusContext().getReferenceResolver().resolve(HttpClient.class);
                timeout = httpClient.getEndpointConfiguration().getTimeout();
            } else {
                httpClient = new HttpClientBuilder()
                        .timeout(timeout)
                        .build();
            }
        }

        eventData = null;
    }

    @Given("^Knative broker (?:URL|url): ([^\\s]+)$")
    public void setUrl(String url) {
        if (url.startsWith("https")) {
            httpClient.getEndpointConfiguration().setRequestFactory(sslRequestFactory());
        }

        this.brokerUrl = url;
    }

    @Given("^Knative client \"([^\"\\s]+)\"$")
    public void setClient(String id) {
        if (!citrus.getCitrusContext().getReferenceResolver().isResolvable(id)) {
            throw new CitrusRuntimeException("Unable to find Knative client for id: " + id);
        }

        httpClient = citrus.getCitrusContext().getReferenceResolver().resolve(id, HttpClient.class);
    }

    @Given("^Knative event producer timeout is (\\d+)(?: ms| milliseconds)$")
    public void configureTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Given("^Knative event data$")
    public void setEventDataMultiline(String data) {
        setEventData(data);
    }

    @Given("^Knative event data: (.+)$")
    public void setEventData(String data) {
        this.eventData = data;
    }

    @When("^(?:create|send) Knative event$")
    public void createEvent(DataTable attributes) {
        runner.run(knative()
                .event()
                .send()
                .client(httpClient)
                .brokerUrl(brokerUrl)
                .eventData(eventData)
                .attributes(attributes.asMap(String.class, Object.class)));
    }

    @When("^(?:create|send) Knative event as json$")
    public void createEventJson(String json) {
        Map<String, Object> attributes = CloudEventSupport.attributesFromJson(json)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        runner.run(knative()
                .event()
                .send()
                .client(httpClient)
                .brokerUrl(brokerUrl)
                .eventData(eventData)
                .attributes(attributes));
    }

    /**
     * Get secure request factory.
     */
    private HttpComponentsClientHttpRequestFactory sslRequestFactory() {
        return new HttpComponentsClientHttpRequestFactory(sslClient());
    }

    /**
     * Get secure http client implementation with trust all strategy and noop host name verifier.
     */
    private org.apache.hc.client5.http.classic.HttpClient sslClient() {
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
