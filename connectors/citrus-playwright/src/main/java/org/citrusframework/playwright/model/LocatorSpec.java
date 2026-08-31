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

package org.citrusframework.playwright.model;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.Objects;
import java.util.function.Function;

public class LocatorSpec {

    public enum Type {
        CSS,
        XPATH,
        TEXT,
        ROLE,
        TEST_ID,
        RAW
    }

    private final Type type;
    private final String selector;
    private String name;
    private Integer nth;
    private boolean first;
    private boolean last;
    private Function<Page, Locator> rawLocator;

    private LocatorSpec(Type type, String selector) {
        this.type = type;
        this.selector = selector;
    }

    public static LocatorSpec css(String selector) {
        return new LocatorSpec(Type.CSS, selector);
    }

    public static LocatorSpec xpath(String selector) {
        return new LocatorSpec(Type.XPATH, selector);
    }

    public static LocatorSpec text(String text) {
        return new LocatorSpec(Type.TEXT, text);
    }

    public static LocatorSpec role(String role) {
        return new LocatorSpec(Type.ROLE, role);
    }

    public static LocatorSpec testId(String testId) {
        return new LocatorSpec(Type.TEST_ID, testId);
    }

    public static LocatorSpec raw(Function<Page, Locator> rawLocator) {
        LocatorSpec spec = new LocatorSpec(Type.RAW, null);
        spec.rawLocator = Objects.requireNonNull(rawLocator, "rawLocator must not be null");
        return spec;
    }

    public LocatorSpec name(String name) {
        this.name = name;
        return this;
    }

    public LocatorSpec nth(int nth) {
        this.nth = nth;
        return this;
    }

    public LocatorSpec first() {
        this.first = true;
        this.last = false;
        this.nth = null;
        return this;
    }

    public LocatorSpec last() {
        this.last = true;
        this.first = false;
        this.nth = null;
        return this;
    }

    public Type getType() {
        return type;
    }

    public String getSelector() {
        return selector;
    }

    public String getName() {
        return name;
    }

    public Integer getNth() {
        return nth;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isLast() {
        return last;
    }

    public Function<Page, Locator> getRawLocator() {
        return rawLocator;
    }
}
