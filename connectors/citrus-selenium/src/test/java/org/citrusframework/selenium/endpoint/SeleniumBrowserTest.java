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

package org.citrusframework.selenium.endpoint;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.htmlunit.BrowserVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;


@Isolated("Mutates the shared Log4j LoggerContext configuration")
public class SeleniumBrowserTest {

    static Stream<Arguments> htmlUnitVersions() {
        return Stream.of(
                Arguments.of("FIREFOX", BrowserVersion.FIREFOX),
                Arguments.of("FIREFOX_ESR", BrowserVersion.FIREFOX_ESR),
                Arguments.of("FIREFOX_78", BrowserVersion.FIREFOX_ESR),
                Arguments.of("CHROME", BrowserVersion.CHROME),
                Arguments.of("EDGE", BrowserVersion.EDGE),
                Arguments.of("INTERNET_EXPLORER", null),
                Arguments.of("FIREFOX_38", null),
                Arguments.of("", null),
                Arguments.of(null, null)
        );
    }

    @ParameterizedTest(name = "htmlUnitVersions={0}")
    @MethodSource("htmlUnitVersions")
    public void shouldResolveHtmlUnitBrowserVersion(String version, BrowserVersion expected) {
        Assertions.assertEquals(SeleniumBrowser.htmlUnitBrowserVersion(version), expected);
    }

    @Test
    public void shouldStartHtmlUnitBrowserWithDefaults() {
        SeleniumBrowser browser = new SeleniumBrowser();

        try {
            browser.start();

            HtmlUnitDriver driver = (HtmlUnitDriver) browser.getWebDriver();
            Assertions.assertTrue(driver.getBrowserVersion().isFirefox());
            Assertions.assertFalse(driver.getBrowserVersion().isFirefoxESR());
            Assertions.assertTrue(driver.isJavascriptEnabled());
        } finally {
            browser.stop();
        }
    }

    @Test
    public void shouldStartHtmlUnitBrowserWithVersionAndJavaScriptDisabled() {
        SeleniumBrowser browser = new SeleniumBrowserBuilder()
                .type("htmlunit")
                .version("EDGE")
                .javaScript(false)
                .build();

        try {
            browser.start();

            HtmlUnitDriver driver = (HtmlUnitDriver) browser.getWebDriver();
            Assertions.assertTrue(driver.getBrowserVersion().isEdge());
            Assertions.assertFalse(driver.isJavascriptEnabled());
        } finally {
            browser.stop();
        }
    }

    @Test
    public void shouldFallBackToDefaultBrowserForInternetExplorer() {
        SeleniumBrowser browser = new SeleniumBrowserBuilder()
                .type("htmlunit")
                .version("INTERNET_EXPLORER")
                .build();

        LoggerContext loggerContext = LoggerContext.getContext(false);
        Configuration configuration = loggerContext.getConfiguration();
        LoggerConfig loggerConfig = new LoggerConfig(SeleniumBrowser.class.getName(), Level.WARN, true);
        CapturingAppender appender = new CapturingAppender();
        appender.start();
        loggerConfig.addAppender(appender, Level.WARN, null);
        configuration.addLogger(SeleniumBrowser.class.getName(), loggerConfig);
        loggerContext.updateLoggers();

        try {
            browser.start();

            HtmlUnitDriver driver = (HtmlUnitDriver) browser.getWebDriver();
            Assertions.assertEquals(driver.getBrowserVersion(), BrowserVersion.getDefault());

            List<String> warnings = appender.events.stream()
                    .filter(event -> event.getLevel() == Level.WARN)
                    .map(event -> event.getMessage().getFormattedMessage())
                    .filter(message -> message.contains("INTERNET_EXPLORER"))
                    .toList();
            Assertions.assertEquals(warnings.size(), 1);
        } finally {
            browser.stop();
            configuration.removeLogger(SeleniumBrowser.class.getName());
            loggerContext.updateLoggers();
            appender.stop();
        }
    }

    private static final class CapturingAppender extends AbstractAppender {

        private final List<LogEvent> events = new CopyOnWriteArrayList<>();

        private CapturingAppender() {
            super("SeleniumBrowserTestAppender", null, null, true, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent event) {
            events.add(event.toImmutable());
        }
    }
}
