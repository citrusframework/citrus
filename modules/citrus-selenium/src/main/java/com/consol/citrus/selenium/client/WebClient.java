/*
 * Copyright 2016 the original author or authors.
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
package com.consol.citrus.selenium.client;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.model.config.selenium.BrowserTypeEnum;
import com.consol.citrus.model.testcase.selenium.ByEnum;
import com.consol.citrus.model.testcase.selenium.ValidationRuleEnum;
import com.consol.citrus.selenium.model.WebPage;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import static java.util.Collections.emptyList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 *
 * @author Tamer Erdogan
 */
public class WebClient implements WebDriverContainer {

	private BrowserTypeEnum browserType;
	private WebClientConfiguration clientConfiguration;
	private final List<WebDriverEventListener> listeners = new ArrayList<>();
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private Path temporaryStorage;
	private WebDriver webDriver;
	private String lastWindow;

	/**
	 * Default constructor using client configuration.
	 *
	 * @param clientConfiguration
	 */
	public WebClient(WebClientConfiguration clientConfiguration) {
		this.clientConfiguration = clientConfiguration;

		if (clientConfiguration.getBrowserType() == null) {
			throw new CitrusRuntimeException("Browser type is not defined. Please configure the browser which you want to use.");
		} else {
			browserType = clientConfiguration.getBrowserType();
		}

		temporaryStorage = createTemporaryStorage();
	}

	/**
	 * Default constructor initializing client configuration.
	 */
	public WebClient() {
		this(new WebClientConfiguration());
	}

	public String acceptAlertBox() {
		String message = null;
		try {
			Alert alert = webDriver.switchTo().alert();
			message = alert.getText();
			alert.accept();
			System.out.println("message" + message);
		} catch (Exception e) {
			message = null;
		}
		return message;
	}

	@Override
	public void addListener(WebDriverEventListener listener) {
		this.listeners.add(listener);

		EventFiringWebDriver wrapper = new EventFiringWebDriver(webDriver);
		logger.info("Add listener to webdriver: " + listener);
		wrapper.register(listener);
	}

	public String cancelAlertBox() {
		String message = null;
		try {
			Alert alert = webDriver.switchTo().alert();
			message = alert.getText();
			alert.dismiss();
		} catch (Exception e) {
			message = null;
		}
		return message;
	}

	/**
	 * Enable or disable a check button.
	 *
	 * @param by
	 * @param str "true" or "false".
	 */
	public void checkItem(By by, String str) {
		boolean value = Boolean.parseBoolean(str);
		WebElement element = findElement(by);
		if (element.isSelected() && !value) {
			element.click();
		} else if (value && !element.isSelected()) {
			element.click();
		}
	}

	@Override
	public void clearBrowserCache() {
		webDriver.manage().deleteAllCookies();
	}

	/**
	 * Click on a single element identified by By.
	 *
	 * @param by The by value.
	 */
	public void click(By by) {
		findElement(by).click();
	}

	/**
	 * Click on a single element identified by its id.
	 *
	 * @param id The ID.
	 */
	public void clickById(String id) {
		findElement(By.id(id)).click();
	}

	/**
	 * Click on a single element indicated by given CSS selector.
	 *
	 * @param selector The selector.
	 */
	public void clickBySelector(String selector) {
		findElement(By.cssSelector(selector)).click();
	}

	@Override
	public void closeWebDriver() {
		try {
			logger.info("Trying to close the browser " + webDriver + " ...");
			webDriver.quit();
		} catch (UnreachableBrowserException e) {
			// It happens for Firefox. It's ok: browser is already closed.
			logger.info("Browser is unreachable", e);
		} catch (WebDriverException cannotCloseBrowser) {
			logger.error("Cannot close browser normally: " + cannotCloseBrowser);
		}
	}

	public boolean closeWindow(String windowHandleToClose) {
		Set<String> windowHandles = webDriver.getWindowHandles();
		String windowHandleToRemain = null;

		logger.info("Current window: " + webDriver.getWindowHandle());
		logger.info("Window to close: " + windowHandleToClose);
		for (String wh : windowHandles) {
			if (!wh.equals(windowHandleToClose)) {
				windowHandleToRemain = wh;
			}
		}
		logger.info("Window to remain: " + windowHandleToRemain);
		if (webDriver.getWindowHandle().equals((windowHandleToClose))) {
			webDriver.close();
			logger.info("switched back to main window!");
			webDriver.switchTo().window(windowHandleToRemain);
			return true;
		} else {
			logger.error("The popup is not closed!!!");
		}

		return false;
	}

	/**
	 * Create a Page Object instance.
	 *
	 * @param <T>
	 * @param pageObject
	 * @return
	 * @see PageFactory#initElements(WebDriver, Class)
	 */
	public <T extends WebPage> WebPage createPage(T pageObject) {
		PageFactory.initElements(webDriver, pageObject);
		return pageObject;
	}

	/**
	 * Constructor.
	 *
	 * @param <T>
	 * @param pageClass
	 * @return
	 */
	public <T extends WebPage> WebPage createPage(Class<T> pageClass) {
		T webPage = PageFactory.initElements(webDriver, pageClass);
		return webPage;
	}

	/**
	 * Delete all cookies.
	 */
	public void deleteAllCookies() {
		webDriver.manage().deleteAllCookies();
	}

	@SuppressWarnings("unchecked")
	public <T> T executeJavaScript(String jsCode, Object... arguments) {
		return (T) ((JavascriptExecutor) webDriver).executeScript(jsCode, arguments);
	}

	/**
	 * Get a specific element from the loaded webpage.
	 *
	 * @param by Element identifier method.
	 * @return WebElement.
	 */
	public WebElement findElement(By by) {
		WebElement element = null;
		try {
			element = webDriver.findElement(by);
		} catch (Exception ex) {

		}
		return element;
	}

	/**
	 * Get all elements matched by By.
	 *
	 * @param by By.
	 * @return List of WebElements.
	 */
	public List<WebElement> findElements(By by) {
		return webDriver.findElements(by);
	}

	/**
	 * Get the alert dialogs on the page.
	 *
	 * @return List of WebElements.
	 */
	public Alert getAlert() {
		return webDriver.switchTo().alert();
	}

	/**
	 * @return The name of the browser which is in use.
	 */
	public BrowserTypeEnum getBrowserType() {
		return browserType;
	}

	public static By getByFromEnum(ByEnum byEnum, String select) {
		By by = null;
		switch (byEnum) {
			case ID:
				by = By.id(select);
				break;
			case CLASS_NAME:
				by = By.className(select);
				break;
			case LINK_TEXT:
				by = By.linkText(select);
				break;
			case CSS_SELECTOR:
				by = By.cssSelector(select);
				break;
			case NAME:
				by = By.name(select);
				break;
			case TAG_NAME:
				by = By.tagName(select);
				break;
			case XPATH:
				by = By.xpath(select);
				break;
		}
		return by;
	}

	@Override
	public String getCurrentFrameUrl() {
		return ((JavascriptExecutor) webDriver).executeScript("return window.location.href").toString();
	}

	@Override
	public String getCurrentUrl() {
		return webDriver.getCurrentUrl();
	}

	/**
	 * Get hostname.
	 *
	 * @return Current hostname.
	 */
	public String getHostname() {
		URL currentURL = null;
		try {
			currentURL = new URL(webDriver.getCurrentUrl());
			return currentURL.getHost();
		} catch (MalformedURLException e) {
			logger.debug("Not a valid URL url to resolve hostname from: " + webDriver.getCurrentUrl(), e);
		}

		return null;
	}

	/**
	 * Get JavaScript errors that happened on this page.
	 *
	 * Format can differ from browser to browser: - Uncaught ReferenceError: $
	 * is not defined at http://localhost:35070/page_with_js_errors.html:8 -
	 * ReferenceError: Can't find variable: $ at
	 * http://localhost:8815/page_with_js_errors.html:8
	 *
	 * Function returns nothing if the page has its own "window.onerror"
	 * handler.
	 *
	 * @return list of error messages. Returns empty list if webdriver is not
	 * started properly.
	 */
	public List<String> getJavascriptErrors() {
		try {
			List<Object> errors = executeJavaScript("return window._selenide_jsErrors");
			if (errors == null || errors.isEmpty()) {
				return emptyList();
			}
			List<String> result = new ArrayList<>(errors.size());
			for (Object error : errors) {
				result.add(error.toString());
			}
			return result;
		} catch (WebDriverException cannotExecuteJs) {
			logger.error(cannotExecuteJs.toString());
			return emptyList();
		}
	}

	public String getModelNamespace() {
		return clientConfiguration.getModelNamespace();
	}

	@Override
	public String getPageSource() {
		return webDriver.getPageSource();
	}

	/**
	 * Get the page title.
	 *
	 * @return The page title.
	 */
	public String getPageTitle() {
		return webDriver.getTitle();
	}

	/**
	 * Get path.
	 *
	 * @return Current path.
	 */
	public String getPath() {
		URL currentURL = null;
		try {
			currentURL = new URL(webDriver.getCurrentUrl());
			return currentURL.getPath();
		} catch (MalformedURLException e) {
			logger.debug("Not a valid URL url to resolve hostname from: " + webDriver.getCurrentUrl(), e);
		}

		return null;
	}

	public String getStartUrl() {
		return clientConfiguration.getStartUrl();
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
			logger.warn("Could not retrieve file: " + filename, e);
		}
		return null;
	}

	/**
	 * @return The WebDriver instance.
	 */
	@Override
	public WebDriver getWebDriver() {
		return webDriver;
	}

	@Override
	public boolean hasWebDriverStarted() {
		return webDriver != null;
	}

	public void navigateBack() {
		webDriver.navigate().back();
	}

	public void navigateForward() {
		webDriver.navigate().forward();
	}

	public void navigateTo(String pageUrl) {
		logger.info(pageUrl);
		try {
			if (browserType.equals(BrowserTypeEnum.INTERNET_EXPLORER)) {
				pageUrl = makeUniqueUrlToAvoidIECaching(pageUrl, new Date().getTime());
			}
			URL url = new URL(pageUrl);
			webDriver.navigate().to(url);
		} catch (MalformedURLException ex) {
			String baseUrl = webDriver.getCurrentUrl();
			try {
				URL currentURL = new URL(baseUrl);
			} catch (MalformedURLException e) {
				if (StringUtils.hasText(clientConfiguration.getStartUrl())) {
					baseUrl = clientConfiguration.getStartUrl();
				} else {
					logger.debug("Not a valid URL url to resolve hostname from: " + webDriver.getCurrentUrl(), ex);
					throw new CitrusRuntimeException(ex);
				}
			}
			String lastChar = baseUrl.substring(baseUrl.length() - 1);
			if (!lastChar.equals("/")) {
				baseUrl = baseUrl + "/";
			}
			webDriver.navigate().to(baseUrl + pageUrl);
		}
	}

	/**
	 * Wait until an item is loaded.
	 *
	 * @param sec Timeout
	 * @return true when the element is invisible.
	 */
	public String openNewWindow() {
		Set<String> windowHandles = webDriver.getWindowHandles();
		String popupWindowHandle = null;
		String mainWindowHandle = webDriver.getWindowHandle();

		logger.info("Main window handle: " + mainWindowHandle);
		//_logger.info("Window handles found: ");
		for (String wh : windowHandles) {
			//_logger.info(wh);
			if (!wh.contentEquals(mainWindowHandle)) {
				popupWindowHandle = wh;
				logger.info("Popup window handle: " + popupWindowHandle);
			}
		}
		if (org.apache.commons.lang.StringUtils.isNotBlank(popupWindowHandle)) {
			webDriver.switchTo().window(popupWindowHandle);
			logger.info("switched to popup window!");
		}
		return popupWindowHandle;
	}

	public void gotoPopup() {
		Set<String> handles = webDriver.getWindowHandles();

		lastWindow = webDriver.getWindowHandle();

		Iterator<String> iterator = handles.iterator();
		while (iterator.hasNext()) {
			String subWindowHandler = iterator.next();
			if (subWindowHandler != null) {
				webDriver.switchTo().window(subWindowHandler);
				logger.info("Switch window focus to popup");
			}
		}
	}

	public void returnFromPopup() {
		if (lastWindow != null) {
			webDriver.switchTo().window(lastWindow);
			lastWindow = null;
		} else {
			webDriver.switchTo().defaultContent();
		}
	}

	public void refresh() {
		webDriver.navigate().refresh();
	}

	/**
	 * Select an item from a pulldown list.
	 *
	 * @param by
	 * @param item The value of the item.
	 */
	public void selectItem(By by, String item) {
		Select dropdown = new Select(findElement(by));
		dropdown.selectByValue(item);
	}

	/**
	 * Select multiple items from a pull-down list.
	 *
	 * @param by
	 * @param from Start at index.
	 * @param to End at index.
	 */
	public void selectMultipleItems(By by, int from, int to) {
		Select dropdown = new Select(findElement(by));

		if (BrowserTypeEnum.INTERNET_EXPLORER.equals(browserType)) {
			for (int i = from; i < to; i++) {
				dropdown.selectByIndex(i);
			}
		} else {
			//Rest:
			List<WebElement> options = dropdown.getOptions();
			Actions builder = new Actions(webDriver);
			builder.keyDown(Keys.CONTROL);
			for (int i = from; i < to; i++) {
				WebElement item = options.get(i);
				if (!item.isSelected()) {
					builder.moveToElement(item).click(item);
				}
			}
			builder.keyUp(Keys.CONTROL);
			Action multiple = builder.build();
			multiple.perform();
		}
	}

	/**
	 * Set a value on a single input element by its id after clearing it.
	 *
	 * @param by
	 * @param value Value to set.
	 */
	public void setInput(By by, String value) {
		WebElement element = findElement(by);
		String tagName = element.getTagName();
		if (null == tagName || !"select".equals(tagName.toLowerCase())) {
			element.clear();
			element.sendKeys(value);
		} else {
			new Select(element).selectByValue(value);
		}
	}

	/**
	 * Set a value on a single input element by its id after clearing it.
	 *
	 * @param id Input element identifier.
	 * @param value Value to set.
	 */
	public void setInput(String id, String value) {
		findElement(By.id(id)).clear();
		findElement(By.id(id)).sendKeys(value);
	}

	/**
	 * Set a value on a single input element by its name.
	 *
	 * @param name name to set.
	 * @param value Value to set.
	 */
	public void setInputByName(String name, String value) {
		findElement(By.name(name)).clear();
		findElement(By.name(name)).sendKeys(value);
	}

	/**
	 * Attach a local file to an input (for file uploading).
	 *
	 * @param id Input element ID.
	 * @param absolutePath absolute path to local filename.
	 */
	public void setInputFile(String id, String absolutePath) {
		WebElement upload = webDriver.findElement(By.id(id));
		upload.sendKeys(absolutePath);
	}

	public void start() {
		if (webDriver != null) {
			logger.warn("There are some open web browsers. They will be stopped.");
			stop();
		}

		logger.info("We are opening a web browser of type {}", clientConfiguration.getBrowserType());
		if (StringUtils.hasText(clientConfiguration.getSeleniumServer())) {
			try {
				webDriver = createRemoteWebDriver(clientConfiguration.getBrowserType(), clientConfiguration.getSeleniumServer());
			} catch (MalformedURLException ex) {
				logger.error(ex.getMessage());
				throw new CitrusRuntimeException(ex);
			}
		} else {
			webDriver = createLocalWebDriver(clientConfiguration.getBrowserType());
		}
	}

	public void stop() {
		if (webDriver == null) {
			logger.warn("There is no web browsers. Nothing will be stopped.");
		} else {
			logger.info("Stopping the WebClient with url <{}>", getCurrentUrl());
			closeWebDriver();
			webDriver = null;
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

			logger.info("Copy " + resourceFile + " to " + newFile);
			FileUtils.copyFile(resourceFile, newFile);

			return newFile.getCanonicalPath();
		} catch (IOException e) {
			logger.warn("Could not store file: " + fileLocation, e);
		}
		return null;
	}

	/**
	 * Take a screenshot.
	 *
	 * @return File containing the screenshot.
	 */
	public File takeScreenshot() {
		if (webDriver instanceof RemoteWebDriver) {
			return ((RemoteWebDriver) webDriver).getScreenshotAs(OutputType.FILE);
		}

		return null;
	}

	public void validate(By by, String expected, ValidationRuleEnum validationRule) {
		String actual = null;
		WebElement foundElement = null;
		if (by != null) {
			foundElement = findElement(by);
			if (foundElement != null) {
				actual = foundElement.getText();
			}
		}
		validate(actual, expected, validationRule);
	}

	public void validate(String actual, String expected, ValidationRuleEnum validationRule) {
		String validationError = "";

		if (validationRule == null) {
			if (expected == null || expected.isEmpty()) {
				validationRule = ValidationRuleEnum.EMPTY;
			} else {
				validationRule = ValidationRuleEnum.EQUALS;
			}
		}
		switch (validationRule) {
			case EMPTY:
				if (expected != null && !expected.isEmpty()) {
					validationError = "Validaton Error: For EMPTY validation not empty expected string is given: " + expected;
				} else if (actual != null && !actual.isEmpty()) { // handle null elements as empty element
					validationError = "Validaton Error: For EMPTY validation not empty element is found: " + actual;
				}
				break;
			case NOT_EMPTY:
				if (expected != null && !expected.isEmpty()) {
					validationError = "Validaton Error: For NOT_EMPTY validation not empty expected string is given: " + expected;
				} else if (actual == null || actual.isEmpty()) {
					validationError = "Validaton Error: For NOT_EMPTY validation empty element is found.";
				}
				break;
			case CONTAINS:
				if (actual == null) {
					validationError = "Validaton Error: For CONTAINS validation empty element is found.";
				} else if (expected == null) {
					validationError = "Validaton Error: For CONTAINS validation empty expected string is given";
				} else if (!actual.contains(expected)) {
					validationError = "Validaton Error: For CONTAINS validation actual <" + actual + "> does not contain <" + expected + ">.";
				}
				break;
			case NULL:
				if (expected != null && !expected.isEmpty()) {
					validationError = "Validaton Error: For NULL validation not empty expected string is given: " + expected;
				} else if (actual != null) {
					validationError = "Validaton Error: For NULL validation not null element is found: " + actual;
				}
				break;
			case NOT_NULL:
				if (actual == null) {
					validationError = "Validaton Error: For NOT_NULL validation empty element is found.";
				}
				break;
			case EQUALS:
			default:
				if (expected == null) {
					validationError = "Validaton Error: For EQUALS validation empty expected string is given";
				} else if (!expected.equals(actual)) {
					validationError = "Validaton Error: For EQUALS validation expected <" + expected + "> and actual <" + actual + "> do not match.";
				}
		}
		if (validationError != null && !validationError.isEmpty()) {
			throw new CitrusRuntimeException(validationError);
		}
	}

	@Override
	public void verifyPage(WebPage page) {
		if (!webDriver.getCurrentUrl().contains(page.getPageUrl())) {
			String errorMessage = "Expected page is <" + page.getPageUrl() + ">"
					+ " but found <" + webDriver.getCurrentUrl() + ">.";
			throw new NoSuchWindowException(errorMessage);
		}
	}

	/**
	 * Implicit wait.
	 *
	 * @param seconds to wait.
	 */
	public void wait(int seconds) {
		try {
			TimeUnit.SECONDS.sleep(seconds);
		} catch (InterruptedException e) {
			logger.debug("Wait interrupted", e);
		}
	}

	/**
	 * Wait until an item is loaded.
	 *
	 * @param by Element to wait for to be hidden.
	 * @param sec Timeout
	 * @return true when the element is invisible.
	 */
	public boolean waitUntilHidden(By by, long sec) {
		WebDriverWait q = new WebDriverWait(webDriver, sec);
		q.until(ExpectedConditions.invisibilityOfElementLocated(by));

		return !findElement(by).isDisplayed();
	}

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

	private WebDriver createLocalWebDriver(BrowserTypeEnum browserType) {
		switch (browserType) {
			case FIREFOX:
				DesiredCapabilities defaults = DesiredCapabilities.firefox();
				defaults.setCapability(FirefoxDriver.PROFILE, createFireFoxProfile());
				return new FirefoxDriver(defaults);
			case INTERNET_EXPLORER:
				return new InternetExplorerDriver();
			case CHROME:
				return new ChromeDriver();
			default:
				HtmlUnitDriver hud = new HtmlUnitDriver(BrowserVersion.FIREFOX_38);
				hud.setJavascriptEnabled(true);
				return hud;
		}
	}

	private RemoteWebDriver createRemoteWebDriver(BrowserTypeEnum browserType, String serverAddress) throws MalformedURLException {
		switch (browserType) {
			case FIREFOX:
				DesiredCapabilities defaultsFF = DesiredCapabilities.firefox();
				defaultsFF.setCapability(FirefoxDriver.PROFILE, createFireFoxProfile());
				return new RemoteWebDriver(new URL(serverAddress), defaultsFF);
			case INTERNET_EXPLORER:
				DesiredCapabilities defaultsIE = DesiredCapabilities.internetExplorer();
				defaultsIE.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
				return new RemoteWebDriver(new URL(serverAddress), defaultsIE);
			default:
				DesiredCapabilities defaults = DesiredCapabilities.chrome();
				defaults.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
				return new RemoteWebDriver(new URL(serverAddress), defaults);
		}
	}

	private Path createTemporaryStorage() {
		Path tempDir = null;
		try {
			tempDir = Files.createTempDirectory("selenium");
			tempDir.toFile().deleteOnExit();

			logger.info("Download storage location is: " + tempDir.toString());
		} catch (IOException e) {
			logger.warn("Could not create temporary storage", e);
		}
		return tempDir;
	}

	private String makeUniqueUrlToAvoidIECaching(String url, long unique) {
		if (url.contains("timestamp=")) {
			return url.replaceFirst("(.*)(timestamp=)(.*)([&#].*)", "$1$2" + unique + "$4")
					.replaceFirst("(.*)(timestamp=)(.*)$", "$1$2" + unique);
		} else {
			return url.contains("?")
					? url + "&timestamp=" + unique
					: url + "?timestamp=" + unique;
		}
	}
}
