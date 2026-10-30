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

package org.citrusframework.spring;

import java.util.concurrent.atomic.AtomicBoolean;

import org.citrusframework.Citrus;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Runs the Citrus before suite hooks once per test run and the after suite hooks when the run ends,
 * as the TestNG {@code @BeforeSuite} and {@code @AfterSuite} methods of the unit test support did.
 */
public class CitrusSuiteExtension implements BeforeAllCallback {

    private static final String SUITE_NAME = "citrus-spring-unit-tests";

    private static final AtomicBoolean started = new AtomicBoolean(false);

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        if (started.compareAndSet(false, true)) {
            Citrus citrus = Citrus.newInstance(new CitrusSpringContextProvider(SpringExtension.getApplicationContext(extensionContext)));
            citrus.beforeSuite(SUITE_NAME);
            extensionContext.getRoot().getStore(ExtensionContext.Namespace.create(CitrusSuiteExtension.class))
                    .put("afterSuite", (AutoCloseable) () -> citrus.afterSuite(SUITE_NAME));
        }
    }
}
