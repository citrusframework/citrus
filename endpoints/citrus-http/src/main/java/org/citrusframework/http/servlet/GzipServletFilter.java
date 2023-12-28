/*
 * Copyright 2006-2024 the original author or authors.
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
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter watches for gzip content and accept header in order to add automatic gzip decompression on request
 * and compression on response body when applicable. Only applies gzip request decompression on requests with Content-Encoding="gzip".
 * Only applies gzip response compression on requests with Accept-Encoding="gzip".
 *
 * @author Christoph Deppisch
 * @since 2.6.2
 */
public class GzipServletFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest filteredRequest = request;
        HttpServletResponse filteredResponse = response;

        if (isGzipEncoding(request.getHeader(HttpHeaders.CONTENT_ENCODING))) {
            filteredRequest = new GzipHttpServletRequestWrapper(request);
        }

        if (isGzipEncoding(request.getHeader(HttpHeaders.ACCEPT_ENCODING))) {
            filteredResponse = new GzipHttpServletResponseWrapper(response);
        }

        filterChain.doFilter(filteredRequest, filteredResponse);

        if (filteredResponse instanceof GzipHttpServletResponseWrapper gzipHttpServletResponseWrapper) {
            gzipHttpServletResponseWrapper.finish();
        }
    }

    private boolean isGzipEncoding(String contentEncoding) {
        return contentEncoding != null && contentEncoding.contains("gzip");
    }
}
