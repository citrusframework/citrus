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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.citrusframework.http.server.HttpServer;
import org.citrusframework.util.FileUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.6.2
 */
public class GzipServletFilterTest {

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
    public void testDoFilterGzipRequestCompression() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.POST.name(), "http://localhost:8080/gzip");
        request.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
        ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
        GZIPOutputStream zipped = new GZIPOutputStream(contentStream);
        zipped.write("Should be decompressed".getBytes());
        zipped.finish();

        request.setContent(contentStream.toByteArray());

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain(servlet, new GzipServletFilter(), new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                Assert.assertEquals(FileUtils.readToString(request.getInputStream()), "Should be decompressed");
            }
        });
        filterChain.doFilter(request, response);
    }

    @Test
    public void testDoFilterGzipResponseCompression() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.POST.name(), "http://localhost:8080/gzip");
        request.addHeader(HttpHeaders.ACCEPT_ENCODING, "gzip");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain(servlet, new GzipServletFilter(), new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                response.getOutputStream().write("Should be compressed".getBytes());
            }
        });
        filterChain.doFilter(request, response);

        ByteArrayOutputStream unzippedStream = new ByteArrayOutputStream();
        StreamUtils.copy(new GZIPInputStream(new ByteArrayInputStream(response.getContentAsByteArray())), unzippedStream);
        String unzipped = new String(unzippedStream.toByteArray());

        Assert.assertEquals(unzipped, "Should be compressed");
    }

    @Test
    public void testDoFilterNoCompression() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.POST.name(), "http://localhost:8080/gzip");
        request.setContent("Should not be decompressed".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain(servlet, new GzipServletFilter(), new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                response.getOutputStream().write("Should not be compressed".getBytes());
            }
        });
        filterChain.doFilter(request, response);

        Assert.assertEquals(FileUtils.readToString(request.getInputStream()), "Should not be decompressed");
        String unzipped = new String(response.getContentAsByteArray());
        Assert.assertEquals(unzipped, "Should not be compressed");
    }

}
