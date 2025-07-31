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

package org.citrusframework.actions.jbang;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.spi.Resource;
import org.citrusframework.validation.ValidationProcessor;

public interface JBangActionBuilder<T extends TestAction, B extends TestActionBuilder<T>> {

    B app(String name);

    B command(String command);

    B script(String script);

    B file(String path);

    B file(Resource resource);

    B arg(String name, String value);

    B arg(String value);

    B args(String... args);

    B systemProperty(String name, String value);

    B exitCode(int code);

    B exitCodes(int... codes);

    B printOutput(boolean enabled);

    B verifyOutput(String expected);

    B verifyOutput(ValidationProcessor validationProcessor);

    B savePid(String variable);

    B saveOutput(String variable);

    interface BuilderFactory {

        JBangActionBuilder<?, ?> jbang();

    }
}
