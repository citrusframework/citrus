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

package org.citrusframework.groovy.yaml;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActionContainerBuilder;
import org.citrusframework.TestActor;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.script.CreateBeansAction;
import org.citrusframework.script.CreateEndpointsAction;
import org.citrusframework.script.GroovyAction;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.yaml.SchemaProperty;

public class Groovy implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    private AbstractTestActionBuilder<?, ?> builder;

    private String description;
    private String actor;

    private ReferenceResolver referenceResolver;

    @SchemaProperty(advanced = true, description = "Test action description printed when the action is executed.")
    public void setDescription(String value) {
        this.description = value;
    }

    @SchemaProperty(advanced = true)
    public void setActor(String actor) {
        this.actor = actor;
    }

    @SchemaProperty(required = true, description = "The Groovy script to execute.")
    public void setScript(Script script) {
        GroovyAction.Builder builder = new GroovyAction.Builder();

        if (script.getValue() != null) {
            builder.script(script.getValue());
        }

        if (script.getFile() != null) {
            builder.scriptResourcePath(script.getFile());
        }

        if (script.getTemplate() != null) {
            builder.template(script.getTemplate());
        }

        builder.useScriptTemplate(script.isUseScriptTemplate());

        this.builder = builder;
    }

    @SchemaProperty(advanced = true, description = "Script that creates endpoints.")
    public void setEndpoints(Script script) {
        CreateEndpointsAction.Builder builder = new CreateEndpointsAction.Builder();

        if (script.getValue() != null) {
            builder.script(script.getValue());
        }

        if (script.getFile() != null) {
            builder.scriptResourcePath(script.getFile());
        }

        this.builder = builder;
    }

    @SchemaProperty(advanced = true, description = "Script that creates beans in the bean registry.")
    public void setBeans(Script script) {
        CreateBeansAction.Builder builder = new CreateBeansAction.Builder();

        if (script.getValue() != null) {
            builder.script(script.getValue());
        }

        if (script.getFile() != null) {
            builder.scriptResourcePath(script.getFile());
        }

        this.builder = builder;
    }

    @Override
    public TestAction build() {
        if (builder == null) {
            throw new CitrusRuntimeException("Missing Groovy action - please provide proper action details");
        }

        if (builder instanceof TestActionContainerBuilder<?,?>) {
            ((TestActionContainerBuilder<?,?>) builder).getActions().stream()
                    .filter(action -> action instanceof ReferenceResolverAware)
                    .forEach(action -> ((ReferenceResolverAware) action).setReferenceResolver(referenceResolver));
        }

        if (builder instanceof ReferenceResolverAware) {
            ((ReferenceResolverAware) builder).setReferenceResolver(referenceResolver);
        }

        builder.description(description);

        if (referenceResolver != null) {
            if (actor != null) {
                builder.actor(referenceResolver.resolve(actor, TestActor.class));
            }
        }

        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
