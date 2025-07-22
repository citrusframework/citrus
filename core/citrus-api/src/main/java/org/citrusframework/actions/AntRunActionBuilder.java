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

package org.citrusframework.actions;

import java.util.Properties;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;

public interface AntRunActionBuilder<T extends TestAction>
        extends ActionBuilder<T, AntRunActionBuilder<T>>,
                TestActionBuilder<T>, ReferenceResolverAwareBuilder<T, AntRunActionBuilder<T>> {

    /**
     * Sets the build file path.
     * @param buildFilePath
     * @return
     */
    AntRunActionBuilder<T> buildFilePath(String buildFilePath);

    /**
     * Build target name to call.
     * @param target
     */
    AntRunActionBuilder<T> target(String target);

    /**
     * Multiple build target names to call.
     * @param targets
     */
    AntRunActionBuilder<T> targets(String... targets);

    /**
     * Adds a build property by name and value.
     * @param name
     * @param value
     */
    AntRunActionBuilder<T> property(String name, Object value);

    /**
     * Adds build properties.
     * @param properties
     */
    AntRunActionBuilder<T> properties(Properties properties);

    /**
     * Adds a build property file reference by file path.
     * @param filePath
     */
    AntRunActionBuilder<T> propertyFile(String filePath);

    /**
     * Adds custom build listener implementation.
     * @param buildListener
     */
    AntRunActionBuilder<T> listener(Object buildListener);

    /**
     * Adds custom build listener implementation.
     * @param buildListener
     */
    AntRunActionBuilder<T> listenerName(String buildListener);

    interface BuilderFactory {

        /**
         * Fluent API action building entry method used in Java DSL.
         */
        AntRunActionBuilder<?> antrun();

        default AntRunActionBuilder<?> antrun(String filePath) {
            return antrun().buildFilePath(filePath);
        }

    }

}
