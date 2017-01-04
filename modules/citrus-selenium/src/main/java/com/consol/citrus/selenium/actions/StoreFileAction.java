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

package com.consol.citrus.selenium.actions;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;

/**
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class StoreFileAction extends AbstractSeleniumAction {

    /** File location */
    private String fileLocation;

    /**
     * Default constructor.
     */
    public StoreFileAction() {
        super("store-file");
    }

    @Override
    protected void execute(SeleniumBrowser browser, TestContext context) {
        browser.storeFile(context.replaceDynamicContentInString(fileLocation));
    }

    /**
     * Gets the fileLocation.
     *
     * @return
     */
    public String getFileLocation() {
        return fileLocation;
    }

    /**
     * Sets the fileLocation.
     *
     * @param fileLocation
     */
    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }
}
