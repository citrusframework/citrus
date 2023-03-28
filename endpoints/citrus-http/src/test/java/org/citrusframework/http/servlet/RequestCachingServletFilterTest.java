/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.http.servlet;

import org.citrusframework.http.server.HttpServer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.6.2
 */
public class RequestCachingServletFilterTest {

    private HttpServer httpServer = new HttpServer();
    private CitrusDispatcherServlet servlet;

    @BeforeClass
    public void setUp() throws ServletException {
        servlet = new CitrusDispatcherServlet(httpServer);

        GenericApplicationContext applicationContext = new GenericApplicationContext();
        applicationContext.refresh();

        servlet.init(new MockServletConfig("citrus"));
        servlet.initStrategies(applicationContext);
    }

    @Test
    public void testDoFilterCached() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.POST.name(), "http://localhost:8080/cache");
        request.setContent("Some content".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain(servlet, new RequestCachingServletFilter(), new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                StreamUtils.copy(request.getInputStream(), System.out);
                StreamUtils.copy(request.getInputStream(), System.out);
                StreamUtils.copy(request.getInputStream(), System.out);
            }
        });
        filterChain.doFilter(request, response);
    }

    @Test
    public void testDoFilterGetQueryParamsPost() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.POST.name(), "http://localhost:8080/cache");
        request.setContentType("application/x-www-form-urlencoded");
        request.setContent("username=test&password=secret".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain(servlet, new RequestCachingServletFilter(), new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                StreamUtils.copy(request.getInputStream(), System.out);
                Map<String, String[]> parameters = request.getParameterMap();

                Assert.assertEquals(parameters.size(), 2L);
                Assert.assertEquals(parameters.get("username"), new String[] { "test" });
                Assert.assertEquals(parameters.get("password"), new String[] { "secret" });
            }
        });
        filterChain.doFilter(request, response);
    }

    @Test
    public void testDoFilterGetQueryParamsGet() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.GET.name(), "http://localhost:8080/cache?username=test&password=secret");
        request.setQueryString("username=test&password=secret");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain(servlet, new RequestCachingServletFilter(), new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                StreamUtils.copy(request.getInputStream(), System.out);
                Map<String, String[]> parameters = request.getParameterMap();

                Assert.assertEquals(parameters.size(), 2L);
                Assert.assertEquals(parameters.get("username"), new String[] { "test" });
                Assert.assertEquals(parameters.get("password"), new String[] { "secret" });
            }
        });
        filterChain.doFilter(request, response);
    }

}
