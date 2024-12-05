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

package org.citrusframework.groovy.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActor;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.script.CreateBeansAction;
import org.citrusframework.script.CreateEndpointsAction;
import org.citrusframework.script.GroovyAction;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.StringUtils;

@XmlRootElement(name = "groovy")
public class Groovy implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    private AbstractTestActionBuilder<?, ?> builder;

    private String description;
    private String actor;

    private ReferenceResolver referenceResolver;

    @XmlElement
    public Groovy setDescription(String value) {
        this.description = value;
        return this;
    }

    @XmlAttribute(name = "actor")
    public Groovy setActor(String actor) {
        this.actor = actor;
        return this;
    }

    @XmlElement
    public Groovy setScript(Script script) {
        GroovyAction.Builder builder = new GroovyAction.Builder();

        if (StringUtils.hasText(script.getValue())) {
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
        return this;
    }

    @XmlElement
    public Groovy setEndpoints(Endpoints endpoints) {
        CreateEndpointsAction.Builder builder = new CreateEndpointsAction.Builder();

        if (StringUtils.hasText(endpoints.getScript().getValue())) {
            builder.script(endpoints.getScript().getValue());
        }

        if (endpoints.getScript().getFile() != null) {
            builder.scriptResourcePath(endpoints.getScript().getFile());
        }

        this.builder = builder;
        return this;
    }

    @XmlElement
    public Groovy setBeans(Beans beans) {
        CreateBeansAction.Builder builder = new CreateBeansAction.Builder();

        if (StringUtils.hasText(beans.getScript().getValue())) {
            builder.script(beans.getScript().getValue());
        }

        if (beans.getScript().getFile() != null) {
            builder.scriptResourcePath(beans.getScript().getFile());
        }

        this.builder = builder;
        return this;
    }

    @Override
    public TestAction build() {
        if (builder == null) {
            throw new CitrusRuntimeException("Missing Groovy action - please provide proper action details");
        }

        if (builder instanceof ReferenceResolverAware referenceResolverAware) {
            referenceResolverAware.setReferenceResolver(referenceResolver);
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

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "script"
    })
    public static class Endpoints {
        @XmlElement
        private Script script;

        public void setScript(Script script) {
            this.script = script;
        }

        public Script getScript() {
            return script;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "script"
    })
    public static class Beans {
        @XmlElement
        private Script script;

        public void setScript(Script script) {
            this.script = script;
        }

        public Script getScript() {
            return script;
        }
    }
}
