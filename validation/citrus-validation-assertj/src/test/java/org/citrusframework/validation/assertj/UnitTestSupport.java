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

package org.citrusframework.validation.assertj;

import org.citrusframework.context.TestContextFactory;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;

public abstract class UnitTestSupport extends AbstractUnitTest {

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory factory = TestContextFactory.newInstance();
        factory.getMessageValidatorRegistry().addMessageValidator("all", new DefaultTextEqualsMessageValidator());
        return factory;
    }
}
