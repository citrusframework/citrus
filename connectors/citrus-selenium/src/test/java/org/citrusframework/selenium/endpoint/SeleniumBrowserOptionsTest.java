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
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SeleniumBrowserOptionsTest {

    @Test
    public void shouldCreateChromeOptionsWithDefaults() {
        SeleniumBrowser browser = new SeleniumBrowserBuilder()
                .type("chrome")
                .build();

        ChromeOptions options = browser.chromeOptions();

        Assert.assertTrue(chromeArgs(options).isEmpty());
    }

    @Test
    public void shouldCreateChromeOptionsWithHeadlessAndArguments() {
        SeleniumBrowser browser = new SeleniumBrowserBuilder()
                .type("chrome")
                .headless(true)
                .argument("--disable-gpu")
                .argument("--no-sandbox")
                .build();

        ChromeOptions options = browser.chromeOptions();

        Assert.assertEquals(chromeArgs(options), List.of("--headless=new", "--disable-gpu", "--no-sandbox"));
    }

    @Test
    public void shouldCreateEdgeOptionsWithHeadlessAndArguments() {
        SeleniumBrowser browser = new SeleniumBrowserBuilder()
                .type("edge")
                .headless(true)
                .argument("--disable-gpu")
                .build();

        EdgeOptions options = browser.edgeOptions();

        Assert.assertEquals(edgeArgs(options), List.of("--headless=new", "--disable-gpu"));
    }

    @Test
    public void shouldCreateFirefoxOptionsWithHeadlessAndArguments() {
        SeleniumBrowser browser = new SeleniumBrowserBuilder()
                .type("firefox")
                .headless(true)
                .argument("--width=1280")
                .build();

        FirefoxOptions options = browser.firefoxOptions();

        Assert.assertEquals(firefoxArgs(options), List.of("-headless", "--width=1280"));
        Assert.assertNotNull(options.getProfile());
    }

    @Test
    public void shouldWarnOnUnsupportedBrowserOptionsForSafari() {
        SeleniumBrowser browser = new SeleniumBrowserBuilder()
                .type("safari")
                .headless(true)
                .argument("--disable-gpu")
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
            browser.warnOnUnsupportedBrowserOptions("safari");

            List<String> warnings = appender.events.stream()
                    .filter(event -> event.getLevel() == Level.WARN)
                    .map(event -> event.getMessage().getFormattedMessage())
                    .filter(message -> message.contains("safari"))
                    .toList();
            Assert.assertEquals(warnings.size(), 1);
        } finally {
            configuration.removeLogger(SeleniumBrowser.class.getName());
            loggerContext.updateLoggers();
            appender.stop();
        }
    }

    @Test
    public void shouldNotWarnOnDefaultBrowserOptionsForSafari() {
        SeleniumBrowser browser = new SeleniumBrowserBuilder()
                .type("safari")
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
            browser.warnOnUnsupportedBrowserOptions("safari");

            List<LogEvent> warnings = appender.events.stream()
                    .filter(event -> event.getLevel() == Level.WARN)
                    .toList();
            Assert.assertTrue(warnings.isEmpty());
        } finally {
            configuration.removeLogger(SeleniumBrowser.class.getName());
            loggerContext.updateLoggers();
            appender.stop();
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> chromeArgs(ChromeOptions options) {
        return (List<String>) ((Map<String, Object>) options.asMap().get(ChromeOptions.CAPABILITY)).get("args");
    }

    @SuppressWarnings("unchecked")
    private static List<String> edgeArgs(EdgeOptions options) {
        return (List<String>) ((Map<String, Object>) options.asMap().get(EdgeOptions.CAPABILITY)).get("args");
    }

    @SuppressWarnings("unchecked")
    private static List<String> firefoxArgs(FirefoxOptions options) {
        return (List<String>) ((Map<String, Object>) options.asMap().get("moz:firefoxOptions")).get("args");
    }

    private static final class CapturingAppender extends AbstractAppender {

        private final List<LogEvent> events = new CopyOnWriteArrayList<>();

        private CapturingAppender() {
            super("SeleniumBrowserOptionsTestAppender", null, null, true, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent event) {
            events.add(event.toImmutable());
        }
    }
}
