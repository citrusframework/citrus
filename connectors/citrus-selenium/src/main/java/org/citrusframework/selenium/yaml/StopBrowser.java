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
import org.citrusframework.selenium.actions.StopBrowserAction;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

public class StopBrowser extends AbstractSeleniumAction.Builder<StopBrowserAction, StopBrowser> implements ReferenceResolverAware {

    private final StopBrowserAction.Builder delegate = new StopBrowserAction.Builder();

    private String seleniumBrowser;

    private ReferenceResolver referenceResolver;

    public void setBrowser(String browser) {
        this.seleniumBrowser = browser;
    }

    @Override
    public StopBrowser description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public StopBrowser actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public StopBrowser browser(SeleniumBrowser seleniumBrowser) {
        delegate.browser(seleniumBrowser);
        return this;
    }

    @Override
    public StopBrowserAction build() {
        if (seleniumBrowser != null) {
            delegate.browser(referenceResolver.resolve(seleniumBrowser, SeleniumBrowser.class));
        }

        return delegate.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
