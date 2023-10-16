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

package org.citrusframework.selenium.actions;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.endpoint.SeleniumHeaders;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.StringUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class MakeScreenshotAction extends AbstractSeleniumAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger( MakeScreenshotAction.class);

    /** Storage to save screenshot to */
    private final String outputDir;

    /**
     * Default constructor.
     */
    public MakeScreenshotAction(Builder builder) {
        super("screenshot", builder);

        this.outputDir = builder.outputDir;
    }

    @Override
    protected void execute(SeleniumBrowser browser, TestContext context) {
        File screenshot = null;
        if (browser.getWebDriver() instanceof TakesScreenshot) {
            screenshot = ((TakesScreenshot) browser.getWebDriver()).getScreenshotAs(OutputType.FILE);
        } else {
            logger.warn("Skip screenshot action because web driver is missing screenshot features");
        }

        if (screenshot != null) {
            String testName = "Test";
            if (context.getVariables().containsKey(CitrusSettings.TEST_NAME_VARIABLE)) {
                testName = context.getVariable(CitrusSettings.TEST_NAME_VARIABLE);
            }

            context.setVariable(SeleniumHeaders.SELENIUM_SCREENSHOT, testName + "_" + screenshot.getName());

            if (StringUtils.hasText(outputDir)) {
                try {
                    File targetDir = new File(context.replaceDynamicContentInString(outputDir));
                    if (!targetDir.exists()) {
                        if (!targetDir.mkdirs()) {
                            throw new CitrusRuntimeException("Failed to create screenshot output directory: " + outputDir);
                        }
                    }

                    FileUtils.copyFile(screenshot, new File(targetDir, testName + "_" + screenshot.getName()));
                } catch (IOException e) {
                    logger.error("Failed to save screenshot to target storage", e);
                }
            } else {
                browser.storeFile(Resources.create(screenshot));
            }
        }
    }

    /**
     * Gets the outputDir.
     * @return
     */
    public String getOutputDir() {
        return outputDir;
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractSeleniumAction.Builder<MakeScreenshotAction, Builder> {

        private String outputDir;

        public Builder outputDir(String outputDir) {
            this.outputDir = outputDir;
            return this;
        }

        @Override
        public MakeScreenshotAction build() {
            return new MakeScreenshotAction(this);
        }
    }
}
