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

package org.citrusframework.cucumber.steps.selenium;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.citrusframework.container.AfterSuite;
import org.citrusframework.container.SequenceAfterSuite;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.adapter.RequestDispatchingEndpointAdapter;
import org.citrusframework.endpoint.adapter.StaticEndpointAdapter;
import org.citrusframework.endpoint.adapter.mapping.HeaderMappingKeyExtractor;
import org.citrusframework.endpoint.adapter.mapping.SimpleMappingStrategy;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.message.Message;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.endpoint.SeleniumBrowserBuilder;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.cucumber.steps.selenium.page.UserFormPage;
import org.openqa.selenium.remote.Browser;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.citrusframework.selenium.actions.SeleniumActionBuilder.selenium;

@Configuration
public class SeleniumConfiguration {

    private static final int HTTP_PORT = 8780;

    @Bean
    public SeleniumBrowser seleniumBrowser() {
        return new SeleniumBrowserBuilder()
                .type(Browser.HTMLUNIT.browserName())
                .build();
    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public UserFormPage userForm() {
        return new UserFormPage();
    }

    @Bean
    public HttpServer webServer() {
        return new HttpServerBuilder()
                .port(HTTP_PORT)
                .autoStart(true)
                .endpointAdapter(templateResponseAdapter())
                .build();
    }

    @Bean
    @DependsOn("seleniumBrowser")
    public AfterSuite afterSuite(SeleniumBrowser browser) {
        return new SequenceAfterSuite() {
            @Override
            public void doExecute(TestContext context) {
                selenium().browser(browser).stop()
                        .build()
                        .execute(context);
            }
        };
    }

    @Bean
    public EndpointAdapter templateResponseAdapter() {
        RequestDispatchingEndpointAdapter dispatchingEndpointAdapter = new RequestDispatchingEndpointAdapter();

        Map<String, EndpointAdapter> mappings = new HashMap<>();

        mappings.put("/", indexPageHandler());
        mappings.put("/form", userFormPageHandler());
        mappings.put("/favicon.ico", faviconHandler());

        SimpleMappingStrategy mappingStrategy = new SimpleMappingStrategy();
        mappingStrategy.setAdapterMappings(mappings);
        dispatchingEndpointAdapter.setMappingStrategy(mappingStrategy);

        dispatchingEndpointAdapter.setMappingKeyExtractor(new HeaderMappingKeyExtractor(HttpMessageHeaders.HTTP_REQUEST_URI));

        return dispatchingEndpointAdapter;
    }

    @Bean
    public EndpointAdapter indexPageHandler() {
        return new StaticEndpointAdapter() {
            @Override
            protected Message handleMessageInternal(Message request) {
                try {
                    return new HttpMessage(FileUtils.readToString(Resources.fromClasspath("templates/index.html")))
                            .contentType(MediaType.TEXT_HTML_VALUE)
                            .status(HttpStatus.OK);
                } catch (IOException e) {
                    return new HttpMessage().status(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        };
    }

    @Bean
    public EndpointAdapter userFormPageHandler() {
        return new StaticEndpointAdapter() {
            @Override
            protected Message handleMessageInternal(Message request) {
                try {
                    return new HttpMessage(FileUtils.readToString(Resources.fromClasspath("templates/form.html")))
                            .contentType(MediaType.TEXT_HTML_VALUE)
                            .status(HttpStatus.OK);
                } catch (IOException e) {
                    return new HttpMessage().status(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        };
    }

    @Bean
    public EndpointAdapter faviconHandler() {
        return new StaticEndpointAdapter() {
            @Override
            protected Message handleMessageInternal(Message request) {
                return new HttpMessage()
                        .status(HttpStatus.OK);
            }
        };
    }
}
