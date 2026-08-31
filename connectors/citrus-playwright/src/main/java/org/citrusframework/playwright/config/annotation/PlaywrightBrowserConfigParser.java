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
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.endpoint.PlaywrightEndpointBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.StringUtils;

/**
 * Parses the {@link PlaywrightBrowserConfig} annotation into a
 * {@link PlaywrightBrowser} endpoint.
 */
public class PlaywrightBrowserConfigParser implements AnnotationConfigParser<PlaywrightBrowserConfig, PlaywrightBrowser> {

    @Override
    public PlaywrightBrowser parse(PlaywrightBrowserConfig annotation, ReferenceResolver referenceResolver, TestContext context) {
        PlaywrightEndpointBuilder builder = new PlaywrightEndpointBuilder();

        if (StringUtils.hasText(annotation.browserType())) {
            builder.browserType(context.replaceDynamicContentInString(annotation.browserType()));
        }

        builder.headless(annotation.headless());

        if (annotation.slowMo() != 0D) {
            builder.slowMo(annotation.slowMo());
        }

        if (StringUtils.hasText(annotation.channel())) {
            builder.channel(context.replaceDynamicContentInString(annotation.channel()));
        }

        if (StringUtils.hasText(annotation.baseUrl())) {
            builder.baseUrl(context.replaceDynamicContentInString(annotation.baseUrl()));
        }

        if (StringUtils.hasText(annotation.startPageUrl())) {
            builder.startPageUrl(context.replaceDynamicContentInString(annotation.startPageUrl()));
        }

        builder.defaultTimeout(annotation.defaultTimeout());
        builder.defaultNavigationTimeout(annotation.defaultNavigationTimeout());
        builder.tracingEnabled(annotation.tracingEnabled());

        return builder.build();
    }
}
