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

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor implementation delegates to list of other handler interceptors.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class DelegatingHandlerInterceptor implements HandlerInterceptor {

    /** List of interceptors to delegate to when this interceptor is invoked */
    private List<HandlerInterceptor> interceptors = new ArrayList<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        for (HandlerInterceptor interceptor : interceptors) {
            if (!interceptor.preHandle(request, response, handler)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        for (HandlerInterceptor interceptor : interceptors) {
            interceptor.postHandle(request, response, handler, modelAndView);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        for (HandlerInterceptor interceptor : interceptors) {
            interceptor.afterCompletion(request, response, handler, ex);
        }
    }

    /**
     * Gets the interceptor list.
     * @return
     */
    public List<HandlerInterceptor> getInterceptors() {
        return interceptors;
    }

    /**
     * Sets the interceptor list.
     * @param interceptors
     */
    public void setInterceptors(List<HandlerInterceptor> interceptors) {
        this.interceptors = interceptors;
    }
}
