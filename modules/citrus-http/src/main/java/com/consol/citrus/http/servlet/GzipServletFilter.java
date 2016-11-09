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

package com.consol.citrus.http.servlet;

import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter watches for gzip accept header and add gzip compression on response body when applicable. Only
 * applies gzip compression on requests with Accept-Encoding="gzip".
 *
 * @author Christoph Deppisch
 * @since 2.6.2
 */
public class GzipServletFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String acceptEncoding = request.getHeader(HttpHeaders.ACCEPT_ENCODING);
        if (acceptEncoding != null && acceptEncoding.indexOf("gzip") >= 0) {
            GzipHttpServletResponseWrapper gzipResponse = new GzipHttpServletResponseWrapper(response);
            filterChain.doFilter(request, gzipResponse);
            gzipResponse.finish();
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
