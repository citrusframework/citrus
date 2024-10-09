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

package org.citrusframework.cucumber.backend;

import java.util.function.Supplier;

import org.citrusframework.cucumber.backend.spring.CitrusSpringBackend;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Lookup;
import org.citrusframework.spi.Resources;

public class CitrusBackendProviderService implements BackendProviderService {
    @Override
    public Backend create(Lookup lookup, Container container, Supplier<ClassLoader> classLoader) {
        if (Resources.fromClasspath("org/citrusframework/CitrusSpringContext.class").exists()) {
            return new CitrusSpringBackend(lookup, container);
        } else {
            return new CitrusBackend(lookup, container);
        }
    }
}
