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

import java.util.List;

import org.citrusframework.TestActor;
import org.citrusframework.selenium.actions.AbstractSeleniumAction;
import org.citrusframework.selenium.actions.JavaScriptAction;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;

public class JavaScript extends AbstractSeleniumAction.Builder<JavaScriptAction, JavaScript> {

    private final JavaScriptAction.Builder delegate = new JavaScriptAction.Builder();

    /**
     * Add script.
     * @param script
     */
    public void setScript(String script) {
        this.delegate.script(script);
    }

    /**
     * Add script argument.
     * @param arg
     */
    public void setArgument(Object arg) {
        this.delegate.argument(arg);
    }

    /**
     * Add script arguments.
     * @param args
     */
    public void setArguments(List<Object> args) {
        this.delegate.arguments(args);
    }

    /**
     * Add expected error.
     * @param error
     */
    public void setError(String error) {
        this.delegate.error(error);
    }

    /**
     * Add expected error.
     * @param errors
     */
    public void setErrors(List<String> errors) {
        this.delegate.errors(errors);
    }

    @Override
    public JavaScript description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public JavaScript actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public JavaScript browser(SeleniumBrowser seleniumBrowser) {
        delegate.browser(seleniumBrowser);
        return this;
    }

    @Override
    public JavaScriptAction build() {
        return delegate.build();
    }
}
