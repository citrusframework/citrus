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

package org.citrusframework.http.config.annotation;

import io.vertx.core.Vertx;
import java.util.Arrays;
import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.StaticEndpoint;
import org.citrusframework.http.interceptor.HttpServerInterceptor;
import org.citrusframework.http.message.HttpMessageConverter;
import org.citrusframework.http.security.HttpAuthentication;
import org.citrusframework.http.security.HttpSecureConnection;
import org.citrusframework.http.server.vertx.VertxHttpServer;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.citrusframework.config.annotation.AnnotationConfigParser.lookup;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class VertxHttpServerConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint(name = "vertxHttpServer1")
    @VertxHttpServerConfig(autoStart = false,
            port = 8091)
    private VertxHttpServer vertxHttpServer1;

    @CitrusEndpoint
    @VertxHttpServerConfig(autoStart = false,
            port = 8092,
            contextPath = "/citrus",
            messageConverter = "messageConverter",
            handleCookies = true,
            removeSemicolonPathContent = false,
            debugLogging = true,
            binaryMediaTypes = {MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/custom"},
            defaultStatus = HttpStatus.NOT_FOUND,
            workerPoolSize = 100)
    private VertxHttpServer vertxHttpServer2;

    @CitrusEndpoint
    @VertxHttpServerConfig(autoStart = false,
            port = 8093,
            endpointAdapter = "endpointAdapter",
            actor = "testActor")
    private VertxHttpServer vertxHttpServer3;

    @CitrusEndpoint
    @VertxHttpServerConfig(autoStart = false,
            port = 8094,
            interceptors = {"citrusInterceptor1"})
    private VertxHttpServer vertxHttpServer4;

    @CitrusEndpoint
    @VertxHttpServerConfig(autoStart = false,
            port = 8095,
            authentication = "authentication",
            securedPath = "/secured/*")
    private VertxHttpServer vertxHttpServer5;

    @CitrusEndpoint
    @VertxHttpServerConfig(autoStart = false,
            port = 8096,
            secured = "secured",
            securePort = 8444)
    private VertxHttpServer vertxHttpServer6;

    @CitrusEndpoint
    @VertxHttpServerConfig(autoStart = false,
            port = 8097,
            vertx = "vertx")
    private VertxHttpServer vertxHttpServer7;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private HttpMessageConverter messageConverter;
    @Mock
    private EndpointAdapter endpointAdapter;
    @Mock
    private HttpServerInterceptor citrusInterceptor1;
    @Mock
    private TestActor testActor;
    @Mock
    private Vertx vertx;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("messageConverter", HttpMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("endpointAdapter", EndpointAdapter.class)).thenReturn(endpointAdapter);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
        when(referenceResolver.resolve("authentication", HttpAuthentication.class)).thenReturn(HttpAuthentication.basic("citrus", "secr3t"));
        when(referenceResolver.resolve("secured", HttpSecureConnection.class)).thenReturn(HttpSecureConnection.ssl());
        when(referenceResolver.resolve("vertx", Vertx.class)).thenReturn(vertx);
        when(referenceResolver.resolve(new String[]{"citrusInterceptor1"}, Object.class)).thenReturn(Arrays.asList(citrusInterceptor1));

        when(endpointAdapter.getEndpoint()).thenReturn(new StaticEndpoint());
        when(endpointAdapter.getEndpointConfiguration()).thenReturn(new StaticEndpoint.StaticEndpointConfiguration());
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testVertxHttpServerParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // default configuration
        assertEquals(vertxHttpServer1.getName(), "vertxHttpServer1");
        assertEquals(vertxHttpServer1.getPort(), 8091);
        assertEquals(vertxHttpServer1.getSecurePort(), 8443);
        assertEquals(vertxHttpServer1.getContextPath(), "/");
        assertFalse(vertxHttpServer1.isHandleCookies());
        assertTrue(vertxHttpServer1.isRemoveSemicolonPathContent());
        assertTrue(vertxHttpServer1.isUseDefaultFilters());
        assertFalse(vertxHttpServer1.isAutoStart());
        assertFalse(vertxHttpServer1.isDebugLogging());
        assertEquals(vertxHttpServer1.getDefaultStatusCode(), HttpStatus.OK.value());
        assertEquals(vertxHttpServer1.getBinaryMediaTypes().size(), 6L);
        assertEquals(vertxHttpServer1.getWorkerPoolSize(), 200);
        assertNull(vertxHttpServer1.getAuthentication());
        assertNull(vertxHttpServer1.getSecureConnection());
        assertNull(vertxHttpServer1.getVertx());

        // custom configuration
        assertEquals(vertxHttpServer2.getPort(), 8092);
        assertEquals(vertxHttpServer2.getContextPath(), "/citrus");
        assertEquals(vertxHttpServer2.getMessageConverter(), messageConverter);
        assertTrue(vertxHttpServer2.isHandleCookies());
        assertFalse(vertxHttpServer2.isRemoveSemicolonPathContent());
        assertTrue(vertxHttpServer2.isDebugLogging());
        assertEquals(vertxHttpServer2.getDefaultStatusCode(), HttpStatus.NOT_FOUND.value());
        assertEquals(vertxHttpServer2.getBinaryMediaTypes().size(), 2L);
        assertTrue(vertxHttpServer2.getBinaryMediaTypes().contains(MediaType.valueOf("application/custom")));
        assertEquals(vertxHttpServer2.getWorkerPoolSize(), 100);

        // endpoint adapter and test actor
        assertNotNull(vertxHttpServer3.getEndpointAdapter());
        assertEquals(vertxHttpServer3.getEndpointAdapter(), endpointAdapter);
        assertEquals(vertxHttpServer3.getActor(), testActor);

        // server interceptors - the default logging interceptor comes first
        assertEquals(vertxHttpServer4.getServerInterceptors().size(), 2L);
        assertEquals(vertxHttpServer4.getServerInterceptors().get(1), citrusInterceptor1);

        // authentication
        assertNotNull(vertxHttpServer5.getAuthentication());
        assertEquals(vertxHttpServer5.getSecuredPath(), "/secured/*");

        // secure connection
        assertNotNull(vertxHttpServer6.getSecureConnection());
        assertEquals(vertxHttpServer6.getSecurePort(), 8444);

        // supplied Vert.x instance
        assertEquals(vertxHttpServer7.getVertx(), vertx);
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser<?, ?>> validators = lookup();
        assertNotNull(validators.get("http.vertx-server"));
        assertEquals(validators.get("http.vertx-server").getClass(), VertxHttpServerConfigParser.class);
    }

    @Test
    public void testLookupByQualifier() {
        assertTrue(lookup("http.vertx-server").isPresent());
    }
}
