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

package org.citrusframework.cucumber.steps.camel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spring.SpringCamelContext;
import org.citrusframework.Citrus;
import org.citrusframework.CitrusSettings;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.camel.context.CamelReferenceResolver;
import org.citrusframework.camel.endpoint.CamelEndpoint;
import org.citrusframework.camel.endpoint.CamelEndpointConfiguration;
import org.citrusframework.camel.endpoint.CamelSyncEndpoint;
import org.citrusframework.camel.endpoint.CamelSyncEndpointConfiguration;
import org.citrusframework.context.TestContext;
import org.citrusframework.cucumber.util.ResourceUtils;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.ByteArrayResource;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;
import static org.citrusframework.camel.dsl.CamelSupport.camel;

public class CamelSteps {

    @CitrusResource
    private TestCaseRunner runner;

    @CitrusFramework
    private Citrus citrus;

    @CitrusResource
    private TestContext context;

    private CamelContext camelContext;

    private final String contextName = CamelSettings.getContextName();

    private final Map<String, CamelEndpoint> endpoints = new HashMap<>();

    private Map<String, Object> headers = new HashMap<>();
    private String body;
    private String messageType = CitrusSettings.DEFAULT_MESSAGE_TYPE;

    private long timeout = CamelSettings.getTimeout();

    private boolean globalCamelContext = false;
    private boolean autoRemoveResources = CamelSettings.isAutoRemoveResources();

    private ExchangePattern exchangePattern = ExchangePattern.InOnly;

    @Before
    public void before(Scenario scenario) {
        if (camelContext == null) {
            if (citrus.getCitrusContext().getReferenceResolver().resolveAll(CamelContext.class).size() == 1L) {
                camelContext = citrus.getCitrusContext().getReferenceResolver().resolve(CamelContext.class);
                globalCamelContext = true;
            } else if (citrus.getCitrusContext().getReferenceResolver().isResolvable(contextName)) {
                camelContext = citrus.getCitrusContext().getReferenceResolver().resolve(contextName, CamelContext.class);
                globalCamelContext = true;
            }
        }

        if (camelContext != null && !(context.getReferenceResolver() instanceof CamelReferenceResolver)) {
            context.setReferenceResolver(new CamelReferenceResolver(camelContext)
                    .withFallback(citrus.getCitrusContext().getReferenceResolver()));
        }

        headers = new HashMap<>();
        body = null;
    }

    @After
    public void after(Scenario scenario) {
        if (autoRemoveResources) {
            endpoints.clear();
            destroyCamelContext();
        }
    }

    @Given("^Disable auto removal of Camel resources$")
    public void disableAutoRemove() {
        autoRemoveResources = false;
    }

    @Given("^Enable auto removal of Camel resources$")
    public void enableAutoRemove() {
        autoRemoveResources = true;
    }

    @Given("^Camel exchange pattern (InOut|InOnly)$")
    public void setExchangePattern(String exchangePattern) {
        this.exchangePattern = ExchangePattern.valueOf(exchangePattern);
    }

    @Given("^(?:Default|New) Camel context$")
    public void defaultContext() {
        verifyNonGlobalContext();
        destroyCamelContext();
        camelContext();
    }

    @Given("^(?:Default|New) global Camel context$")
    public void defaultGlobalContext() {
        destroyCamelContext();
        citrus.getCitrusContext().bind(contextName, camelContext());
        globalCamelContext = true;
    }

    @Given("^(?:Default|New) Spring Camel context$")
    public void camelContext(String beans) {
        verifyNonGlobalContext();
        destroyCamelContext();
        springCamelContext(beans);
    }

    @Given("^(?:Default|New) global Spring Camel context$")
    public void defaultGlobalContext(String beans) {
        destroyCamelContext();
        citrus.getCitrusContext().bind(contextName, springCamelContext(beans));
        globalCamelContext = true;
    }

    @Given("^Camel consumer timeout is (\\d+)(?: ms| milliseconds)$")
    public void configureTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Given("^bind to Camel registry ([^\"\\s]+)\\.groovy$")
    public void bindComponent(String name, String configurationScript) {
        runner.run(camel().camelContext(camelContext())
                .bind()
                .component(name, configurationScript));
    }

    @Given("^load to Camel registry ([^\"\\s]+)\\.groovy$")
    public void loadComponent(String filePath) throws IOException {
        Resource scriptFile = ResourceUtils.resolve(filePath + ".groovy", context);
        String script = FileUtils.readToString(scriptFile);
        final String fileName = FileUtils.getFileName(scriptFile.getLocation());
        final String baseName = Optional.ofNullable(fileName)
                .map(f -> f.lastIndexOf("."))
                .filter(index -> index >= 0)
                .map(index -> fileName.substring(0, index))
                .orElse(fileName);
        bindComponent(baseName, script);
    }

    @Given("^Camel route ([^\\s]+)\\.xml")
    public void camelRouteXml(String id, String routeSpec) throws Exception {
        runner.run(camel().camelContext(camelContext())
                .route()
                .create(routeSpec)
                .routeId(id));
    }

    @Given("^Camel route ([^\\s]+)\\.groovy")
    public void camelRouteGroovy(String id, String route) throws Exception {
        runner.run(camel().camelContext(camelContext())
                .route()
                .create(route)
                .routeId(id));
    }

    @Given("^load Camel route ([^\\s]+)\\.(groovy|xml)")
    public void loadCamelRoute(String fileName, String language) throws Exception {
        String route = FileUtils.readToString(ResourceUtils.resolve("%s.%s".formatted(fileName, language), context));
        switch (language) {
            case "groovy":
                camelRouteGroovy(fileName, route);
                break;
            case "xml":
                camelRouteXml(fileName, route);
                break;
        }
    }

    @Given("^start Camel route ([^\\s]+)$")
    public void startRoute(String routeId) {
        runner.run(camel().camelContext(camelContext())
                        .route()
                        .start(routeId));
    }

    @Given("^stop Camel route ([^\\s]+)$")
    public void stopRoute(String routeId) {
        runner.run(camel().camelContext(camelContext())
                        .route()
                        .stop(routeId));
    }

    @Given("^remove Camel route ([^\\s]+)$")
    public void removeRoute(String routeId) {
        runner.run(camel().camelContext(camelContext())
                        .route()
                        .remove(routeId));
    }

    @Given("^Camel exchange message header ([^\\s]+)(?:=| is )\"(.+)\"$")
    @Then("^(?:expect|verify) Camel exchange message header ([^\\s]+)(?:=| is )\"(.+)\"$")
    public void addMessageHeader(String name, Object value) {
        headers.put(name, value);
    }

    @Given("^Camel exchange message headers$")
    public void addMessageHeaders(DataTable headers) {
        Map<String, Object> headerPairs = headers.asMap(String.class, Object.class);
        headerPairs.forEach(this::addMessageHeader);
    }

    @Given("^Camel exchange body type: (.+)$")
    @Then("^(?:expect|verify) Camel exchange body type: (.+)$")
    public void setExchangeBodyType(String messageType) {
        this.messageType = messageType;
    }

    @Given("^Camel exchange body$")
    @Then("^(?:expect|verify) Camel exchange body$")
    public void setExchangeBodyMultiline(String body) {
        setExchangeBody(body);
    }

    @Given("^Camel exchange body: (.+)$")
    @Then("^(?:expect|verify) Camel exchange body: (.+)$")
    public void setExchangeBody(String body) {
        this.body = body;
    }

    @Given("^load Camel exchange body ([^\\s]+)$")
    @Then("^(?:expect|verify) Camel exchange body loaded from ([^\\s]+)$")
    public void loadExchangeBody(String file) {
        try {
            this.body = FileUtils.readToString(ResourceUtils.resolve(file, context));
        } catch (IOException e) {
            throw new CitrusRuntimeException(String.format("Failed to load body from file resource %s", file));
        }
    }

    @When("^send Camel exchange to\\(\"(.+)\"\\)$")
    public void sendExchange(String endpointUri) {
        runner.run(send().endpoint(camelEndpoint(endpointUri))
                .message()
                .type(messageType)
                .body(body)
                .headers(headers));

        body = null;
        headers.clear();
    }

    @Then("^(?:receive|expect|verify) Camel exchange from\\(\"(.+)\"\\)$")
    public void receiveExchange(String endpointUri) {
        runner.run(receive().endpoint(camelEndpoint(endpointUri))
                .timeout(timeout)
                .transform(camel(camelContext()).convertBodyTo(String.class))
                .message()
                .type(messageType)
                .body(body)
                .headers(headers));

        body = null;
        headers.clear();
    }

    @When("^send Camel exchange to\\(\"(.+)\"\\) with body$")
    public void sendExchangeMultilineBody(String endpointUri, String body) {
        setExchangeBody(body);
        sendExchange(endpointUri);
    }

    @When("^send Camel exchange to\\(\"(.+)\"\\) with body: (.+)$")
    public void sendExchangeBody(String endpointUri, String body) {
        setExchangeBody(body);
        sendExchange(endpointUri);
    }

    @When("^send Camel exchange to\\(\"(.+)\"\\) with body and headers: (.+)$")
    public void sendMessageBodyAndHeaders(String endpointUri, String body, DataTable headers) {
        setExchangeBody(body);
        addMessageHeaders(headers);
        sendExchange(endpointUri);
    }

    @Then("^(?:receive|expect|verify) Camel exchange from\\(\"(.+)\"\\) with body$")
    public void receiveExchangeBodyMultiline(String endpointUri, String body) {
        setExchangeBody(body);
        receiveExchange(endpointUri);
    }
    @Then("^(?:receive|expect|verify) Camel exchange from\\(\"(.+)\"\\) with body: (.+)$")
    public void receiveExchangeBody(String endpointUri, String body) {
        setExchangeBody(body);
        receiveExchange(endpointUri);
    }

    @Then("^(?:receive|expect|verify) Camel exchange from\\(\"(.+)\"\\) message with body and headers: (.+)$")
    public void receiveFromJms(String endpointUri, String body, DataTable headers) {
        setExchangeBody(body);
        addMessageHeaders(headers);
        receiveExchange(endpointUri);
    }

    // **************************
    // Helpers
    // **************************

    private CamelEndpoint camelEndpoint(String endpointUri) {
        if (endpoints.containsKey(endpointUri)) {
            return endpoints.get(endpointUri);
        }

        if (exchangePattern.equals(ExchangePattern.InOut)) {
            CamelSyncEndpointConfiguration endpointConfiguration = new CamelSyncEndpointConfiguration();
            endpointConfiguration.setCamelContext(camelContext());
            endpointConfiguration.setEndpointUri(endpointUri);
            endpointConfiguration.setTimeout(timeout);

            CamelSyncEndpoint endpoint = new CamelSyncEndpoint(endpointConfiguration);

            endpoints.put(endpointUri, endpoint);

            return endpoint;
        } else {
            CamelEndpointConfiguration endpointConfiguration = new CamelEndpointConfiguration();
            endpointConfiguration.setCamelContext(camelContext());
            endpointConfiguration.setEndpointUri(endpointUri);
            endpointConfiguration.setTimeout(timeout);
            CamelEndpoint endpoint = new CamelEndpoint(endpointConfiguration);

            endpoints.put(endpointUri, endpoint);

            return endpoint;
        }
    }

    private CamelContext camelContext() {
        if (camelContext == null) {
            try {
                camelContext = new DefaultCamelContext();
                context.setReferenceResolver(new CamelReferenceResolver(camelContext)
                        .withFallback(citrus.getCitrusContext().getReferenceResolver()));
                camelContext.start();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to start default Camel context", e);
            }
        }

        return camelContext;
    }

    private CamelContext springCamelContext(String beans) {
        if (camelContext == null) {
            try {
                ApplicationContext ctx = new GenericXmlApplicationContext(
                        new ByteArrayResource(context.replaceDynamicContentInString(beans).getBytes(StandardCharsets.UTF_8)));
                camelContext = ctx.getBean(SpringCamelContext.class);
                context.setReferenceResolver(new CamelReferenceResolver(camelContext)
                        .withFallback(citrus.getCitrusContext().getReferenceResolver()));
                camelContext.start();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to start Spring Camel context", e);
            }
        }

        return camelContext;
    }

    private void destroyCamelContext() {
        if (globalCamelContext) {
            // do not destroy global Camel context
            return;
        }

        try {
            if (camelContext != null) {
                camelContext.stop();
                camelContext = null;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to stop existing Camel context", e);
        }
    }

    private void verifyNonGlobalContext() {
        if (globalCamelContext) {
            throw new CitrusRuntimeException("Unable to create new Spring Camel context because of active global Camel context. " +
                    "You are not allowed to overwrite global Camel context");
        }
    }
}
