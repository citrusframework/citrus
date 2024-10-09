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

package org.citrusframework;

import org.citrusframework.config.CitrusSpringConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Context provider registered via resource path lookup. When module is on classpath this provider will be used to instantiate
 * Citrus.
 *
 * Provider creates a CitrusContext that is backed with a Spring application context. Provider caches the last application context
 * that has created a context. When very same application context creates another CitrusContext use the cached instance. This
 * caching should give us some performance improvements and less instance duplications.
 *
 */
public class CitrusSpringContextProvider implements CitrusContextProvider {

    private static CitrusSpringContext context;

    private final ApplicationContext applicationContext;

    public CitrusSpringContextProvider() {
        this.applicationContext = null;
    }

    public CitrusSpringContextProvider(Class<? extends CitrusSpringConfig> configClass) {
        this(new AnnotationConfigApplicationContext(configClass));
    }

    public CitrusSpringContextProvider(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public CitrusContext create() {
        if (applicationContext == null) {
            context = CitrusSpringContext.create();
        } else if (context == null) {
            context = CitrusSpringContext.create(applicationContext);
        } else if (!context.getApplicationContext().equals(applicationContext)) {
            context = CitrusSpringContext.create(applicationContext);
        }

        return context;
    }
}
