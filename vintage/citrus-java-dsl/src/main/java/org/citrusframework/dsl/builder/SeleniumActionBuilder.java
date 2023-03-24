package org.citrusframework.dsl.builder;

import java.nio.charset.Charset;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.selenium.actions.AlertAction;
import org.citrusframework.selenium.actions.CheckInputAction;
import org.citrusframework.selenium.actions.ClearBrowserCacheAction;
import org.citrusframework.selenium.actions.ClickAction;
import org.citrusframework.selenium.actions.CloseWindowAction;
import org.citrusframework.selenium.actions.DropDownSelectAction;
import org.citrusframework.selenium.actions.FindElementAction;
import org.citrusframework.selenium.actions.GetStoredFileAction;
import org.citrusframework.selenium.actions.HoverAction;
import org.citrusframework.selenium.actions.JavaScriptAction;
import org.citrusframework.selenium.actions.MakeScreenshotAction;
import org.citrusframework.selenium.actions.NavigateAction;
import org.citrusframework.selenium.actions.OpenWindowAction;
import org.citrusframework.selenium.actions.PageAction;
import org.citrusframework.selenium.actions.SeleniumAction;
import org.citrusframework.selenium.actions.SetInputAction;
import org.citrusframework.selenium.actions.StartBrowserAction;
import org.citrusframework.selenium.actions.StopBrowserAction;
import org.citrusframework.selenium.actions.StoreFileAction;
import org.citrusframework.selenium.actions.SwitchWindowAction;
import org.citrusframework.selenium.actions.WaitUntilAction;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.model.WebPage;
import org.citrusframework.util.FileUtils;
import org.springframework.core.io.Resource;

/**
 * @author Christoph Deppisch
 */
public class SeleniumActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<SeleniumAction> {

    private final org.citrusframework.selenium.actions.SeleniumActionBuilder delegate = new org.citrusframework.selenium.actions.SeleniumActionBuilder();

    public SeleniumActionBuilder browser(SeleniumBrowser seleniumBrowser) {
        delegate.browser(seleniumBrowser);
        return this;
    }

    public StartBrowserAction.Builder start() {
        return delegate.start();
    }

    public StartBrowserAction.Builder start(SeleniumBrowser seleniumBrowser) {
        return delegate.start(seleniumBrowser);
    }

    public StopBrowserAction.Builder stop() {
        return delegate.stop();
    }

    public StopBrowserAction.Builder stop(SeleniumBrowser seleniumBrowser) {
        return delegate.stop(seleniumBrowser);
    }

    public AlertAction.Builder alert() {
        return delegate.alert();
    }

    public NavigateAction.Builder navigate(String page) {
        return delegate.navigate(page);
    }

    public PageAction.Builder page(WebPage page) {
        return delegate.page(page);
    }

    public PageAction.Builder page(Class<? extends WebPage> pageType) {
        return delegate.page(pageType);
    }

    public FindElementAction.Builder find() {
        return delegate.find();
    }

    public DropDownSelectAction.Builder select(String option) {
        return delegate.select(option);
    }

    public DropDownSelectAction.Builder select(String ... options) {
        return delegate.select(options);
    }

    public SetInputAction.Builder setInput(String value) {
        return delegate.setInput(value);
    }

    public CheckInputAction.Builder checkInput(boolean checked) {
        return delegate.checkInput(checked);
    }

    public ClickAction.Builder click() {
        return delegate.click();
    }

    public HoverAction.Builder hover() {
        return delegate.hover();
    }

    public ClearBrowserCacheAction.Builder clearCache() {
        return delegate.clearCache();
    }

    public MakeScreenshotAction.Builder screenshot() {
        return delegate.screenshot();
    }

    public MakeScreenshotAction.Builder screenshot(String outputDir) {
        return delegate.screenshot(outputDir);
    }

    public StoreFileAction.Builder store(String filePath) {
        return delegate.store(filePath);
    }

    public GetStoredFileAction.Builder getStored(String fileName) {
        return delegate.getStored(fileName);
    }

    public WaitUntilAction.Builder waitUntil() {
        return delegate.waitUntil();
    }

    public JavaScriptAction.Builder javascript(String script) {
        return delegate.javascript(script);
    }

    public JavaScriptAction.Builder javascript(Resource script) {
        return javascript(script, FileUtils.getDefaultCharset());
    }

    public JavaScriptAction.Builder javascript(Resource scriptResource, Charset charset) {
        return delegate.javascript(scriptResource, charset);
    }

    public OpenWindowAction.Builder open() {
        return delegate.open();
    }

    public CloseWindowAction.Builder close() {
        return delegate.close();
    }

    public SwitchWindowAction.Builder focus() {
        return delegate.focus();
    }

    @Override
    public SeleniumAction build() {
        return delegate.build();
    }

    @Override
    public TestActionBuilder<?> getDelegate() {
        return delegate;
    }
}
