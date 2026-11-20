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
import org.springframework.util.AntPathMatcher;

/**
 * Applies a delegate {@link VertxHttpInterceptor} only to matching request paths, mirroring
 * Spring's {@code MappedInterceptor}: at least one include pattern must match (an empty include
 * list matches every path) and no exclude pattern may match.
 */
public final class VertxMappedInterceptor implements VertxHttpInterceptor {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final List<String> includePatterns;
    private final List<String> excludePatterns;
    private final VertxHttpInterceptor delegate;

    public static VertxMappedInterceptor include(String pattern, VertxHttpInterceptor delegate) {
        return new VertxMappedInterceptor(List.of(pattern), List.of(), delegate);
    }

    public static VertxMappedInterceptor include(List<String> patterns, VertxHttpInterceptor delegate) {
        return new VertxMappedInterceptor(patterns, List.of(), delegate);
    }

    public static VertxMappedInterceptor include(List<String> patterns, List<String> excludePatterns,
                                                 VertxHttpInterceptor delegate) {
        return new VertxMappedInterceptor(patterns, excludePatterns, delegate);
    }

    private VertxMappedInterceptor(List<String> includePatterns, List<String> excludePatterns,
                                   VertxHttpInterceptor delegate) {
        this.includePatterns = List.copyOf(includePatterns);
        this.excludePatterns = List.copyOf(excludePatterns);
        this.delegate = java.util.Objects.requireNonNull(delegate);
    }

    public boolean matches(HttpMessage request) {
        String path = request.getPath();
        boolean included = includePatterns.isEmpty()
                || includePatterns.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
        return included && excludePatterns.stream().noneMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    @Override
    public Message preHandle(HttpMessage request) throws Exception {
        return matches(request) ? delegate.preHandle(request) : null;
    }

    @Override
    public Message postHandle(HttpMessage request, Message reply) throws Exception {
        return matches(request) ? delegate.postHandle(request, reply) : reply;
    }

    @Override
    public void afterCompletion(HttpMessage request, Message reply, Exception error) throws Exception {
        if (matches(request)) {
            delegate.afterCompletion(request, reply, error);
        }
    }
}
