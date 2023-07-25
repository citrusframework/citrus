/*
 *  Copyright 2023 the original author or authors.
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements. See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.selenium.yaml;

import org.citrusframework.TestActor;
import org.citrusframework.selenium.actions.AbstractSeleniumAction;
import org.citrusframework.selenium.actions.AlertAction;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;

public class Alert extends AbstractSeleniumAction.Builder<AlertAction, Alert> {

    private final AlertAction.Builder delegate = new AlertAction.Builder();

    /**
     * Add alert text validation.
     * @param text
     */
    public void setText(String text) {
        this.delegate.text(text);
    }

    /**
     * Accept alert dialog.
     */
    public void setAccept(boolean accept) {
        if (accept) {
            this.delegate.accept();
        } else {
            this.delegate.dismiss();
        }
    }

    /**
     * Dismiss alert dialog.
     */
    public void setDismiss(boolean dismiss) {
        if (dismiss) {
            this.delegate.dismiss();
        } else {
            this.delegate.accept();
        }
    }

    @Override
    public Alert description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Alert actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Alert browser(SeleniumBrowser seleniumBrowser) {
        delegate.browser(seleniumBrowser);
        return this;
    }

    @Override
    public AlertAction build() {
        return delegate.build();
    }
}
