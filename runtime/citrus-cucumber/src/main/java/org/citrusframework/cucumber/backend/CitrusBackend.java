/*
 * Copyright 2006-2016 the original author or authors.
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

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.cucumber.CitrusLifecycleHooks;
import org.citrusframework.cucumber.CitrusReporter;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.Snippet;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.resource.ClasspathSupport;
import io.cucumber.java.After;
import io.cucumber.java.Before;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class CitrusBackend implements Backend {

    /** Basic resource loader */
    protected final Lookup lookup;
    protected final Container container;

    /**
     * Constructor using resource loader.
     * @param lookup
     * @param container
     */
    public CitrusBackend(Lookup lookup, Container container) {
        this.lookup = lookup;
        this.container = container;

        CitrusInstanceManager.addInstanceProcessor(instance -> instance.beforeSuite(CitrusReporter.SUITE_NAME));
    }

    @Override
    public void loadGlue(Glue glue, List<URI> gluePaths) {
        try {
            if (!gluePaths.contains(getLifecycleHooksGluePath()) && container.addClass(CitrusLifecycleHooks.class)) {
                Method beforeMethod = CitrusLifecycleHooks.class.getMethod("before", Scenario.class);
                Before beforeAnnotation = beforeMethod.getAnnotation(Before.class);
                glue.addBeforeHook(new CitrusHookDefinition(beforeMethod, beforeAnnotation.value(), beforeAnnotation.order(), lookup));

                Method afterMethod = CitrusLifecycleHooks.class.getMethod("after", Scenario.class);
                After afterAnnotation = afterMethod.getAnnotation(After.class);
                glue.addAfterHook(new CitrusHookDefinition(afterMethod, afterAnnotation.value(), afterAnnotation.order(), lookup));
            }
        } catch (NoSuchMethodException e) {
            throw new CucumberException("Unable to add Citrus lifecycle hooks");
        }
    }

    /**
     * Helper to create proper URI pointing to {@link CitrusLifecycleHooks}.
     * @return
     */
    private static URI getLifecycleHooksGluePath() {
        return URI.create(ClasspathSupport.CLASSPATH_SCHEME_PREFIX +
                ClasspathSupport.resourceNameOfPackageName(CitrusLifecycleHooks.class.getPackage().getName()));
    }

    @Override
    public void buildWorld() {
    }

    @Override
    public void disposeWorld() {
    }

    @Override
    public Snippet getSnippet() {
        return new NoopSnippet();
    }
}
