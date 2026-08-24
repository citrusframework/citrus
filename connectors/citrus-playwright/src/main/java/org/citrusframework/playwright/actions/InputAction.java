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

package org.citrusframework.playwright.actions;

import com.microsoft.playwright.Locator;

import java.nio.file.Path;
import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.LocatorResolver;
import org.citrusframework.util.StringUtils;

public class InputAction extends AbstractPlaywrightAction {

    public enum Command {
        FILL,
        CLEAR,
        PRESS,
        CHECK,
        UNCHECK,
        SELECT,
        UPLOAD
    }

    private final Command command;
    private final org.citrusframework.playwright.model.LocatorSpec locator;
    private final String value;
    private final List<String> values;

    public InputAction(Builder builder) {
        super(builder.command.name().toLowerCase(), builder);
        this.command = builder.command;
        this.locator = builder.locator;
        this.value = builder.value;
        this.values = builder.values;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        Locator element = LocatorResolver.resolve(browser.getCurrentPage(), locator, context);
        switch (command) {
            case FILL -> element.fill(LocatorResolver.resolve(value, context));
            case CLEAR -> element.clear();
            case PRESS -> element.press(LocatorResolver.resolve(value, context));
            case CHECK -> element.check();
            case UNCHECK -> element.uncheck();
            case SELECT -> element.selectOption(resolveValues(context));
            case UPLOAD -> element.setInputFiles(Path.of(LocatorResolver.resolve(value, context)));
        }
    }

    private String[] resolveValues(TestContext context) {
        List<String> source = values == null || values.isEmpty() ? List.of(value) : values;
        return source.stream().map(v -> LocatorResolver.resolve(v, context)).toArray(String[]::new);
    }

    public Command getCommand() {
        return command;
    }

    public String getValue() {
        return value;
    }

    public List<String> getValues() {
        return values;
    }

    public static class Builder extends ElementActionBuilder<InputAction, Builder> {
        private final Command command;
        private String value;
        private List<String> values;

        public Builder(Command command) {
            this.command = command;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder key(String key) {
            return value(key);
        }

        public Builder file(String file) {
            return value(file);
        }

        public Builder values(String... values) {
            this.values = List.of(values);
            return this;
        }

        @Override
        public InputAction build() {
            requireLocator();
            if ((command == Command.FILL || command == Command.PRESS || command == Command.UPLOAD)
                    && !StringUtils.hasText(value)) {
                throw new CitrusRuntimeException("Missing Playwright input value - call value(...), key(...), or file(...) before building action");
            }
            if (command == Command.SELECT && !StringUtils.hasText(value) && (values == null || values.isEmpty())) {
                throw new CitrusRuntimeException("Missing Playwright select value - call value(...) or values(...) before building action");
            }
            return new InputAction(this);
        }
    }
}
