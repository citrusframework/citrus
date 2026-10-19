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

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.Logs;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SeleniumBrowserWindowTimeoutsTest {

    @Test
    public void shouldApplyWindowSize() {
        SeleniumBrowser browser = new SeleniumBrowserBuilder()
                .windowSize("1280x800")
                .build();

        RecordingWebDriver webDriver = new RecordingWebDriver();
        browser.setWebDriver(webDriver);
        browser.applyWindowSize();

        Assert.assertEquals(webDriver.windowSize, new Dimension(1280, 800));
    }

    @Test
    public void shouldApplyPageLoadAndScriptTimeouts() {
        SeleniumBrowser browser = new SeleniumBrowserBuilder()
                .pageLoadTimeout(10000L)
                .scriptTimeout(5000L)
                .build();

        RecordingWebDriver webDriver = new RecordingWebDriver();
        browser.setWebDriver(webDriver);
        browser.applyTimeouts();

        Assert.assertEquals(webDriver.pageLoadTimeout, Duration.ofMillis(10000L));
        Assert.assertEquals(webDriver.scriptTimeout, Duration.ofMillis(5000L));
    }

    @Test
    public void shouldSkipWindowAndTimeoutsByDefault() {
        SeleniumBrowser browser = new SeleniumBrowserBuilder().build();

        RecordingWebDriver webDriver = new RecordingWebDriver();
        browser.setWebDriver(webDriver);
        browser.applyWindowSize();
        browser.applyTimeouts();

        Assert.assertFalse(webDriver.manageCalled);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Invalid browser window size 'invalid'.*")
    public void shouldFailOnInvalidWindowSize() {
        SeleniumBrowser browser = new SeleniumBrowserBuilder()
                .windowSize("invalid")
                .build();

        browser.setWebDriver(new RecordingWebDriver());
        browser.applyWindowSize();
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void shouldFailOnNonNumericWindowSize() {
        SeleniumBrowser browser = new SeleniumBrowserBuilder()
                .windowSize("widextall")
                .build();

        browser.setWebDriver(new RecordingWebDriver());
        browser.applyWindowSize();
    }

    private static final class RecordingWebDriver implements WebDriver {

        private boolean manageCalled;
        private Dimension windowSize;
        private Duration pageLoadTimeout;
        private Duration scriptTimeout;

        private final Options options = new Options() {
            @Override
            public void addCookie(Cookie cookie) {
            }

            @Override
            public void deleteCookieNamed(String name) {
            }

            @Override
            public void deleteCookie(Cookie cookie) {
            }

            @Override
            public void deleteAllCookies() {
            }

            @Override
            public Set<Cookie> getCookies() {
                return Collections.emptySet();
            }

            @Override
            public Cookie getCookieNamed(String name) {
                return null;
            }

            @Override
            public Timeouts timeouts() {
                return new Timeouts() {
                    @Override
                    public Timeouts implicitlyWait(Duration duration) {
                        return this;
                    }

                    @Override
                    public Timeouts pageLoadTimeout(Duration duration) {
                        pageLoadTimeout = duration;
                        return this;
                    }

                    @Override
                    public Timeouts scriptTimeout(Duration duration) {
                        scriptTimeout = duration;
                        return this;
                    }
                };
            }

            @Override
            public Window window() {
                return new Window() {
                    @Override
                    public void setSize(Dimension targetSize) {
                        windowSize = targetSize;
                    }

                    @Override
                    public void setPosition(Point targetPosition) {
                    }

                    @Override
                    public Dimension getSize() {
                        return windowSize;
                    }

                    @Override
                    public Point getPosition() {
                        return null;
                    }

                    @Override
                    public void maximize() {
                    }

                    @Override
                    public void minimize() {
                    }

                    @Override
                    public void fullscreen() {
                    }
                };
            }

            @Override
            public Logs logs() {
                return null;
            }
        };

        @Override
        public void get(String url) {
        }

        @Override
        public String getCurrentUrl() {
            return "";
        }

        @Override
        public String getTitle() {
            return "";
        }

        @Override
        public List<org.openqa.selenium.WebElement> findElements(By by) {
            return Collections.emptyList();
        }

        @Override
        public org.openqa.selenium.WebElement findElement(By by) {
            return null;
        }

        @Override
        public String getPageSource() {
            return "";
        }

        @Override
        public void close() {
        }

        @Override
        public void quit() {
        }

        @Override
        public Set<String> getWindowHandles() {
            return Collections.emptySet();
        }

        @Override
        public String getWindowHandle() {
            return "";
        }

        @Override
        public TargetLocator switchTo() {
            return null;
        }

        @Override
        public Navigation navigate() {
            return null;
        }

        @Override
        public Options manage() {
            manageCalled = true;
            return options;
        }
    }
}
