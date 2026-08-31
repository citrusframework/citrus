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

package org.citrusframework.playwright.page;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;

/**
 * Reflection-based page-object factory used by {@code PageObjectAction}.
 *
 * <p>The factory supports constructors and methods that accept Playwright
 * runtime objects ({@link Page}, {@link BrowserContext}), the
 * {@link PlaywrightBrowser} endpoint, the Citrus {@link TestContext}, and
 * string arguments resolved from Citrus variables.</p>
 */
public class PlaywrightPageFactory {

    /**
     * Instantiates a page object and calls its initialization hook when it
     * implements {@link PlaywrightPage}.
     *
     * @param type page-object class
     * @param browser active Playwright endpoint
     * @param context active Citrus test context
     * @param <T> page-object type
     * @return instantiated page object
     */
    public <T> T create(Class<T> type, PlaywrightBrowser browser, TestContext context) {
        try {
            T page = instantiate(type, browser.getCurrentPage(), browser.getCurrentContext(), browser, context);
            if (page instanceof PlaywrightPage playwrightPage) {
                playwrightPage.initialize(browser.getCurrentPage(), browser.getCurrentContext(), browser, context);
            }
            return page;
        } catch (ReflectiveOperationException e) {
            throw new CitrusRuntimeException("Failed to create Playwright page object: " + type.getName(), e);
        }
    }

    /**
     * Invokes a supported method on a page object.
     *
     * @param target page-object instance
     * @param methodName method name
     * @param arguments string arguments resolved through Citrus variables
     * @param browser active Playwright endpoint
     * @param context active Citrus test context
     * @return method return value
     */
    public Object invoke(Object target, String methodName, String[] arguments, PlaywrightBrowser browser, TestContext context) {
        Method method = Arrays.stream(target.getClass().getMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .filter(candidate -> supports(candidate.getParameterTypes(), arguments.length))
                .findFirst()
                .orElseThrow(() -> new CitrusRuntimeException("No supported Playwright page-object method found: " + methodName));
        try {
            return method.invoke(target, resolveArguments(method.getParameterTypes(), arguments, browser, context));
        } catch (ReflectiveOperationException e) {
            throw new CitrusRuntimeException("Failed to invoke Playwright page-object method: " + methodName, e);
        }
    }

    private <T> T instantiate(Class<T> type, Page page, BrowserContext browserContext,
                             PlaywrightBrowser browser, TestContext context) throws ReflectiveOperationException {
        for (Constructor<?> constructor : type.getConstructors()) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 1 && parameterTypes[0] == Page.class) {
                return type.cast(constructor.newInstance(page));
            }
            if (parameterTypes.length == 1 && parameterTypes[0] == BrowserContext.class) {
                return type.cast(constructor.newInstance(browserContext));
            }
            if (parameterTypes.length == 1 && parameterTypes[0] == PlaywrightBrowser.class) {
                return type.cast(constructor.newInstance(browser));
            }
            if (parameterTypes.length == 1 && parameterTypes[0] == TestContext.class) {
                return type.cast(constructor.newInstance(context));
            }
        }
        Constructor<T> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private boolean supports(Class<?>[] parameterTypes, int stringArguments) {
        int strings = 0;
        for (Class<?> type : parameterTypes) {
            if (type == String.class) {
                strings++;
            } else if (type != Page.class && type != BrowserContext.class
                    && type != PlaywrightBrowser.class && type != TestContext.class) {
                return false;
            }
        }
        return strings == stringArguments;
    }

    private Object[] resolveArguments(Class<?>[] parameterTypes, String[] arguments,
                                      PlaywrightBrowser browser, TestContext context) {
        Object[] resolved = new Object[parameterTypes.length];
        int stringIndex = 0;
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> type = parameterTypes[i];
            if (type == String.class) {
                resolved[i] = context.replaceDynamicContentInString(arguments[stringIndex++]);
            } else if (type == Page.class) {
                resolved[i] = browser.getCurrentPage();
            } else if (type == BrowserContext.class) {
                resolved[i] = browser.getCurrentContext();
            } else if (type == PlaywrightBrowser.class) {
                resolved[i] = browser;
            } else if (type == TestContext.class) {
                resolved[i] = context;
            }
        }
        return resolved;
    }
}
