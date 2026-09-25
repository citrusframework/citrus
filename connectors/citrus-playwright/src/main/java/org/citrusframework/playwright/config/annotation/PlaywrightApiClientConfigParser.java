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

package org.citrusframework.playwright.config.annotation;

import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.context.TestContext;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.playwright.http.PlaywrightApiClientBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.StringUtils;

public class PlaywrightApiClientConfigParser implements AnnotationConfigParser<PlaywrightApiClientConfig, HttpClient> {

    @Override
    public HttpClient parse(PlaywrightApiClientConfig annotation, ReferenceResolver referenceResolver, TestContext context) {
        PlaywrightApiClientBuilder builder = new PlaywrightApiClientBuilder()
                .browser(context.replaceDynamicContentInString(annotation.browser()));

        if (StringUtils.hasText(annotation.context())) {
            builder.context(context.replaceDynamicContentInString(annotation.context()));
        }

        if (StringUtils.hasText(annotation.requestUrl())) {
            builder.requestUrl(context.replaceDynamicContentInString(annotation.requestUrl()));
        }

        if (annotation.timeout() >= 0) {
            builder.timeout(annotation.timeout());
        }

        if (annotation.maxRedirects() >= 0) {
            builder.maxRedirects(annotation.maxRedirects());
        }

        if (annotation.maxRetries() >= 0) {
            builder.maxRetries(annotation.maxRetries());
        }

        builder.ignoreHttpsErrors(annotation.ignoreHttpsErrors());
        builder.handleCookies(annotation.handleCookies());
        builder.referenceResolver(referenceResolver);

        return builder.build();
    }
}
