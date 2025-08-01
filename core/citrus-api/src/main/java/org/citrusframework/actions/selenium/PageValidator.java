/*
 * Copyright the original author or authors.
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

package org.citrusframework.actions.selenium;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * @since 2.7
 */
@FunctionalInterface
public interface PageValidator<T extends WebPage, B extends Endpoint> {

    /**
     * Validate page contents.
     */
    void validate(T webPage, B browser, TestContext context);

    @SuppressWarnings("unchecked")
    default void adaptAndValidate(WebPage webPage, Endpoint endpoint, TestContext context) {
        B browser;
        T page;

        try {
            page = (T) webPage;
        } catch (ClassCastException e) {
            throw new CitrusRuntimeException("Page validation failed, provided page '%s' is not of required type".formatted(webPage.getClass().getName()), e);
        }

        try {
            browser = (B) endpoint;
        } catch (ClassCastException e) {
            throw new CitrusRuntimeException("Page validation failed, provided endpoint '%s' is not of required type".formatted(endpoint.getClass().getName()), e);
        }

        validate(page, browser, context);
    }
}
