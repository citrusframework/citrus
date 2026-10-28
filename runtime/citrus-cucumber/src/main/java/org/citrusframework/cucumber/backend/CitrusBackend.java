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

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition.HookType;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.Snippet;
import io.cucumber.core.backend.StaticHookDefinition;
import io.cucumber.core.backend.discovery.GlueDiscoveryRequest;
import io.cucumber.core.backend.discovery.UriGlueDiscoverySelector;
import io.cucumber.core.exception.CucumberException;
import org.citrusframework.cucumber.CitrusLifecycleHooks;

/**
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
    }

    @Override
    public void loadGlue(Glue glue, GlueDiscoveryRequest request) {
        List<URI> gluePaths = request.getSelectorsByType(UriGlueDiscoverySelector.class).stream()
                .map(UriGlueDiscoverySelector::uri)
                .toList();

        loadGlue(glue, gluePaths);
    }

    /**
     * Registers the Citrus lifecycle hooks on the given glue.
     * @param glue
     * @param gluePaths
     * @deprecated Cucumber 8 discovers the glue paths via {@link #loadGlue(Glue, GlueDiscoveryRequest)}. This method
     * is only kept for backwards compatibility with subclasses overriding the former signature.
     */
    @Deprecated
    @Override
    public void loadGlue(Glue glue, List<URI> gluePaths) {
        try {
            if (container.addClass(CitrusLifecycleHooks.class)) {
                Method beforeMethod = CitrusLifecycleHooks.class.getMethod("before", Scenario.class);
                glue.addBeforeHook(new CitrusHookDefinition(beforeMethod, "", 10000, lookup, HookType.BEFORE));

                Method afterMethod = CitrusLifecycleHooks.class.getMethod("after", Scenario.class);
                glue.addAfterHook(new CitrusHookDefinition(afterMethod, "", 10000, lookup, HookType.AFTER));

                Method beforeSuiteMethod = CitrusLifecycleHooks.class.getMethod("beforeSuite");
                glue.addBeforeAllHook(new CitrusStaticHookDefinition(beforeSuiteMethod, CitrusLifecycleHooks.SUITE_HOOK_ORDER, StaticHookDefinition.HookType.BEFORE_ALL));

                Method afterSuiteMethod = CitrusLifecycleHooks.class.getMethod("afterSuite");
                glue.addAfterAllHook(new CitrusStaticHookDefinition(afterSuiteMethod, CitrusLifecycleHooks.SUITE_HOOK_ORDER, StaticHookDefinition.HookType.AFTER_ALL));
            }
        } catch (NoSuchMethodException e) {
            throw new CucumberException("Unable to add Citrus lifecycle hooks");
        }
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
