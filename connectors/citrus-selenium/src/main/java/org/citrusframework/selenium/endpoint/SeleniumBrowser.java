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

import org.citrusframework.api.common.ShutdownPhase;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.messaging.Producer;
import org.citrusframework.selenium.actions.SeleniumAction;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.htmlunit.BrowserVersion;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.events.EventFiringDecorator;
import org.openqa.selenium.support.events.WebDriverListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.htmlunit.BrowserVersion.CHROME;
import static org.htmlunit.BrowserVersion.EDGE;
import static org.htmlunit.BrowserVersion.FIREFOX;
import static org.htmlunit.BrowserVersion.FIREFOX_ESR;

/**
 * Selenium browser provides access to web driver and initializes Selenium environment from endpoint configuration.
 *
 * @since 2.7
 */
public class SeleniumBrowser extends AbstractEndpoint implements Producer, ShutdownPhase {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(SeleniumBrowser.class);

    /** Selenium web driver */
    private WebDriver webDriver;

    /** Temporary storage */
    private final Path temporaryStorage;

    /**
     * Default constructor initializing endpoint configuration.
     */
    public SeleniumBrowser() {
        this(new SeleniumBrowserConfiguration());
    }

    /**
     * Default constructor using endpoint configuration.
     */
    public SeleniumBrowser(SeleniumBrowserConfiguration endpointConfiguration) {
        super(endpointConfiguration);
        temporaryStorage = createTemporaryStorage();
    }

    @Override
    public void send(Message message, TestContext context) {
        SeleniumAction action = message.getPayload(SeleniumAction.class);
        action.execute(context);

        logger.debug("Selenium action successfully executed");
    }

    /**
     * Starts the browser and create local or remote web driver.
     */
    @SuppressWarnings("unchecked")
    public void start() {
        if (!isStarted()) {
            if (getEndpointConfiguration().getWebDriver() != null) {
                webDriver = getEndpointConfiguration().getWebDriver();
            } else if (StringUtils.hasText(getEndpointConfiguration().getRemoteServerUrl())) {
                webDriver = createRemoteWebDriver(getEndpointConfiguration().getBrowserType(), getEndpointConfiguration().getRemoteServerUrl());
            } else {
                webDriver = createLocalWebDriver(getEndpointConfiguration().getBrowserType());
            }

            if (getEndpointConfiguration().getEventListeners() != null &&
                    !getEndpointConfiguration().getEventListeners().isEmpty()) {
                logger.debug("Add event listeners to web driver: {}", getEndpointConfiguration().getEventListeners().size());
                webDriver = new EventFiringDecorator(getEndpointConfiguration().getEventListeners().toArray(new WebDriverListener[0])).decorate(webDriver);
            }

            // Apply window size and timeouts universally: covers custom, remote and local
            // drivers, and runs after event-listener decoration so manage() calls reach the decorated driver.
            applyWindowSize();
            applyTimeouts();
        } else {
            logger.debug("Browser already started");
        }
    }

    /**
     * Stop the browser when started.
     */
    public void stop() {
        if (isStarted()) {
            logger.info("Stopping browser {}", webDriver.getCurrentUrl());

            try {
                logger.debug("Trying to close the browser {} ...", webDriver);
                webDriver.quit();
            } catch (UnreachableBrowserException e) {
                // It happens for Firefox. It's ok: browser is already closed.
                logger.warn("Browser is unreachable", e);
            } catch (WebDriverException e) {
                logger.error("Failed to close browser", e);
            }

            webDriver = null;
        } else {
            logger.warn("Browser already stopped");
        }
    }

    @Override
    public void destroy() {
        stop();
    }

    /**
     * Deploy resource object from resource folder and return path of deployed
     * file
     *
     * @param fileLocation Resource to deploy to temporary storage
     * @return String containing the filename to which the file is uploaded to.
     */
    public String storeFile(String fileLocation) {
        return storeFile(Resources.create(fileLocation));
    }

    /**
     * Deploy resource object from resource folder and return path of deployed
     * file
     *
     * @param file Resource to deploy to temporary storage
     * @return String containing the filename to which the file is uploaded to.
     */
    public String storeFile(Resource file) {
        try {
            File newFile = new File(temporaryStorage.toFile(), FileUtils.getFileName(file.location()));

            logger.debug("Store file {} to {}", file, newFile);

            org.apache.commons.io.FileUtils.copyFile(file.file(), newFile);

            return newFile.getCanonicalPath();
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to store file: " + file, e);
        }
    }

    /**
     * Retrieve resource object
     *
     * @param filename Resource to retrieve.
     * @return String with the path to the resource.
     */
    public String getStoredFile(String filename) {
        try {
            File stored = new File(temporaryStorage.toFile(), filename);

            if (!stored.exists()) {
                throw new CitrusRuntimeException("Failed to access stored file: " + stored.getCanonicalPath());
            }

            return stored.getCanonicalPath();
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to retrieve file: " + filename, e);
        }
    }

    /**
     * Creates local web driver.
     * @param browserType
     * @return
     */
    private WebDriver createLocalWebDriver(String browserType) {
        if (Browser.FIREFOX.is(browserType)) {
            FirefoxProfile firefoxProfile = getEndpointConfiguration().getFirefoxProfile();

            /* set custom download folder */
            firefoxProfile.setPreference("browser.download.dir", temporaryStorage.toFile().getAbsolutePath());

            // NB: firefoxOptions() reuses the same FirefoxProfile instance from the configuration,
            // so the download directory preference set above is preserved.
            FirefoxOptions firefoxOptions = firefoxOptions();
            firefoxOptions.setAcceptInsecureCerts(true);
            return new FirefoxDriver(firefoxOptions);
        } else if (Browser.IE.is(browserType)) {
            return new InternetExplorerDriver();
        } else if (Browser.EDGE.is(browserType)) {
            return new EdgeDriver(edgeOptions());
        } else if (Browser.SAFARI.is(browserType)) {
            warnOnUnsupportedBrowserOptions(browserType);
            return new SafariDriver();
        } else if (Browser.CHROME.is(browserType)) {
            return new ChromeDriver(chromeOptions());
        } else if (Browser.HTMLUNIT.is(browserType)) {
            HtmlUnitDriver htmlUnitDriver = getHtmlUnitDriver();
            return htmlUnitDriver;
        }

        throw new CitrusRuntimeException("Unsupported local browser type: " + browserType);
    }

    /**
     * Creates Chrome options from the endpoint configuration.
     * Applies headless mode and additional browser arguments.
     * @return
     */
    ChromeOptions chromeOptions() {
        ChromeOptions options = new ChromeOptions();
        if (getEndpointConfiguration().isHeadless()) {
            options.addArguments("--headless=new");
        }
        options.addArguments(getEndpointConfiguration().getArguments());
        return options;
    }

    /**
     * Creates Edge options from the endpoint configuration.
     * Applies headless mode and additional browser arguments.
     * @return
     */
    EdgeOptions edgeOptions() {
        EdgeOptions options = new EdgeOptions();
        if (getEndpointConfiguration().isHeadless()) {
            options.addArguments("--headless=new");
        }
        options.addArguments(getEndpointConfiguration().getArguments());
        return options;
    }

    /**
     * Creates Firefox options from the endpoint configuration.
     * Applies headless mode and additional browser arguments.
     * @return
     */
    FirefoxOptions firefoxOptions() {
        FirefoxOptions options = new FirefoxOptions().setProfile(getEndpointConfiguration().getFirefoxProfile());
        if (getEndpointConfiguration().isHeadless()) {
            options.addArguments("-headless");
        }
        options.addArguments(getEndpointConfiguration().getArguments());
        return options;
    }

    /**
     * Logs a warning when headless mode or browser arguments are configured for a browser type
     * that does not support these options. The configured options are ignored in this case.
     * @param browserType
     */
    void warnOnUnsupportedBrowserOptions(String browserType) {
        if (getEndpointConfiguration().isHeadless() || !getEndpointConfiguration().getArguments().isEmpty()) {
            logger.warn("Browser type '{}' does not support headless mode or browser arguments - configured browser options are ignored", browserType);
        }
    }

    /**
     * Applies the configured browser window size to the started web driver.
     * Expects the window size in the form WIDTHxHEIGHT (e.g. 1280x800).
     */
    void applyWindowSize() {
        if (StringUtils.hasText(getEndpointConfiguration().getWindowSize())) {
            String windowSize = getEndpointConfiguration().getWindowSize().trim();
            String[] size = windowSize.split("x", -1);
            if (size.length != 2) {
                throw new CitrusRuntimeException(String.format(
                        "Invalid browser window size '%s' - expected format WIDTHxHEIGHT (e.g. 1280x800)", windowSize));
            }

            try {
                Dimension dimension = new Dimension(Integer.parseInt(size[0].trim()), Integer.parseInt(size[1].trim()));
                logger.debug("Set browser window size to {}", windowSize);
                webDriver.manage().window().setSize(dimension);
            } catch (NumberFormatException e) {
                throw new CitrusRuntimeException(String.format(
                        "Invalid browser window size '%s' - expected format WIDTHxHEIGHT (e.g. 1280x800)", windowSize), e);
            }
        }
    }

    /**
     * Applies the configured page load and script timeouts to the started web driver.
     */
    void applyTimeouts() {
        if (getEndpointConfiguration().getPageLoadTimeout() != null
                && getEndpointConfiguration().getPageLoadTimeout() >= 0L) {
            Duration pageLoadTimeout = Duration.ofMillis(getEndpointConfiguration().getPageLoadTimeout());
            logger.debug("Set browser page load timeout to {}", pageLoadTimeout);
            webDriver.manage().timeouts().pageLoadTimeout(pageLoadTimeout);
        }

        if (getEndpointConfiguration().getScriptTimeout() != null
                && getEndpointConfiguration().getScriptTimeout() >= 0L) {
            Duration scriptTimeout = Duration.ofMillis(getEndpointConfiguration().getScriptTimeout());
            logger.debug("Set browser script timeout to {}", scriptTimeout);
            webDriver.manage().timeouts().scriptTimeout(scriptTimeout);
        }
    }

    private HtmlUnitDriver getHtmlUnitDriver() {
        BrowserVersion browserVersion = htmlUnitBrowserVersion(getEndpointConfiguration().getVersion());

        HtmlUnitDriver htmlUnitDriver;
        if (browserVersion != null) {
            htmlUnitDriver = new HtmlUnitDriver(browserVersion, getEndpointConfiguration().isJavaScript());
        } else {
            htmlUnitDriver = new HtmlUnitDriver(getEndpointConfiguration().isJavaScript());
        }

        return htmlUnitDriver;
    }

    /**
     * Maps the configured browser version to the HtmlUnit browser emulation.
     * @param version the configured browser version
     * @return the HtmlUnit browser version or null to use the HtmlUnit default browser
     */
    static BrowserVersion htmlUnitBrowserVersion(String version) {
        if (version == null) {
            return null;
        }

        return switch (version) {
            case "FIREFOX" -> FIREFOX;
            case "FIREFOX_78", "FIREFOX_ESR" -> FIREFOX_ESR;
            case "CHROME" -> CHROME;
            case "EDGE" -> EDGE;
            case "INTERNET_EXPLORER" -> {
                logger.warn("HtmlUnit browser version '{}' is no longer supported - using HtmlUnit default browser emulation", version);
                yield null;
            }
            default -> null;
        };
    }

    /**
     * Creates remote web driver.
     * @param browserType
     * @param serverAddress
     * @return
     * @throws MalformedURLException
     */
    private RemoteWebDriver createRemoteWebDriver(String browserType, String serverAddress) {
        try {
            MutableCapabilities options;
            if (Browser.FIREFOX.is(browserType)) {
                FirefoxOptions firefoxOptions = firefoxOptions();
                firefoxOptions.setAcceptInsecureCerts(true);
                options = firefoxOptions;
            } else if (Browser.IE.is(browserType)) {
                options = new InternetExplorerOptions();
                options.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
                return new RemoteWebDriver(new URL(serverAddress), options);
            } else if (Browser.EDGE.is(browserType)) {
                options = edgeOptions();
                options.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
                return new RemoteWebDriver(new URL(serverAddress), options);
            } else if (Browser.CHROME.is(browserType)) {
                options = chromeOptions();
                options.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
            } else {
                throw new CitrusRuntimeException("Unsupported remote browser type: " + browserType);
            }

            return new RemoteWebDriver(new URL(serverAddress), options);
        } catch (MalformedURLException e) {
            throw new CitrusRuntimeException("Failed to access remote server", e);
        }
    }

    /**
     * Creates temporary storage.
     * @return
     */
    private Path createTemporaryStorage() {
        try {
            Path tempDir = Files.createTempDirectory("selenium");
            tempDir.toFile().deleteOnExit();

            logger.debug("Download storage location is: {}", tempDir);
            return tempDir;
        } catch (IOException e) {
            throw new CitrusRuntimeException("Could not create temporary storage", e);
        }
    }

    /**
     * Gets the web driver.
     * @return
     */
    public WebDriver getWebDriver() {
        return webDriver;
    }

    /**
     * Sets the webDriver.
     *
     * @param webDriver
     */
    public void setWebDriver(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    /**
     * Gets the started state of the web driver.
     * @return
     */
    public boolean isStarted() {
        return webDriver != null;
    }

    @Override
    public SeleniumBrowserConfiguration getEndpointConfiguration() {
        return (SeleniumBrowserConfiguration) super.getEndpointConfiguration();
    }

    @Override
    public Producer createProducer() {
        return this;
    }

    @Override
    public Consumer createConsumer() {
        throw new UnsupportedOperationException("Selenium browser must not be used as message consumer");
    }
}
