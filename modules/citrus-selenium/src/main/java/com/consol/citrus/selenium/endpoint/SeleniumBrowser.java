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

package com.consol.citrus.selenium.endpoint;

import com.consol.citrus.endpoint.AbstractEndpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.messaging.Producer;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.*;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class SeleniumBrowser extends AbstractEndpoint {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(SeleniumBrowser.class);

    /** Selenium web driver */
    private WebDriver webDriver;

    /** Temporary storage */
    private Path temporaryStorage;

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

    /**
     * Starts the browser and create local or remote web driver.
     */
    public void start() {
        if (!isStarted()) {
            if (StringUtils.hasText(getEndpointConfiguration().getRemoteServerUrl())) {
                webDriver = createRemoteWebDriver(getEndpointConfiguration().getBrowserType(), getEndpointConfiguration().getRemoteServerUrl());
            } else {
                webDriver = createLocalWebDriver(getEndpointConfiguration().getBrowserType());
            }

            if (!CollectionUtils.isEmpty(getEndpointConfiguration().getEventListeners())) {
                EventFiringWebDriver wrapper = new EventFiringWebDriver(webDriver);
                log.info("Add event listeners to web driver: " + getEndpointConfiguration().getEventListeners().size());
                for (WebDriverEventListener listener : getEndpointConfiguration().getEventListeners()) {
                    wrapper.register(listener);
                }
            }
        } else {
            log.warn("Browser already started");
        }
    }

    /**
     * Stop the browser when started.
     */
    public void stop() {
        if (isStarted()) {
            log.info("Stopping browser " + webDriver.getCurrentUrl());

            try {
                log.info("Trying to close the browser " + webDriver + " ...");
                webDriver.quit();
            } catch (UnreachableBrowserException e) {
                // It happens for Firefox. It's ok: browser is already closed.
                log.warn("Browser is unreachable", e);
            } catch (WebDriverException e) {
                log.error("Failed to close browser", e);
            }

            webDriver = null;
        } else {
            log.warn("Browser already stopped");
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
        try {
            File resourceFile = new File(fileLocation);
            Path dir = temporaryStorage.getFileName();
            File newFile = new File(dir.toFile(), resourceFile.getName());

            log.info("Copy " + resourceFile + " to " + newFile);
            FileUtils.copyFile(resourceFile, newFile);

            return newFile.getCanonicalPath();
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to store file: " + fileLocation, e);
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
            Path dir = temporaryStorage.getFileName();
            File newFile = new File(dir.toFile(), filename);
            return newFile.getCanonicalPath();
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
        switch (browserType) {
            case BrowserType.FIREFOX:
                DesiredCapabilities defaults = DesiredCapabilities.firefox();
                defaults.setCapability(FirefoxDriver.PROFILE, createFireFoxProfile());
                return new FirefoxDriver(defaults);
            case BrowserType.IE:
                return new InternetExplorerDriver();
            case BrowserType.CHROME:
                return new ChromeDriver();
            case BrowserType.GOOGLECHROME:
                return new ChromeDriver();
            case BrowserType.HTMLUNIT:
                BrowserVersion browserVersion = null;
                if (getEndpointConfiguration().getVersion().equals("FIREFOX38")) {
                    browserVersion = BrowserVersion.FIREFOX_38;
                } else if (getEndpointConfiguration().getVersion().equals("INTERNET_EXPLORER_8")) {
                    browserVersion = BrowserVersion.INTERNET_EXPLORER_8;
                } else if (getEndpointConfiguration().getVersion().equals("INTERNET_EXPLORER_11")) {
                    browserVersion = BrowserVersion.INTERNET_EXPLORER_11;
                } else if (getEndpointConfiguration().getVersion().equals("CHROME")) {
                    browserVersion = BrowserVersion.CHROME;
                }

                HtmlUnitDriver htmlUnitDriver;
                if (browserVersion != null) {
                    htmlUnitDriver = new HtmlUnitDriver(browserVersion);
                } else {
                    htmlUnitDriver = new HtmlUnitDriver();
                }
                htmlUnitDriver.setJavascriptEnabled(getEndpointConfiguration().isJavaScript());
                return htmlUnitDriver;
            default:
                throw new CitrusRuntimeException("Unsupported local browser type: " + browserType);
        }
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
            switch (browserType) {
                case BrowserType.FIREFOX:
                    DesiredCapabilities defaultsFF = DesiredCapabilities.firefox();
                    defaultsFF.setCapability(FirefoxDriver.PROFILE, createFireFoxProfile());
                    return new RemoteWebDriver(new URL(serverAddress), defaultsFF);
                case BrowserType.IE:
                    DesiredCapabilities defaultsIE = DesiredCapabilities.internetExplorer();
                    defaultsIE.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
                    return new RemoteWebDriver(new URL(serverAddress), defaultsIE);
                case BrowserType.CHROME:
                    DesiredCapabilities defaultsChrome = DesiredCapabilities.chrome();
                    defaultsChrome.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
                    return new RemoteWebDriver(new URL(serverAddress), defaultsChrome);
                case BrowserType.GOOGLECHROME:
                    DesiredCapabilities defaultsGoogleChrome = DesiredCapabilities.chrome();
                    defaultsGoogleChrome.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
                    return new RemoteWebDriver(new URL(serverAddress), defaultsGoogleChrome);
                default:
                    throw new CitrusRuntimeException("Unsupported remote browser type: " + browserType);
            }
        } catch (MalformedURLException e) {
            throw new CitrusRuntimeException("Failed to access remote server", e);
        }
    }

    /**
     * Create new default Firefox profile.
     * @return
     */
    private FirefoxProfile createFireFoxProfile() {
        FirefoxProfile fp = new FirefoxProfile();

        fp.setAcceptUntrustedCertificates(true);
        fp.setAssumeUntrustedCertificateIssuer(false);

        /* set custom download folder */
        fp.setPreference("browser.download.dir", temporaryStorage.toFile().getAbsolutePath());

        /* default download folder, set to 2 to use custom download folder */
        fp.setPreference("browser.download.folderList", 2);

        /* comma separated list if MIME types to save without asking */
        fp.setPreference("browser.helperApps.neverAsk.saveToDisk", "text/plain");

        /* do not show download manager */
        fp.setPreference("browser.download.manager.showWhenStarting", false);

        fp.setEnableNativeEvents(true);

        return fp;
    }

    /**
     * Creates temporary storage.
     * @return
     */
    private Path createTemporaryStorage() {
        try {
            Path tempDir = Files.createTempDirectory("selenium");
            tempDir.toFile().deleteOnExit();

            log.info("Download storage location is: " + tempDir.toString());
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
        return null;
    }

    @Override
    public Consumer createConsumer() {
        return null;
    }
}
