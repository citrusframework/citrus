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

import java.util.LinkedHashMap;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.openqa.selenium.By;
import org.openqa.selenium.json.Json;

/**
 * Fill out form with given key-value pairs where each key is used to find the form field.
 * Sets field values with set input action that supports both input and select form controls.
 * Supports to submit the form after all fields are set.
 *
 * @author Christoph Deppisch
 */
public class FillFormAction extends AbstractSeleniumAction {

    /** Key value pairs representing the form fields to fill */
    private final Map<By, String> formFields;

    /** Optional submit button id that gets clicked after fields are filled */
    private final By submitButton;

    /**
     * Default constructor.
     */
    public FillFormAction(Builder builder) {
        super("fill-form", builder);

        this.formFields = builder.formFields;
        this.submitButton = builder.submitButton;
    }

    @Override
    public void execute(SeleniumBrowser browser, TestContext context) {
        formFields.forEach((by, value) -> {
            new SetInputAction.Builder()
                    .element(by)
                    .value(value)
                    .build()
                    .execute(browser, context);
        });

        if (submitButton != null) {
            new ClickAction.Builder()
                    .element(submitButton)
                    .build()
                    .execute(browser, context);
        }
    }

    public Map<By, String> getFormFields() {
        return formFields;
    }

    public By getSubmitButton() {
        return submitButton;
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractSeleniumAction.Builder<FillFormAction, FillFormAction.Builder> {

        private final Map<By, String> formFields = new LinkedHashMap<>();

        private By submitButton;

        public Builder field(By by, String value) {
            this.formFields.put(by, value);
            return this;
        }

        public Builder field(String id, String value) {
            return field(By.id(id), value);
        }

        public Builder fromJson(String formFieldsJson) {
            return fields(new Json().toType(formFieldsJson, Map.class));
        }

        public Builder submit() {
            this.submitButton = By.xpath("//input[@type='submit']");
            return this;
        }

        public Builder submit(String id) {
            return submit(By.id(id));
        }

        public Builder submit(By button) {
            this.submitButton = button;
            return this;
        }

        public Builder fields(Map<String, String> fields) {
            fields.forEach(this::field);
            return this;
        }

        @Override
        public FillFormAction build() {
            return new FillFormAction(this);
        }
    }
}
