/*
 * Copyright 2006-2017 the original author or authors.
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

package org.citrusframework.selenium.config.annotation;

import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.endpoint.SeleniumBrowserBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.events.WebDriverListener;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class SeleniumBrowserConfigParser implements AnnotationConfigParser<SeleniumBrowserConfig, SeleniumBrowser> {

    @Override
    public SeleniumBrowser parse(SeleniumBrowserConfig annotation, ReferenceResolver referenceResolver) {
        SeleniumBrowserBuilder builder = new SeleniumBrowserBuilder();

        if (StringUtils.hasText(annotation.startPage())) {
            builder.startPage(annotation.startPage());
        }

        if (StringUtils.hasText(annotation.version())) {
            builder.version(annotation.version());
        }

        if (StringUtils.hasText(annotation.remoteServer())) {
            builder.remoteServer(annotation.remoteServer());
        }

        if (StringUtils.hasText(annotation.type())) {
            builder.type(annotation.type());
        }

        if (StringUtils.hasText(annotation.webDriver())) {
            builder.webDriver(referenceResolver.resolve(annotation.webDriver(), WebDriver.class));
        }

        if (StringUtils.hasText(annotation.firefoxProfile())) {
            builder.profile(referenceResolver.resolve(annotation.firefoxProfile(), FirefoxProfile.class));
        }

        builder.eventListeners(referenceResolver.resolve(annotation.eventListeners(), WebDriverListener.class));

        builder.javaScript(annotation.javaScript());

        builder.timeout(annotation.timeout());

        return builder.build();
    }
}
