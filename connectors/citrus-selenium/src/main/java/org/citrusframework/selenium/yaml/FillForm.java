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
import java.util.stream.Collectors;

import org.citrusframework.TestActor;
import org.citrusframework.selenium.actions.AbstractSeleniumAction;
import org.citrusframework.selenium.actions.FillFormAction;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;

public class FillForm extends AbstractSeleniumAction.Builder<FillFormAction, FillForm> {

    private final FillFormAction.Builder delegate = new FillFormAction.Builder();

    public void setFields(List<Field> fields) {
        delegate.fields(fields
                .stream()
                .collect(Collectors.toMap(Field::getId, Field::getValue)));
    }

    public void setJson(String json) {
        this.delegate.fromJson(json);
    }

    public void setSubmit(String value) {
        this.delegate.submit(value);
    }

    @Override
    public FillForm description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public FillForm actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public FillForm browser(SeleniumBrowser seleniumBrowser) {
        delegate.browser(seleniumBrowser);
        return this;
    }

    public static class Field {
        private String id;
        private String value;

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @Override
    public FillFormAction build() {
        return delegate.build();
    }
}
