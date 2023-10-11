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

package org.citrusframework.selenium.endpoint;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import com.gargoylesoftware.htmlunit.BrowserVersion;
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

/**
 * Selenium browser provides access to web driver and initializes Selenium environment from endpoint configuration.
 *
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class SeleniumBrowser extends AbstractEndpoint implements Producer {

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
     *
     * @param endpointConfiguration
     */
    public SeleniumBrowser(SeleniumBrowserConfiguration endpointConfiguration) {
        super(endpointConfiguration);
        temporaryStorage = createTemporaryStorage();
    }

    @Override
    public void send(Message message, TestContext context) {
        SeleniumAction action = message.getPayload(SeleniumAction.class);
        action.execute(context);

        logger.info("Selenium action successfully executed");
    }

    /**
     * Starts the browser and create local or remote web driver.
     */
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
                logger.info("Add event listeners to web driver: " + getEndpointConfiguration().getEventListeners().size());
                webDriver = new EventFiringDecorator(getEndpointConfiguration().getEventListeners().toArray(new WebDriverListener[0])).decorate(webDriver);
            }
        } else {
            logger.debug("Browser already started");
        }
    }

    /**
     * Stop the browser when started.
     */
    public void stop() {
        if (isStarted()) {
            logger.info("Stopping browser " + webDriver.getCurrentUrl());

            try {
                logger.info("Trying to close the browser " + webDriver + " ...");
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
            File newFile = new File(temporaryStorage.toFile(), FileUtils.getFileName(file.getLocation()));

            logger.info("Store file " + file + " to " + newFile);

            org.apache.commons.io.FileUtils.copyFile(file.getFile(), newFile);

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

            return new FirefoxDriver(new FirefoxOptions().setProfile(firefoxProfile));
        } else if (Browser.IE.is(browserType)) {
            return new InternetExplorerDriver();
        } else if (Browser.EDGE.is(browserType)) {
            return new EdgeDriver();
        } else if (Browser.SAFARI.is(browserType)) {
            return new SafariDriver();
        } else if (Browser.CHROME.is(browserType)) {
            return new ChromeDriver();
        } else if (Browser.HTMLUNIT.is(browserType)) {
            BrowserVersion browserVersion = null;
            switch (getEndpointConfiguration().getVersion()) {
                case "FIREFOX":
                    browserVersion = BrowserVersion.FIREFOX;
                    break;
                case "FIREFOX_78":
                case "FIREFOX_ESR":
                    browserVersion = BrowserVersion.FIREFOX_ESR;
                    break;
                case "INTERNET_EXPLORER":
                    browserVersion = BrowserVersion.INTERNET_EXPLORER;
                    break;
                case "CHROME":
                    browserVersion = BrowserVersion.CHROME;
                    break;
            }

            HtmlUnitDriver htmlUnitDriver;
            if (browserVersion != null) {
                htmlUnitDriver = new HtmlUnitDriver(browserVersion, getEndpointConfiguration().isJavaScript());
            } else {
                htmlUnitDriver = new HtmlUnitDriver(getEndpointConfiguration().isJavaScript());
            }
            return htmlUnitDriver;
        }

        throw new CitrusRuntimeException("Unsupported local browser type: " + browserType);
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
                options = new FirefoxOptions().setProfile(getEndpointConfiguration().getFirefoxProfile());
            } else if (Browser.IE.is(browserType)) {
                options = new InternetExplorerOptions();
                options.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
                return new RemoteWebDriver(new URL(serverAddress), options);
            } else if (Browser.EDGE.is(browserType)) {
                options = new EdgeOptions();
                options.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
                return new RemoteWebDriver(new URL(serverAddress), options);
            } else if (Browser.CHROME.is(browserType)) {
                options = new ChromeOptions();
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

            logger.info("Download storage location is: " + tempDir);
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
