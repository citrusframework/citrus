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

package org.citrusframework.ws.interceptor;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.SmartEndpointInterceptor;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.server.SoapEndpointInterceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Interceptor implementation delegates to list of other endpoint interceptors.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class DelegatingEndpointInterceptor implements SmartEndpointInterceptor, SoapEndpointInterceptor {

    /** List of interceptors to delegate to when this interceptor is invoked */
    private List<EndpointInterceptor> interceptors = new ArrayList<EndpointInterceptor>();

    @Override
    public boolean shouldIntercept(MessageContext messageContext, Object endpoint) {
        // always intercept as this class only delegates to nested interceptors
        return true;
    }

    @Override
    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
        for (EndpointInterceptor interceptor : interceptors) {
            if (shouldIntercept(interceptor, messageContext, endpoint) && !interceptor.handleRequest(messageContext, endpoint)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        for (EndpointInterceptor interceptor : interceptors) {
            if (shouldIntercept(interceptor, messageContext, endpoint) && !interceptor.handleResponse(messageContext, endpoint)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        for (EndpointInterceptor interceptor : interceptors) {
            if (shouldIntercept(interceptor, messageContext, endpoint) && !interceptor.handleFault(messageContext, endpoint)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) throws Exception {
        for (EndpointInterceptor interceptor : interceptors) {
            if (shouldIntercept(interceptor, messageContext, endpoint)) {
                interceptor.afterCompletion(messageContext, endpoint, ex);
            }
        }
    }

    @Override
    public boolean understands(SoapHeaderElement header) {
        for (EndpointInterceptor interceptor : interceptors) {
            if (interceptor instanceof SoapEndpointInterceptor &&
                    ((SoapEndpointInterceptor)interceptor).understands(header)) {
                return true;
            }
        }

        return false;
    }

    private boolean shouldIntercept(EndpointInterceptor interceptor, MessageContext messageContext, Object endpoint) {
        if (interceptor instanceof SmartEndpointInterceptor) {
            return ((SmartEndpointInterceptor) interceptor).shouldIntercept(messageContext, endpoint);
        }

        return true;
    }

    /**
     * Gets the interceptor list.
     * @return
     */
    public List<EndpointInterceptor> getInterceptors() {
        return interceptors;
    }

    /**
     * Sets the interceptor list.
     * @param interceptors
     */
    public void setInterceptors(List<EndpointInterceptor> interceptors) {
        this.interceptors = interceptors;
    }

}
