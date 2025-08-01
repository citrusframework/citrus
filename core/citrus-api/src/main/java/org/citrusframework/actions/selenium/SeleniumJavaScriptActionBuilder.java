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

import java.nio.charset.Charset;
import java.util.List;

import org.citrusframework.TestAction;
import org.citrusframework.spi.Resource;

public interface SeleniumJavaScriptActionBuilder<T extends TestAction, B extends SeleniumJavaScriptActionBuilder<T, B>>
        extends SeleniumActionBuilderBase<T, B> {

    /**
     * Add script.
     */
    B script(String script);

    B script(Resource resource);

    B script(Resource resource, Charset charset);

    /**
     * Add script arguments.
     */
    B arguments(Object... args);

    /**
     * Add script arguments.
     */
    B arguments(List<Object> args);

    /**
     * Add script argument.
     */
    B argument(Object arg);

    /**
     * Add expected error.
     */
    B errors(String... errors);

    /**
     * Add expected error.
     */
    B errors(List<String> errors);

    /**
     * Add expected error.
     */
    B error(String error);
}
