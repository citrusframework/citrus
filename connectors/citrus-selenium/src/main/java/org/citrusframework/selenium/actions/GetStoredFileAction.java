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

import org.citrusframework.context.TestContext;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.endpoint.SeleniumHeaders;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class GetStoredFileAction extends AbstractSeleniumAction {

    /** File name to look for */
    private final String fileName;

    /**
     * Default constructor.
     */
    public GetStoredFileAction(Builder builder) {
        super("get-stored-file", builder);

        this.fileName = builder.fileName;
    }

    @Override
    protected void execute(SeleniumBrowser browser, TestContext context) {
        String filePath = browser.getStoredFile(context.replaceDynamicContentInString(fileName));
        context.setVariable(SeleniumHeaders.SELENIUM_DOWNLOAD_FILE, filePath);
    }

    /**
     * Gets the fileName.
     * @return
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractSeleniumAction.Builder<GetStoredFileAction, Builder> {

        private String fileName;

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        @Override
        public GetStoredFileAction build() {
            return new GetStoredFileAction(this);
        }
    }
}
