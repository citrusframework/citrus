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

package org.citrusframework.actions.groovy;

import java.nio.charset.Charset;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ActionBuilder;
import org.citrusframework.spi.Resource;

public interface GroovyCreateEndpointsActionBuilder<T extends TestAction, B extends GroovyCreateEndpointsActionBuilder<T, B>>
        extends ActionBuilder<T, B>, TestActionBuilder<T> {

    /**
     * Sets the Groovy script to execute.
     */
    B script(String script);

    /**
     * Sets the Groovy script to execute.
     */
    B script(Resource scriptResource);

    /**
     * Sets the Groovy script to execute.
     */
    B script(Resource scriptResource, Charset charset);

    /**
     * Sets the Groovy script to execute.
     */
    B scriptResourcePath(String scriptResourcePath);
}
