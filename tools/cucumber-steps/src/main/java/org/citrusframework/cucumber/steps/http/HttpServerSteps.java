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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.citrusframework.Citrus;
import org.citrusframework.CitrusSettings;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.actions.HttpServerActionBuilder;
import org.citrusframework.http.actions.HttpServerRequestActionBuilder;
import org.citrusframework.http.actions.HttpServerResponseActionBuilder;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.security.BasicAuthConstraint;
import org.citrusframework.http.security.SecurityHandlerFactory;
import org.citrusframework.http.security.User;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.citrusframework.variable.dictionary.DataDictionary;
import org.citrusframework.cucumber.util.ResourceUtils;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.citrusframework.TestActionBuilder.logger;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.PathExpressionValidationContext.Builder.pathExpression;

public class HttpServerSteps implements HttpSteps {

    @CitrusResource
    private TestCaseRunner runner;

    @CitrusFramework
    private Citrus citrus;

    @CitrusResource
    private TestContext context;

    private HttpServer httpServer;

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

    private int securePort = HttpSettings.getSecurePort();
    private int serverPort = HttpSettings.getServerPort();
    private String serverName = HttpSettings.getServerName();

    private boolean useSslConnector = HttpSettings.isUseSslConnector();
    private boolean useSslKeyStore = HttpSettings.isUseSslKeyStore();

    private String sslKeyStorePath = HttpSettings.getServerKeyStorePath();
    private String sslKeyStorePassword = HttpSettings.getServerKeyStorePassword();

    private String authMethod = HttpSettings.getServerAuthMethod();
    private String authPath = HttpSettings.getServerAuthPath();
    private String[] authUserRoles = HttpSettings.getServerAuthUserRoles();
    private String authUser = HttpSettings.getServerAuthUser();
    private String authPassword = HttpSettings.getServerAuthPassword();

    private long timeout = HttpSettings.getTimeout();

    @Before
    public void before(Scenario scenario) {
        if (httpServer == null) {
            if (citrus.getCitrusContext().getReferenceResolver().isResolvable(serverName)) {
                httpServer = citrus.getCitrusContext().getReferenceResolver().resolve(serverName, HttpServer.class);
                serverPort = httpServer.getPort();
                timeout = httpServer.getDefaultTimeout();
            } else if (citrus.getCitrusContext().getReferenceResolver().resolveAll(HttpServer.class).size() == 1L) {
                httpServer = citrus.getCitrusContext().getReferenceResolver().resolve(HttpServer.class);
                serverName = httpServer.getName();
                serverPort = httpServer.getPort();
                timeout = httpServer.getDefaultTimeout();
            }
        }

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

    @Given("^HTTP server \"([^\"\\s]+)\"$")
    public void setServer(String name) {
        this.serverName = name;
        if (citrus.getCitrusContext().getReferenceResolver().isResolvable(name)) {
            httpServer = citrus.getCitrusContext().getReferenceResolver().resolve(name, HttpServer.class);
        }
    }

    @Given("^HTTP server header name ignore case is (enabled|disabled)$")
    public void configureHeaderNameIgnoreCase(String mode) {
        this.headerNameIgnoreCase = "enabled".equals(mode);
    }

    @Given("^HTTP server \"([^\"\\s]+)\" with configuration$")
    public void setServerWithProperties(String name, DataTable properties) {
        configureServer(properties.asMap(String.class, String.class));
        setServer(name);
    }

    @Given("^(?:create|new) HTTP server \"([^\"\\s]+)\"$")
    public void newServer(String name) {
        this.serverName = name;
        if (citrus.getCitrusContext().getReferenceResolver().isResolvable(name)) {
            httpServer = citrus.getCitrusContext().getReferenceResolver().resolve(name, HttpServer.class);
        } else {
            httpServer = getOrCreateHttpServer();
        }
    }

    @Given("^(?:create|new) HTTP server \"([^\"\\s]+)\" with configuration$")
    public void newServerWithProperties(String name, DataTable properties) {
        configureServer(properties.asMap(String.class, String.class));
        newServer(name);
    }

    @Given("^HTTP server listening on port (\\d+)$")
    public void setServerPort(int port) {
        this.serverPort = port;
    }

    @Given("^HTTP server secure port (\\d+)$")
    public void setSecureServerPort(int port) {
        this.securePort = port;
        setSecureConnector("enable");
    }

    @Given("^HTTP server (enable|disable) basic auth$")
    public void setBasicAuth(String mode) {
        if ("enable".equals(mode)) {
            this.authMethod = "basic";
        } else {
            this.authMethod = "none";
        }
    }

    @Given("^HTTP server auth method (basic|digest|ntlm)$")
    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

    @Given("^HTTP server auth path ([^\\s]+)$")
    public void setAuthPath(String authPath) {
        this.authPath = authPath;
    }

    @Given("^HTTP server auth user roles ([^\\s]+)$")
    public void setAuthUserRoles(String authUserRoles) {
        this.authUserRoles = authUserRoles.split(",");
    }

    @Given("^HTTP server auth user ([^\\s]+)$")
    public void setAuthUser(String authUser) {
        this.authUser = authUser;
    }

    @Given("^HTTP server auth password ([^\\s]+)$")
    public void setAuthPassword(String authPassword) {
        this.authPassword = authPassword;
    }

    @Given("^HTTP server (enable|disable) SSL$")
    public void setSecureConnector(String mode) {
        useSslConnector = "enable".equals(mode);
        if (useSslConnector) {
            this.useSslKeyStore = true;
        }
    }

    @Given("^HTTP server (enable|disable) SSL keystore$")
    public void setSecureKeyStore(String mode) {
        this.useSslKeyStore = "enable".equals(mode);
    }

    @Given("^HTTP server SSL keystore path ([^\\s]+)$")
    public void setSslKeyStorePath(String sslKeyStorePath) {
        this.sslKeyStorePath = sslKeyStorePath;
        this.useSslKeyStore = true;
    }

    @Given("^HTTP server SSL keystore password ([^\\s]+)$")
    public void setSslKeyStorePassword(String sslKeyStorePassword) {
        this.sslKeyStorePassword = sslKeyStorePassword;
    }

    @Given("^start HTTP server$")
    public void startServer() {
        HttpServer httpServer = getOrCreateHttpServer();
        if (!httpServer.isRunning()) {
            httpServer.start();
        }
    }

    @Given("^stop HTTP server$")
    public void stopServer() {
        HttpServer httpServer = getOrCreateHttpServer();
        if (httpServer.isRunning()) {
            httpServer.stop();
        }
    }

    @Given("^HTTP server timeout is (\\d+)(?: ms| milliseconds)$")
    public void configureTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Then("^(?:expect|verify) HTTP request header: ([^\\s]+)(?:=| is )\"(.+)\"$")
    public void addRequestHeader(String name, String value) {
        if (name.equals(HttpHeaders.CONTENT_TYPE)) {
            requestMessageType = getMessageType(value);
        }

        requestHeaders.put(name, value);
    }

    @Then("^(?:expect|verify) HTTP request headers$")
    public void addRequestHeaders(DataTable headers) {
        Map<String, String> headerPairs = headers.asMap(String.class, String.class);
        headerPairs.forEach(this::addRequestHeader);
    }

    @Given("^(?:expect|verify) HTTP request query parameter ([^\\s]+)(?:=| is )\"(.+)\"$")
    public void addRequestQueryParam(String name, String value) {
        requestParams.put(name, value);
    }

    @Given("^HTTP response header: ([^\\s]+)(?:=| is )\"(.+)\"$")
    public void addResponseHeader(String name, String value) {
        if (name.equals(HttpHeaders.CONTENT_TYPE)) {
            responseMessageType = getMessageType(value);
        }

        responseHeaders.put(name, value);
    }

    @Given("^HTTP response headers$")
    public void addResponseHeaders(DataTable headers) {
        Map<String, String> headerPairs = headers.asMap(String.class, String.class);
        headerPairs.forEach(this::addResponseHeader);
    }

    @Then("^(?:expect|verify) HTTP request expression: ([^\\s]+)(?:=| is )\"(.+)\"$")
    public void addBodyValidationExpression(String name, String value) {
        bodyValidationExpressions.put(name, value);
    }

    @Then("^(?:expect|verify) HTTP request expressions$")
    public void addBodyValidationExpressions(DataTable validationExpressions) {
        Map<String, String> expressions = validationExpressions.asMap(String.class, String.class);
        expressions.forEach(this::addBodyValidationExpression);
    }

    @Given("^HTTP response body$")
    public void setResponseBodyMultiline(String body) {
        setResponseBody(body);
    }

    @Given("^load HTTP response body ([^\\s]+)$")
    public void loadResponseBody(String file) {
        try {
            setResponseBody(FileUtils.readToString(ResourceUtils.resolve(file, context)));
        } catch (IOException e) {
            throw new CitrusRuntimeException(String.format("Failed to load body from file resource %s", file));
        }
    }

    @Given("^HTTP response body: (.+)$")
    public void setResponseBody(String body) {
        this.responseBody = body;
    }

    @Then("^(?:expect|verify) HTTP request body$")
    public void setRequestBodyMultiline(String body) {
        setRequestBody(body);
    }

    @Then("^(?:expect|verify) HTTP request body loaded from ([^\\s]+)$")
    public void loadRequestBody(String file) {
        try {
            setRequestBody(FileUtils.readToString(ResourceUtils.resolve(file, context)));
        } catch (IOException e) {
            throw new CitrusRuntimeException(String.format("Failed to load body from file resource %s", file));
        }
    }

    @Then("^(?:expect|verify) HTTP request body: (.+)$")
    public void setRequestBody(String body) {
        this.requestBody = body;
    }

    @When("^receive HTTP request$")
    public void receiveServerRequestFull(String requestData) {
        receiveServerRequest(HttpMessage.fromRequestData(requestData));
    }

    @Then("^send HTTP response$")
    public void sendServerResponseFull(String responseData) {
        sendServerResponse(HttpMessage.fromResponseData(responseData));
    }

    @When("^receive (GET|HEAD|POST|PUT|PATCH|DELETE|OPTIONS|TRACE)$")
    public void receiveServerRequestMultilineBody(String method) {
        receiveServerRequest(method, null);
    }

    @When("^receive (GET|HEAD|POST|PUT|PATCH|DELETE|OPTIONS|TRACE) ([^\"\\s]+)$")
    public void receiveServerRequest(String method, String path) {
        receiveServerRequest(createRequest(requestBody, requestHeaders, requestParams, method, path, context));
        requestBody = null;
        requestHeaders.clear();
        requestParams.clear();
    }

    @Then("^send HTTP (\\d+)(?: [^\\s]+)?$")
    public void sendServerResponse(Integer status) {
        sendServerResponse(createResponse(responseBody, responseHeaders, status, context));
        responseBody = null;
        responseHeaders.clear();
    }

    /**
     * Receives server request.
     * @param request
     */
    public void receiveServerRequest(HttpMessage request) {
        HttpServer httpServer = getOrCreateHttpServer();
        if (!httpServer.isRunning()) {
            httpServer.start();
        }

        HttpServerActionBuilder.HttpServerReceiveActionBuilder receiveBuilder = http().server(httpServer).receive();
        HttpServerRequestActionBuilder.HttpMessageBuilderSupport requestBuilder;

        if (request.getRequestMethod() == null || request.getRequestMethod().equals(RequestMethod.POST)) {
            requestBuilder = receiveBuilder.post().message(request);
        } else if (request.getRequestMethod().equals(RequestMethod.GET)) {
            requestBuilder = receiveBuilder.get().message(request);
        } else if (request.getRequestMethod().equals(RequestMethod.PUT)) {
            requestBuilder = receiveBuilder.put().message(request);
        } else if (request.getRequestMethod().equals(RequestMethod.DELETE)) {
            requestBuilder = receiveBuilder.delete().message(request);
        } else if (request.getRequestMethod().equals(RequestMethod.HEAD)) {
            requestBuilder = receiveBuilder.head().message(request);
        } else if (request.getRequestMethod().equals(RequestMethod.TRACE)) {
            requestBuilder = receiveBuilder.trace().message(request);
        } else if (request.getRequestMethod().equals(RequestMethod.PATCH)) {
            requestBuilder = receiveBuilder.patch().message(request);
        } else if (request.getRequestMethod().equals(RequestMethod.OPTIONS)) {
            requestBuilder = receiveBuilder.options().message(request);
        } else {
            requestBuilder = receiveBuilder.post().message(request);
        }

        requestBuilder.headerNameIgnoreCase(headerNameIgnoreCase);

        if (!bodyValidationExpressions.isEmpty()) {
            requestBuilder.validate(pathExpression().expressions(bodyValidationExpressions));
            bodyValidationExpressions.clear();
        }

        requestBuilder
                .timeout(timeout)
                .type(requestMessageType);

        if (inboundDictionary != null) {
            requestBuilder.dictionary(inboundDictionary);
        }

        runner.run(requestBuilder);
    }

    /**
     * Create a new server instance and bind it to the context.
     * @return
     */
    public HttpServer getOrCreateHttpServer() {
        if (httpServer != null && httpServer.getName().equals(serverName)) {
            return httpServer;
        }

        if (citrus.getCitrusContext().getReferenceResolver().isResolvable(serverName)) {
            httpServer = citrus.getCitrusContext().getReferenceResolver().resolve(serverName, HttpServer.class);
            serverName = httpServer.getName();
            serverPort = httpServer.getPort();
            timeout = httpServer.getDefaultTimeout();
            return httpServer;
        }

        httpServer = new HttpServerBuilder()
                .autoStart(true)
                .timeout(timeout)
                .port(serverPort)
                .name(serverName)
                .build();

        if (useSslConnector) {
            httpServer.setConnector(sslConnector());
        }

        if ("basic".equals(authMethod)) {
            try {
                SecurityHandlerFactory securityHandlerFactory = basicAuthSecurityHandler();
                securityHandlerFactory.initialize();
                httpServer.setSecurityHandler(securityHandlerFactory.getObject());
            } catch (Exception e) {
                throw new CitrusRuntimeException("Failed to configure basic auth on server", e);
            }
        } else {
            logger.warn("Unsupported auth method for Http server: '%s'".formatted(authMethod));
        }

        citrus.getCitrusContext().getReferenceResolver().bind(serverName, httpServer);
        httpServer.initialize();

        return httpServer;
    }

    /**
     * Configure server from given properties map.
     * @param settings
     */
    private void configureServer(Map<String, String> settings) {
        setServerPort(Optional.ofNullable(settings.get("port")).map(context::replaceDynamicContentInString).map(Integer::parseInt).orElse(serverPort));
        configureTimeout(Optional.ofNullable(settings.get("timeout")).map(context::replaceDynamicContentInString).map(Long::valueOf).orElse(timeout));

        if (settings.containsKey("sslKeyStorePath")) {
            setSslKeyStorePath(settings.get("sslKeyStorePath"));
        }

        if (settings.containsKey("sslKeyStorePassword")) {
            setSslKeyStorePassword(settings.get("sslKeyStorePassword"));
        }

        if (settings.containsKey("securePort")) {
            setSecureServerPort(Integer.parseInt(context.replaceDynamicContentInString(settings.get("securePort"))));
        } else if (Boolean.parseBoolean(context.replaceDynamicContentInString(settings.getOrDefault("secure", "false")))) {
            setSecureConnector("enable");
        }

        setAuthMethod(context.replaceDynamicContentInString(settings.getOrDefault("authMethod", authMethod)));
        setAuthUser(context.replaceDynamicContentInString(settings.getOrDefault("authUser", authUser)));
        setAuthPassword(context.replaceDynamicContentInString(settings.getOrDefault("authPassword", authPassword)));

        if (settings.containsKey("authUserRoles")) {
            setAuthUserRoles(context.replaceDynamicContentInString(settings.get("authUserRoles")));
        }
        setAuthPath(context.replaceDynamicContentInString(settings.getOrDefault("authPath", authPath)));
    }

    /**
     * Sends server response.
     * @param response
     */
    public void sendServerResponse(HttpMessage response) {
        response.setType(responseMessageType);

        HttpServerResponseActionBuilder.HttpMessageBuilderSupport responseBuilder = http().server(httpServer)
                .send()
                .response(HttpStatus.resolve(response.getStatusCode().value()))
                .message(response);

        if (outboundDictionary != null) {
            responseBuilder.dictionary(outboundDictionary);
        }


        runner.run(responseBuilder);
    }

    /**
     * Sends server response.
     * @param status
     */
    public void sendServerResponse(HttpStatus status) {
        runner.run(http().server(httpServer)
                .send()
                .response(status));
    }

    private ServerConnector sslConnector() {
        ServerConnector connector = new ServerConnector(new Server(),
                new SslConnectionFactory(sslContextFactory(), HttpVersion.HTTP_1_1.asString()),
                new HttpConnectionFactory(httpConfiguration()));
        connector.setPort(securePort);
        return connector;
    }

    private HttpConfiguration httpConfiguration() {
        HttpConfiguration parent = new HttpConfiguration();
        parent.setSecureScheme("https");
        parent.setSecurePort(securePort);
        HttpConfiguration configuration = new HttpConfiguration(parent);
        SecureRequestCustomizer secureRequestCustomizer = new SecureRequestCustomizer();
        secureRequestCustomizer.setSniHostCheck(false);
        configuration.setCustomizers(Collections.singletonList(secureRequestCustomizer));
        return configuration;
    }

    private SslContextFactory.Server sslContextFactory() {
        try {
            SslContextFactory.Server contextFactory = new SslContextFactory.Server();
            if (useSslKeyStore && StringUtils.hasText(sslKeyStorePath)) {
                contextFactory.setKeyStorePath(getKeyStorePathPath());
                contextFactory.setKeyStorePassword(context.replaceDynamicContentInString(sslKeyStorePassword));
            }
            return contextFactory;
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read keystore file in path: " + sslKeyStorePath);
        }
    }

    private String getKeyStorePathPath() throws IOException {
        if (sslKeyStorePath.equals(HttpSettings.SERVER_KEYSTORE_PATH_DEFAULT)) {
            File tmpKeyStore = File.createTempFile("server", ".jks");

            try (InputStream in = ResourceUtils.resolve(sslKeyStorePath, context).getInputStream()) {
                Files.copy(in, tmpKeyStore.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return tmpKeyStore.getPath();
            }
        } else {
            return ResourceUtils.resolve(sslKeyStorePath, context).getLocation();
        }
    }

    private SecurityHandlerFactory basicAuthSecurityHandler() {
        SecurityHandlerFactory securityHandlerFactory = new SecurityHandlerFactory();
        securityHandlerFactory.setUsers(Collections.singletonList(new User(authUser, authPassword, authUserRoles)));
        securityHandlerFactory.setConstraints(Collections.singletonMap(authPath, new BasicAuthConstraint(authUserRoles)));

        return securityHandlerFactory;
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

    /**
     * Specify the request message type.
     * @param requestType
     */
    public void setRequestMessageType(String requestType) {
        this.requestMessageType = requestType;
    }
}
