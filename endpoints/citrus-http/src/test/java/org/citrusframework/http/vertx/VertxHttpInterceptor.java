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

package org.citrusframework.http.vertx;

import java.util.List;

import org.citrusframework.message.Message;
import org.citrusframework.http.message.HttpMessage;

/**
 * Message-world analog of a Spring {@code HandlerInterceptor}. Interceptors run on the exchange
 * worker around handler and Citrus-adapter invocation, after routing, authentication, admission,
 * body limits, gzip decoding, identity and request conversion: {@code preHandle} callbacks run in
 * registration order, {@code postHandle} in reverse order, and {@code afterCompletion} is always
 * invoked in reverse order for every interceptor whose {@code preHandle} ran, including
 * short-circuits and failures.
 *
 * <p>Differences from {@code HandlerInterceptor}: there is no {@code HttpServletRequest},
 * {@code HttpServletResponse}, handler object or {@code ModelAndView}, only the converted
 * {@link HttpMessage}. A short-circuit returns the reply message directly (in Spring MVC the
 * interceptor writes the raw response itself). An {@code afterCompletion} callback that throws
 * propagates instead of being logged, failing the exchange with an empty 500 response.
 */
public interface VertxHttpInterceptor {

    /**
     * Runs before the handler. Return a message to short-circuit with that reply, skipping the
     * handler and every {@code postHandle}; {@code afterCompletion} still runs.
     */
    default Message preHandle(HttpMessage request) throws Exception {
        return null;
    }

    /**
     * Runs after the handler in reverse registration order. May replace the reply.
     */
    default Message postHandle(HttpMessage request, Message reply) throws Exception {
        return reply;
    }

    /**
     * Always runs in reverse registration order for every interceptor whose {@code preHandle}
     * ran. Exactly one of {@code reply} and {@code error} is non-null.
     */
    default void afterCompletion(HttpMessage request, Message reply, Exception error) throws Exception {
    }

    /**
     * Runs the interceptor chain around {@code handler} following Spring's
     * {@code HandlerExecutionChain} ordering: {@code preHandle} in order with short-circuit,
     * {@code postHandle} in reverse, {@code afterCompletion} in reverse on every path.
     */
    static Message invoke(List<VertxHttpInterceptor> interceptors, HttpMessage request,
                          VertxHttpHandler handler) throws Exception {
        int applied = -1;
        try {
            for (int i = 0; i < interceptors.size(); i++) {
                Message shortCircuit = interceptors.get(i).preHandle(request);
                applied = i;
                if (shortCircuit != null) {
                    triggerAfterCompletion(interceptors, applied, request, shortCircuit, null);
                    return shortCircuit;
                }
            }
        } catch (Exception | Error e) {
            try {
                triggerAfterCompletion(interceptors, applied, request, null, asException(e));
            } catch (Exception | Error completionFailure) {
                e.addSuppressed(completionFailure);
            }
            throw e;
        }

        Message reply;
        try {
            reply = handler.handle(request);
        } catch (Exception | Error e) {
            try {
                triggerAfterCompletion(interceptors, applied, request, null, asException(e));
            } catch (Exception | Error completionFailure) {
                e.addSuppressed(completionFailure);
            }
            throw e;
        }

        try {
            Message current = reply;
            for (int i = interceptors.size() - 1; i >= 0; i--) {
                current = interceptors.get(i).postHandle(request, current);
            }
            triggerAfterCompletion(interceptors, applied, request, current, null);
            return current;
        } catch (Exception | Error e) {
            try {
                triggerAfterCompletion(interceptors, applied, request, null, asException(e));
            } catch (Exception | Error completionFailure) {
                e.addSuppressed(completionFailure);
            }
            throw e;
        }
    }

    private static void triggerAfterCompletion(List<VertxHttpInterceptor> interceptors, int applied,
                                               HttpMessage request, Message reply, Exception error)
            throws Exception {
        for (int i = applied; i >= 0; i--) {
            interceptors.get(i).afterCompletion(request, reply, error);
        }
    }

    private static Exception asException(Throwable failure) {
        return failure instanceof Exception exception ? exception : new IllegalStateException(failure);
    }
}
