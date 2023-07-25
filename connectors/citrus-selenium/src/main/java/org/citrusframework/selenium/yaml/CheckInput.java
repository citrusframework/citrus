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
import org.citrusframework.selenium.actions.CheckInputAction;
import org.citrusframework.selenium.actions.FindElementAction;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;

public class CheckInput extends AbstractSeleniumAction.Builder<CheckInputAction, CheckInput> implements ElementAware {

    private final CheckInputAction.Builder delegate = new CheckInputAction.Builder();

    public void setChecked(boolean checked) {
        this.delegate.checked(checked);
    }

    @Override
    public void setElement(Element element) {
        ElementAware.super.setElement(element);
    }

    @Override
    public FindElementAction.ElementActionBuilder<?, ?> getElementBuilder() {
        return delegate;
    }

    @Override
    public CheckInput description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public CheckInput actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public CheckInput browser(SeleniumBrowser seleniumBrowser) {
        delegate.browser(seleniumBrowser);
        return this;
    }

    @Override
    public CheckInputAction build() {
        return delegate.build();
    }
}
