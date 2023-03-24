/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.http.interceptor;

import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.util.UrlPathHelper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Adapter for {@link org.springframework.web.servlet.handler.MappedInterceptor} conditionally applies interceptor
 * based on request url and path matcher.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MappedInterceptorAdapter implements HandlerInterceptor {

    private final MappedInterceptor mappedInterceptor;
    private final UrlPathHelper urlPathHelper;
    private final PathMatcher pathMatcher;

    /**
     * Default constructor using mapped interceptor, url path helper as well
     * as path matcher instance.
     *
     * @param mappedInterceptor
     * @param urlPathHelper
     * @param pathMatcher
     */
    public MappedInterceptorAdapter(MappedInterceptor mappedInterceptor, UrlPathHelper urlPathHelper, PathMatcher pathMatcher) {
        this.mappedInterceptor = mappedInterceptor;
        this.urlPathHelper = urlPathHelper;
        this.pathMatcher = pathMatcher;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (mappedInterceptor.matches(urlPathHelper.getLookupPathForRequest(request), pathMatcher)) {
            return mappedInterceptor.getInterceptor().preHandle(request, response, handler);
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (mappedInterceptor.matches(urlPathHelper.getLookupPathForRequest(request), pathMatcher)) {
            mappedInterceptor.getInterceptor().postHandle(request, response, handler, modelAndView);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (mappedInterceptor.matches(urlPathHelper.getLookupPathForRequest(request), pathMatcher)) {
            mappedInterceptor.getInterceptor().afterCompletion(request, response, handler, ex);
        }
    }
}
