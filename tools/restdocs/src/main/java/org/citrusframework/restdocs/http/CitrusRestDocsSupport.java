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

package org.citrusframework.restdocs.http;

import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.generate.RestDocumentationGenerator;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.restdocs.snippet.Snippet;

/**
 * Support class provides static factory methods for RestDoc configurer and interceptor.
 * @author Christoph Deppisch
 * @since 2.6
 */
public class CitrusRestDocsSupport {

    private static final RestDocRequestConverter REQUEST_CONVERTER = new RestDocRequestConverter();
    private static final RestDocResponseConverter RESPONSE_CONVERTER = new RestDocResponseConverter();

    private CitrusRestDocsSupport() {
        super();
    }

    /**
     * Documents the API call with the given {@code identifier} using the given
     * {@code snippets}.
     *
     * @param identifier an identifier for the API call that is being documented
     * @param snippets the snippets that will document the API call
     * @return a {@link RestDocClientInterceptor} that will produce the documentation
     */
    public static RestDocClientInterceptor restDocsInterceptor(String identifier,
                                                   Snippet... snippets) {
        return new RestDocClientInterceptor(new RestDocumentationGenerator<>(identifier,
                REQUEST_CONVERTER, RESPONSE_CONVERTER, snippets));
    }

    /**
     * Documents the API call with the given {@code identifier} using the given
     * {@code snippets} in addition to any default snippets. The given
     * {@code requestPreprocessor} is applied to the request before it is documented.
     *
     * @param identifier an identifier for the API call that is being documented
     * @param requestPreprocessor the request preprocessor
     * @param snippets the snippets
     * @return a {@link RestDocClientInterceptor} that will produce the documentation
     */
    public static RestDocClientInterceptor restDocsInterceptor(String identifier,
                                                   OperationRequestPreprocessor requestPreprocessor, Snippet... snippets) {
        return new RestDocClientInterceptor(new RestDocumentationGenerator<>(identifier,
                REQUEST_CONVERTER, RESPONSE_CONVERTER, requestPreprocessor, snippets));
    }

    /**
     * Documents the API call with the given {@code identifier} using the given
     * {@code snippets} in addition to any default snippets. The given
     * {@code responsePreprocessor} is applied to the request before it is documented.
     *
     * @param identifier an identifier for the API call that is being documented
     * @param responsePreprocessor the response preprocessor
     * @param snippets the snippets
     * @return a {@link RestDocClientInterceptor} that will produce the documentation
     */
    public static RestDocClientInterceptor restDocsInterceptor(String identifier,
                                                   OperationResponsePreprocessor responsePreprocessor, Snippet... snippets) {
        return new RestDocClientInterceptor(new RestDocumentationGenerator<>(identifier,
                REQUEST_CONVERTER, RESPONSE_CONVERTER, responsePreprocessor, snippets));
    }

    /**
     * Documents the API call with the given {@code identifier} using the given
     * {@code snippets} in addition to any default snippets. The given
     * {@code requestPreprocessor} and {@code responsePreprocessor} are applied to the
     * request and response respectively before they are documented.
     *
     * @param identifier an identifier for the API call that is being documented
     * @param requestPreprocessor the request preprocessor
     * @param responsePreprocessor the response preprocessor
     * @param snippets the snippets
     * @return a {@link RestDocClientInterceptor} that will produce the documentation
     */
    public static RestDocClientInterceptor restDocsInterceptor(String identifier,
                                                   OperationRequestPreprocessor requestPreprocessor,
                                                   OperationResponsePreprocessor responsePreprocessor, Snippet... snippets) {
        return new RestDocClientInterceptor(new RestDocumentationGenerator<>(identifier,
                REQUEST_CONVERTER, RESPONSE_CONVERTER, requestPreprocessor,
                responsePreprocessor, snippets));
    }

    /**
     * Provides access to a {@link CitrusRestDocConfigurer} that can be
     * used to configure Spring REST Docs using the given {@code contextProvider}.
     *
     * @param contextProvider the context provider
     * @return the configurer
     */
    public static CitrusRestDocConfigurer restDocsConfigurer(RestDocumentationContextProvider contextProvider) {
        return new CitrusRestDocConfigurer(contextProvider);
    }
}
