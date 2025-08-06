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

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ReferenceResolverAwareBuilder;
import org.citrusframework.spi.Resource;

public interface GroovyActionBuilder<T extends TestAction, B extends GroovyActionBuilder<T, B>>
        extends ReferenceResolverAwareBuilder<T, B>, TestActionBuilder<T> {
    /**
     * Run Groovy script.
     */
    GroovyRunActionBuilder<?, ?> run();

    /**
     * Run given Groovy script.
     */
    default GroovyRunActionBuilder<?, ?> script(String script) {
        return run().script(script);
    }

    /**
     * Run given Groovy script resource.
     */
    default GroovyRunActionBuilder<?, ?> script(Resource script) {
        return run().script(script);
    }

    /**
     * Create endpoints from Groovy script.
     */
    GroovyCreateEndpointsActionBuilder<?, ?> endpoints();

    /**
     * Create beans in registry from Groovy script.
     */
    GroovyCreateBeansActionBuilder<?, ?> beans();

    interface BuilderFactory {

        GroovyActionBuilder<?, ?> groovy();

    }
}
