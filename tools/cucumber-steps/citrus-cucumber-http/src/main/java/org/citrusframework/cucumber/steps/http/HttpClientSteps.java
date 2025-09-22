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

package org.citrusframework.cucumber.steps.http;

import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.citrusframework.Citrus;
import org.citrusframework.CitrusSettings;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.actions.HttpClientActionBuilder;
import org.citrusframework.http.actions.HttpClientRequestActionBuilder;
import org.citrusframework.http.actions.HttpClientResponseActionBuilder;
import org.citrusframework.http.client.BasicAuthClientHttpRequestFactory;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.citrusframework.variable.dictionary.DataDictionary;
import org.citrusframework.cucumber.steps.util.ResourceUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.citrusframework.TestActionBuilder.logger;
import static org.citrusframework.container.Wait.Builder.waitFor;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.PathExpressionValidationContext.Builder.pathExpression;

public class HttpClientSteps implements HttpSteps {

    @CitrusResource
    private TestCaseRunner runner;

    @CitrusFramework
    private Citrus citrus;

    @CitrusResource
    private TestContext context;

    private HttpClient httpClient;

    private String requestUrl;

    private Map<String, String> requestHeaders = new HashMap<>();
    private Map<String, String> responseHeaders = new HashMap<>();
    private Map<String, String> requestParams = new HashMap<>();

    private boolean headerNameIgnoreCase = HttpSettings.isHeaderNameIgnoreCase();
    private Map<String, Object> bodyValidationExpressions = new HashMap<>();

    private String requestMessageType;
    private String responseMessageType;

    private String requestBody;
    private String responseBody;

    private DataDictionary<?> outboundDictionary;
    private DataDictionary<?> inboundDictionary;

    private long timeout;

    private boolean forkMode = HttpSettings.getForkMode();

    private String authMethod = HttpSettings.getClientAuthMethod();
    private String authUser = HttpSettings.getClientAuthUser();
    private String authPassword = HttpSettings.getClientAuthPassword();

    private boolean useSslKeyStore = HttpSettings.isUseSslKeyStore();
    private String sslKeyStorePath = HttpSettings.getClientKeyStorePath();
    private String sslKeyStorePassword = HttpSettings.getClientKeyStorePassword();

    private boolean useSslTrustStore = HttpSettings.isUseSslTrustStore();
    private String sslTrustStorePath = HttpSettings.getTrustStorePath();
    private String sslTrustStorePassword = HttpSettings.getTrustStorePassword();

    @Before
    public void before(Scenario scenario) {
        if (httpClient == null) {
            if (citrus.getCitrusContext().getReferenceResolver().resolveAll(HttpClient.class).size() == 1L) {
                httpClient = citrus.getCitrusContext().getReferenceResolver().resolve(HttpClient.class);
            } else {
                httpClient = new HttpClientBuilder()
                        .timeout(HttpSettings.getTimeout())
                        .build();
            }
        }

        timeout = httpClient.getEndpointConfiguration().getTimeout();

        requestHeaders = new HashMap<>();
        responseHeaders = new HashMap<>();
        requestParams = new HashMap<>();
        requestMessageType = CitrusSettings.DEFAULT_MESSAGE_TYPE;
        responseMessageType = CitrusSettings.DEFAULT_MESSAGE_TYPE;
        requestBody = null;
        responseBody = null;
        bodyValidationExpressions = new HashMap<>();
        outboundDictionary = null;
        inboundDictionary = null;
    }

    @Given("^HTTP client \"([^\"\\s]+)\"$")
    public void setClient(String id) {
        if (!citrus.getCitrusContext().getReferenceResolver().isResolvable(id)) {
            throw new CitrusRuntimeException("Unable to find http client for id: " + id);
        }

        httpClient = citrus.getCitrusContext().getReferenceResolver().resolve(id, HttpClient.class);
    }

    @Given("^(?:URL|url): ([^\\s]+)$")
    public void setUrl(String url) {
        String resolvedUrl = context.replaceDynamicContentInString(url);

        if (resolvedUrl.startsWith("https")) {
            httpClient.getEndpointConfiguration().setRequestFactory(sslRequestFactory());
        }

        this.requestUrl = resolvedUrl;
    }

    @Given("^HTTP client (enable|disable) SSL truststore")
    public void setSecureTrustStore(String mode) {
        this.useSslTrustStore = "enable".equals(mode);
    }

    @Given("^HTTP client SSL truststore path ([^\\s]+)$")
    public void setSslTrustStorePath(String sslTrustStorePath) {
        this.sslTrustStorePath = sslTrustStorePath;
        this.useSslTrustStore = true;
    }

    @Given("^HTTP client SSL truststore password ([^\\s]+)$")
    public void setSslTrustStorePassword(String sslTrustStorePassword) {
        this.sslTrustStorePassword = sslTrustStorePassword;
    }

    @Given("^HTTP client (enable|disable) SSL keystore$")
    public void setSecureKeyStore(String mode) {
        this.useSslKeyStore = "enable".equals(mode);
    }

    @Given("^HTTP client SSL keystore path ([^\\s]+)$")
    public void setSslKeyStorePath(String sslKeyStorePath) {
        this.sslKeyStorePath = sslKeyStorePath;
        this.useSslKeyStore = true;
    }

    @Given("^HTTP client SSL keystore password ([^\\s]+)$")
    public void setSslKeyStorePassword(String sslKeyStorePassword) {
        this.sslKeyStorePassword = sslKeyStorePassword;
    }

    @Given("^HTTP client (enable|disable) basic auth$")
    public void setBasicAuth(String mode) {
        if ("enable".equals(mode)) {
            this.authMethod = "basic";
        } else {
            this.authMethod = "none";
        }
    }

    @Given("^HTTP client auth method (basic|digest|ntlm)$")
    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

    @Given("^HTTP client auth user ([^\\s]+)$")
    public void setAuthUser(String authUser) {
        this.authUser = authUser;
    }

    @Given("^HTTP client auth password ([^\\s]+)$")
    public void setAuthPassword(String authPassword) {
        this.authPassword = authPassword;
    }

    @Given("^HTTP request timeout is (\\d+)(?: ms| milliseconds)$")
    public void configureTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Given("^HTTP request fork mode is (enabled|disabled)$")
    public void configureForkMode(String mode) {
        this.forkMode = "enabled".equals(mode);
    }

    @Given("^HTTP header name ignore case is (enabled|disabled)$")
    public void configureHeaderNameIgnoreCase(String mode) {
        this.headerNameIgnoreCase = "enabled".equals(mode);
    }

    @Given("^(?:URL|url) is healthy$")
    public void healthCheck() {
        waitForHttpUrl(requestUrl);
    }

    @Given("^(?:URL|url|path) ([^\\s]+) is healthy$")
    public void healthCheck(String urlOrPath) {
        waitForHttpUrl(getRequestUrl(urlOrPath, context));
    }

    @Given("^wait for (?:URL|url|path) ([^\\s]+)$")
    public void waitForHttpUrl(String urlOrPath) {
        waitForHttpStatus(getRequestUrl(urlOrPath, context), 200);
    }

    @Given("^wait for (GET|HEAD|POST|PUT|PATCH|DELETE|OPTIONS|TRACE) on (?:URL|url|path) ([^\\s]+)$")
    public void waitForHttpUrlUsingMethod(String method, String urlOrPath) {
        waitForHttpStatusUsingMethod(method, getRequestUrl(urlOrPath, context), 200);
    }

    @Given("^wait for (?:URL|url|path) ([^\\s]+) to return (\\d+)(?: [^\\s]+)?$")
    public void waitForHttpStatus(String urlOrPath, Integer statusCode) {
        runner.given(waitFor().http()
                .milliseconds(timeout)
                .interval(timeout / 10)
                .status(statusCode)
                .url(getRequestUrl(urlOrPath, context)));
    }

    @Given("^wait for (GET|HEAD|POST|PUT|PATCH|DELETE|OPTIONS|TRACE) on (?:URL|url|path) ([^\\s]+) to return (\\d+)(?: [^\\s]+)?$")
    public void waitForHttpStatusUsingMethod(String method, String urlOrPath, Integer statusCode) {
        runner.given(waitFor().http()
                .milliseconds(timeout)
                .method(method)
                .interval(timeout / 10)
                .status(statusCode)
                .url(getRequestUrl(urlOrPath, context)));
    }

    @Then("^(?:expect|verify) HTTP response header ([^\\s]+)(?:=| is )\"(.+)\"$")
    public void addResponseHeader(String name, String value) {
        if (name.equals(HttpHeaders.CONTENT_TYPE)) {
            responseMessageType = getMessageType(value);
        }

        responseHeaders.put(name, value);
    }

    @Then("^(?:expect|verify) HTTP response headers$")
    public void addResponseHeaders(DataTable headers) {
        Map<String, String> headerPairs = headers.asMap(String.class, String.class);
        headerPairs.forEach(this::addResponseHeader);
    }

    @Given("^HTTP request header ([^\\s]+)(?:=| is )\"(.+)\"$")
    public void addRequestHeader(String name, String value) {
        if (name.equals(HttpHeaders.CONTENT_TYPE)) {
            requestMessageType = getMessageType(context.replaceDynamicContentInString(value));
        }

        requestHeaders.put(name, context.replaceDynamicContentInString(value));
    }

    @Given("^HTTP request query parameter ([^\\s]+)(?:=| is )\"(.+)\"$")
    public void addRequestQueryParam(String name, String value) {
        requestParams.put(name, value);
    }

    @Given("^HTTP request headers$")
    public void addRequestHeaders(DataTable headers) {
        Map<String, String> headerPairs = headers.asMap(String.class, String.class);
        headerPairs.forEach(this::addRequestHeader);
    }

    @Then("^(?:expect|verify) HTTP response expression: ([^\\s]+)(?:=| is )\"(.+)\"$")
    public void addBodyValidationExpression(String name, String value) {
        bodyValidationExpressions.put(name, value);
    }

    @Then("^(?:expect|verify) HTTP response expressions$")
    public void addBodyValidationExpressions(DataTable validationExpressions) {
        Map<String, String> expressions = validationExpressions.asMap(String.class, String.class);
        expressions.forEach(this::addBodyValidationExpression);
    }

    @Given("^HTTP request body$")
    public void setRequestBodyMultiline(String body) {
        setRequestBody(body);
    }

    @Given("^load HTTP request body ([^\\s]+)$")
    public void loadRequestBody(String file) {
        try {
            setRequestBody(FileUtils.readToString(ResourceUtils.resolve(file, context)));
        } catch (IOException e) {
            throw new CitrusRuntimeException(String.format("Failed to load body from file resource %s", file));
        }
    }

    @Given("^HTTP request body: (.+)$")
    public void setRequestBody(String body) {
        this.requestBody = body;
    }

    @Then("^(?:expect|verify) HTTP response body$")
    public void setResponseBodyMultiline(String body) {
        setResponseBody(body);
    }

    @Given("^(?:expect|verify) HTTP response body loaded from ([^\\s]+)$")
    public void loadResponseBody(String file) {
        try {
            setResponseBody(FileUtils.readToString(ResourceUtils.resolve(file, context)));
        } catch (IOException e) {
            throw new CitrusRuntimeException(String.format("Failed to load body from file resource %s", file));
        }
    }

    @Then("^(?:expect|verify) HTTP response body: (.+)$")
    public void setResponseBody(String body) {
        this.responseBody = body;
    }

    @When("^send HTTP request$")
    public void sendClientRequestFull(String requestData) {
        sendClientRequest(HttpMessage.fromRequestData(requestData));
    }

    @Then("^receive HTTP response$")
    public void receiveClientResponseFull(String responseData) {
        receiveClientResponse(HttpMessage.fromResponseData(responseData));
    }

    @When("^send (GET|HEAD|POST|PUT|PATCH|DELETE|OPTIONS|TRACE)$")
    public void sendClientRequestMultilineBody(String method) {
        sendClientRequest(method, null);
    }

    @When("^send (GET|HEAD|POST|PUT|PATCH|DELETE|OPTIONS|TRACE) ([^\"\\s]+)$")
    public void sendClientRequest(String method, String path) {
        sendClientRequest(createRequest(requestBody, requestHeaders, requestParams, method, path, context));
        requestBody = null;
        requestHeaders.clear();
        requestParams.clear();
    }

    @Then("^receive HTTP (\\d+)(?: [^\\s]+)?$")
    public void receiveClientResponse(Integer status) {
        receiveClientResponse(createResponse(responseBody, responseHeaders, status, context));
        responseBody = null;
        responseHeaders.clear();
    }

    /**
     * Sends client request.
     * @param request
     */
    private void sendClientRequest(HttpMessage request) {
        HttpClientActionBuilder.HttpClientSendActionBuilder sendBuilder = http().client(httpClient).send();
        HttpClientRequestActionBuilder.HttpMessageBuilderSupport requestBuilder;

        if (request.getRequestMethod() == null || request.getRequestMethod().equals(RequestMethod.GET)) {
            requestBuilder = sendBuilder.post().message(request);
        } else if (request.getRequestMethod().equals(RequestMethod.GET)) {
            requestBuilder = sendBuilder.get().message(request);
        } else if (request.getRequestMethod().equals(RequestMethod.PUT)) {
            requestBuilder = sendBuilder.put().message(request);
        } else if (request.getRequestMethod().equals(RequestMethod.DELETE)) {
            requestBuilder = sendBuilder.delete().message(request);
        } else if (request.getRequestMethod().equals(RequestMethod.HEAD)) {
            requestBuilder = sendBuilder.head().message(request);
        } else if (request.getRequestMethod().equals(RequestMethod.TRACE)) {
            requestBuilder = sendBuilder.trace().message(request);
        } else if (request.getRequestMethod().equals(RequestMethod.PATCH)) {
            requestBuilder = sendBuilder.patch().message(request);
        } else if (request.getRequestMethod().equals(RequestMethod.OPTIONS)) {
            requestBuilder = sendBuilder.options().message(request);
        } else {
            requestBuilder = sendBuilder.post().message(request);
        }

        requestBuilder.fork(forkMode);

        if (StringUtils.hasText(requestUrl)) {
            requestBuilder.uri(requestUrl);
        }

        requestBuilder.type(requestMessageType);

        if (outboundDictionary != null) {
            requestBuilder.dictionary(outboundDictionary);
        }

        if ("basic".equals(authMethod)) {
            try {
                httpClient.getEndpointConfiguration().setRequestFactory(basicAuthRequestFactory());
            } catch (Exception e) {
                throw new CitrusRuntimeException("Failed to configure basic auth on server", e);
            }
        } else {
            logger.warn("Unsupported auth method for Http server: '%s'".formatted(authMethod));
        }

        runner.run(requestBuilder);
    }

    /**
     * Receives client response.
     * @param response
     */
    private void receiveClientResponse(HttpMessage response) {
        HttpClientResponseActionBuilder.HttpMessageBuilderSupport responseBuilder = http().client(httpClient).receive()
                .response(HttpStatus.resolve(response.getStatusCode().value()))
                .message(response)
                .headerNameIgnoreCase(headerNameIgnoreCase);

        if (!bodyValidationExpressions.isEmpty()) {
            responseBuilder.validate(pathExpression().expressions(bodyValidationExpressions));
            bodyValidationExpressions.clear();
        }

        responseBuilder.timeout(timeout);
        responseBuilder.type(responseMessageType);

        if (inboundDictionary != null) {
            responseBuilder.dictionary(inboundDictionary);
        }

        runner.run(responseBuilder);
    }

    /**
     * Get secure request factory.
     * @return
     */
    private HttpComponentsClientHttpRequestFactory sslRequestFactory() {
        return new HttpComponentsClientHttpRequestFactory(sslClient());
    }

    /**
     * Get secure http client implementation with trust all strategy and noop host name verifier.
     * @return
     */
    private org.apache.hc.client5.http.classic.HttpClient sslClient() {
        try {
            SSLContextBuilder sslContextBuilder = SSLContexts.custom();

            if (useSslTrustStore && StringUtils.hasText(sslTrustStorePath)) {
                sslContextBuilder.loadTrustMaterial(ResourceUtils.resolve(sslTrustStorePath, context).getURL(), sslTrustStorePassword.toCharArray());
            } else {
                sslContextBuilder.loadTrustMaterial(TrustAllStrategy.INSTANCE);
            }

            if (useSslKeyStore && StringUtils.hasText(sslKeyStorePath)) {
                    sslContextBuilder.loadKeyMaterial(ResourceUtils.resolve(sslKeyStorePath, context).getURL(),
                            sslKeyStorePassword.toCharArray(), sslKeyStorePassword.toCharArray());
            }

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslContextBuilder.build(), NoopHostnameVerifier.INSTANCE);

            PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(sslSocketFactory)
                    .build();

            return HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException | IOException | UnrecoverableKeyException | CertificateException e) {
            throw new CitrusRuntimeException("Failed to create http client for ssl connection", e);
        }
    }

    /**
     * Helper method concatenating base request URL and given relative URL resource path. In case given parameter us a full qualified
     * URL itself use this URL as a result. Adds error handling in case base request URL is not set properly and avoids duplicate path
     * separators in concatenated URLs.
     *
     * @param urlOrPath
     * @param context
     * @return
     */
    private String getRequestUrl(String urlOrPath, TestContext context) {
        String resolvedUrlOrPath = context.replaceDynamicContentInString(urlOrPath);

        if (StringUtils.hasText(resolvedUrlOrPath) && resolvedUrlOrPath.startsWith("http")) {
            return resolvedUrlOrPath;
        }

        String url;
        if (StringUtils.hasText(requestUrl)) {
            url = requestUrl;
        } else if (StringUtils.hasText(httpClient.getEndpointConfiguration().getRequestUrl())) {
            url = httpClient.getEndpointConfiguration().getRequestUrl();
        } else {
            throw new CitrusRuntimeException("Must provide a base request URL first when using relative resource path: " + urlOrPath);
        }

        if (!StringUtils.hasText(resolvedUrlOrPath) || resolvedUrlOrPath.equals("/")) {
            return url;
        }

        return (url.endsWith("/") ? url : url + "/") + (resolvedUrlOrPath.startsWith("/") ? resolvedUrlOrPath.substring(1) : resolvedUrlOrPath);
    }

    private HttpComponentsClientHttpRequestFactory basicAuthRequestFactory() {
        try {
            BasicAuthClientHttpRequestFactory requestFactory = new BasicAuthClientHttpRequestFactory();

            URL url;
            if (StringUtils.hasText(requestUrl)) {
                url = new URL(requestUrl);
            } else if (StringUtils.hasText(httpClient.getEndpointConfiguration().getRequestUrl())) {
                url = new URL(httpClient.getEndpointConfiguration().getRequestUrl());
            } else {
                throw new CitrusRuntimeException("Must provide a base request URL when configuring basic auth on Http client");
            }

            AuthScope authScope = new AuthScope(url.getProtocol(), url.getHost(), url.getPort(), "", "basic");
            requestFactory.setAuthScope(authScope);

            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(authUser, authPassword.toCharArray());
            requestFactory.setCredentials(credentials);

            requestFactory.initialize();
            return requestFactory.getObject();
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to configure basic auth on Http client", e);
        }
    }

    /**
     * Specifies the inboundDictionary.
     *
     * @param inboundDictionary
     */
    public void setInboundDictionary(DataDictionary<?> inboundDictionary) {
        this.inboundDictionary = inboundDictionary;
    }

    /**
     * Specifies the outboundDictionary.
     *
     * @param outboundDictionary
     */
    public void setOutboundDictionary(DataDictionary<?> outboundDictionary) {
        this.outboundDictionary = outboundDictionary;
    }
}
