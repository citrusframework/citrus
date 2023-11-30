/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.selenium.xml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActionContainerBuilder;
import org.citrusframework.TestActor;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.selenium.actions.AbstractSeleniumAction;
import org.citrusframework.selenium.actions.SeleniumAction;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

/**
 * @author Christoph Deppisch
 */
@XmlRootElement(name = "selenium")
public class Selenium implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    private AbstractSeleniumAction.Builder<?, ?> builder;

    private String description;
    private String actor;

    private String seleniumBrowser;

    private ReferenceResolver referenceResolver;

    @XmlElement
    public Selenium setDescription(String value) {
        this.description = value;
        return this;
    }

    @XmlAttribute
    public Selenium setActor(String actor) {
        this.actor = actor;
        return this;
    }

    @XmlAttribute
    public Selenium setBrowser(String browser) {
        this.seleniumBrowser = browser;
        return this;
    }

    /**
     * Start browser instance.
     */
    @XmlElement(name = "start")
    public void setStart(StartBrowser builder) {
        this.builder = builder;
    }

    /**
     * Stop browser instance.
     */
    @XmlElement(name = "stop")
    public void setStop(StopBrowser builder) {
        this.builder = builder;
    }

    /**
     * Alert element.
     */
    @XmlElement(name = "alert")
    public void setAlert(Alert builder) {
        this.builder = builder;
    }

    /**
     * Navigate action.
     */
    @XmlElement(name = "navigate")
    public void setNavigate(Navigate builder) {
        this.builder = builder;
    }

    /**
     * Page action.
     */
    @XmlElement(name = "page")
    public void setPage(Page builder) {
        this.builder = builder;
    }

    /**
     * Finds element.
     */
    @XmlElement(name = "find")
    public void setFind(FindElement builder) {
        this.builder = builder;
    }

    /**
     * Dropdown select single option action.
     */
    @XmlElement(name = "dropdown-select")
    public void setDropdownSelect(DropDownSelect builder) {
        this.builder = builder;
    }

    /**
     * Set input action.
     */
    @XmlElement(name = "set-input")
    public void setSetInput(SetInput builder) {
        this.builder = builder;
    }

    /**
     * Fill form action.
     */
    @XmlElement(name = "fill-form")
    public void setFillForm(FillForm builder) {
        this.builder = builder;
    }

    /**
     * Check input action.
     */
    @XmlElement(name = "check-input")
    public void setCheckInput(CheckInput builder) {
        this.builder = builder;
    }

    /**
     * Clicks element.
     */
    @XmlElement(name = "click")
    public void setClick(Click builder) {
        this.builder = builder;
    }

    /**
     * Hover element.
     */
    @XmlElement(name = "hover")
    public void setHover(Hover builder) {
        this.builder = builder;
    }

    /**
     * Clear browser cache.
     */
    @XmlElement(name = "clear-cache")
    public void setClearCache(ClearBrowserCache builder) {
        this.builder = builder;
    }

    /**
     * Make screenshot.
     */
    @XmlElement(name = "screenshot")
    public void setScreenshot(MakeScreenshot builder) {
        this.builder = builder;
    }

    /**
     * Store file.
     */
    @XmlElement(name = "store-file")
    public void setStoreFile(StoreFile builder) {
        this.builder = builder;
    }

    /**
     * Get stored file.
     */
    @XmlElement(name = "get-stored-file")
    public void setGetStoredFile(GetStoredFile builder) {
        this.builder = builder;
    }

    /**
     * Wait until element meets condition.
     */
    @XmlElement(name = "wait")
    public void setWaitUntil(WaitUntil builder) {
        this.builder = builder;
    }

    /**
     * Execute JavaScript.
     */
    @XmlElement(name = "javascript")
    public void setJavascript(JavaScript builder) {
        this.builder = builder;
    }

    /**
     * Open window.
     */
    @XmlElement(name = "open-window")
    public void setOpenWindow(OpenWindow builder) {
        this.builder = builder;
    }

    /**
     * Close window.
     */
    @XmlElement(name = "close-window")
    public void setCloseWindow(CloseWindow builder) {
        this.builder = builder;
    }

    /**
     * Focus window.
     */
    @XmlElement(name = "focus-window")
    public void setFocusWindow(SwitchWindow builder) {
        this.builder = builder;
    }

    /**
     * Switch window.
     */
    @XmlElement(name = "switch-window")
    public void setSwitchWindow(SwitchWindow builder) {
        this.builder = builder;
    }

    @Override
    public SeleniumAction build() {
        if (builder == null) {
            throw new CitrusRuntimeException("Missing Selenium action - please provide proper action details");
        }

        if (builder instanceof TestActionContainerBuilder<?,?>) {
            ((TestActionContainerBuilder<?,?>) builder).getActions().stream()
                    .filter(action -> action instanceof ReferenceResolverAware)
                    .forEach(action -> ((ReferenceResolverAware) action).setReferenceResolver(referenceResolver));
        }

        if (builder instanceof ReferenceResolverAware) {
            ((ReferenceResolverAware) builder).setReferenceResolver(referenceResolver);
        }

        builder.description(description);

        if (referenceResolver != null) {
            if (seleniumBrowser != null) {
                builder.browser(referenceResolver.resolve(seleniumBrowser, SeleniumBrowser.class));
            }

            if (actor != null) {
                builder.actor(referenceResolver.resolve(actor, TestActor.class));
            }
        }

        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
