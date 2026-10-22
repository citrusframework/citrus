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

package org.citrusframework.http.config.xml;

import java.util.Map;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.interceptor.HttpServerInterceptor;
import org.citrusframework.http.interceptor.LoggingServerInterceptor;
import org.citrusframework.http.server.HttpServerSettings;
import org.citrusframework.http.server.vertx.VertxHttpServer;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class VertxHttpServerParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testVertxHttpServerParser() {
        Map<String, VertxHttpServer> servers = beanDefinitionContext.getBeansOfType(VertxHttpServer.class);

        assertEquals(servers.size(), 3);

        // 1st message sender - default values
        VertxHttpServer server = servers.get("vertxHttpServer1");
        assertEquals(server.getName(), "vertxHttpServer1");
        assertEquals(server.getPort(), 8081);
        assertEquals(server.getSecurePort(), 8443);
        assertEquals(server.getContextPath(), "/");
        assertEquals(server.getDefaultTimeout(), 1000L);
        assertFalse(server.isAutoStart());
        assertFalse(server.isDebugLogging());
        assertFalse(server.isHandleCookies());
        assertTrue(server.isRemoveSemicolonPathContent());
        assertTrue(server.isUseDefaultFilters());
        assertEquals(server.getDefaultStatusCode(), HttpStatus.OK.value());
        assertEquals(server.getBinaryMediaTypes().size(), 6L);
        assertEquals(server.getSecuredPath(), "/*");
        assertEquals(server.getWorkerPoolSize(), 200);
        assertNull(server.getAuthentication());
        assertNull(server.getSecureConnection());
        assertNull(server.getVertx());
        assertNotNull(server.getServerInterceptors());
        assertEquals(server.getServerInterceptors().size(), 1L);
        assertTrue(server.getServerInterceptors().get(0) instanceof LoggingServerInterceptor);
        assertEquals(server.getConfiguredInterceptors().size(), 0L);

        // 2nd message sender - all options
        server = servers.get("vertxHttpServer2");
        assertEquals(server.getName(), "vertxHttpServer2");
        assertEquals(server.getPort(), 8082);
        assertEquals(server.getSecurePort(), 8443);
        assertEquals(server.getDefaultTimeout(), 6000L);
        assertEquals(server.getContextPath(), "/citrus");
        assertFalse(server.isUseDefaultFilters());
        assertTrue(server.isHandleCookies());
        assertTrue(server.isDebugLogging());
        assertEquals(server.getDefaultStatusCode(), HttpStatus.NOT_FOUND.value());
        assertEquals(server.getBinaryMediaTypes().size(), 2L);
        assertTrue(server.getBinaryMediaTypes().contains(MediaType.valueOf("application/custom")));
        assertEquals(server.getMessageConverter(), beanDefinitionContext.getBean("messageConverter"));
        assertEquals(server.getAuthentication(), beanDefinitionContext.getBean("authentication"));
        assertEquals(server.getSecuredPath(), "/secured/*");
        assertEquals(server.getSecureConnection(), beanDefinitionContext.getBean("securedConnection"));
        assertEquals(server.getVertx(), beanDefinitionContext.getBean("vertx"));
        assertEquals(server.getWorkerPoolSize(), 50);

        // 3rd message sender - interceptors
        server = servers.get("vertxHttpServer3");
        assertEquals(server.getName(), "vertxHttpServer3");
        assertEquals(server.getPort(), 8083);
        assertNotNull(server.getServerInterceptors());
        assertEquals(server.getServerInterceptors().size(), 2L);
        assertTrue(server.getServerInterceptors().get(0) instanceof LoggingServerInterceptor);
        assertTrue(server.getServerInterceptors().get(1) instanceof HttpServerInterceptor);
        assertNotNull(server.getConfiguredInterceptors());
        assertEquals(server.getConfiguredInterceptors().size(), 0L);
    }

    @Test
    public void testRejectSpringInterceptor() {
        assertThatThrownBy(() -> createApplicationContext("fail-fast"))
                .hasRootCauseInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("org.citrusframework.http.interceptor.HttpServerInterceptor");
    }

    @Test
    public void testUseDefaultFiltersDefault() {
        assertTrue(HttpServerSettings.isUseDefaultFilters());
    }
}
