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

package org.citrusframework.http.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.MappedInterceptor;

/**
 * Adapter for {@link org.springframework.web.servlet.handler.MappedInterceptor} conditionally applies interceptor
 * based on request url and path matcher.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MappedInterceptorAdapter implements HandlerInterceptor {

    private final MappedInterceptor mappedInterceptor;

    /**
     * Default constructor using mapped interceptor, url path helper as well
     * as path matcher instance.
     *
     * @param mappedInterceptor
     */
    public MappedInterceptorAdapter(MappedInterceptor mappedInterceptor) {
        this.mappedInterceptor = mappedInterceptor;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (mappedInterceptor.matches(request)) {
            return mappedInterceptor.getInterceptor().preHandle(request, response, handler);
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (mappedInterceptor.matches(request)) {
            mappedInterceptor.getInterceptor().postHandle(request, response, handler, modelAndView);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (mappedInterceptor.matches(request)) {
            mappedInterceptor.getInterceptor().afterCompletion(request, response, handler, ex);
        }
    }
}
