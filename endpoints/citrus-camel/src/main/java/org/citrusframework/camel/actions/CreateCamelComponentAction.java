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

package org.citrusframework.camel.actions;

import java.io.IOException;

import org.citrusframework.camel.context.CamelReferenceResolver;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.groovy.dsl.GroovySupport;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;

/**
 * Creates component and binds it to the given Camel context.
 */
public class CreateCamelComponentAction extends AbstractCamelAction implements ReferenceResolverAware {

    private final String name;
    private final Object component;
    private final String script;

    protected CreateCamelComponentAction(Builder builder) {
        super("create-component", builder);

        this.name = builder.name;
        this.component = builder.component;
        this.script = builder.script;
    }

    @Override
    public void doExecute(TestContext context) {
        Object toCreate;
        if (script != null) {
            toCreate = new GroovySupport()
                    .withTestContext(context)
                    .load(context.replaceDynamicContentInString(script), "org.apache.camel.*");
        } else {
            toCreate = component;
        }

        bindComponent(context.replaceDynamicContentInString(name), toCreate);
    }

    private void bindComponent(String name, Object component) {
        if (referenceResolver instanceof CamelReferenceResolver camelReferenceResolver) {
            camelReferenceResolver.bind(name, component);
        } else {
            camelContext.getRegistry().bind(name, component);
            referenceResolver.bind(name, component);
        }
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractCamelAction.Builder<CreateCamelComponentAction, Builder> {

        private String name;
        private Object component;
        private String script;

        /**
         * Static entry method for the fluent API.
         * @return
         */
        public static Builder bind() {
            return new Builder();
        }

        public Builder component(String name, Object component) {
            this.name = name;
            this.component = component;
            return this;
        }

        public Builder component(Resource resource) {
            return component(FileUtils.getBaseName(FileUtils.getFileName(resource.getLocation())), resource);
        }

        public Builder component(String name, Resource resource) {
            this.name = name;
            try {
                this.script = FileUtils.readToString(resource);
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read Camel component from resource '%s'".formatted(resource.getLocation()), e);
            }
            return this;
        }

        @Override
        public CreateCamelComponentAction doBuild() {
            return new CreateCamelComponentAction(this);
        }
    }
}
