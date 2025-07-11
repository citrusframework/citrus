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

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.camel.jbang.CamelJBang;
import org.citrusframework.camel.jbang.CamelJBangTestActor;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

/**
 * Abstract action to access Camel JBang tooling. Action provides common Camel JBang settings such as explicit Camel version.
 */
public abstract class AbstractCamelJBangAction extends AbstractTestAction {

    private final String camelVersion;
    private final String kameletsVersion;

    private final CamelJBang camelJBang;

    protected AbstractCamelJBangAction(String name, Builder<?, ?> builder) {
        super(name.startsWith("camel") ? name : "camel-" + name, builder);

        this.camelVersion = builder.camelVersion;
        this.kameletsVersion = builder.kameletsVersion;
        this.camelJBang = builder.camelJBang;

        if (camelVersion != null) {
            camelJBang.withSystemProperty("camel.jbang.version", camelVersion);
        }

        if (kameletsVersion != null) {
            camelJBang.withSystemProperty("camel-kamelets.version", camelVersion);
        }
    }

    /**
     * Provides access to the configured Camel JBang instance.
     * @return
     */
    protected CamelJBang camelJBang() {
        return camelJBang;
    }

    public String getCamelVersion() {
        return camelVersion;
    }

    public String getKameletsVersion() {
        return kameletsVersion;
    }

    /**
     * Action builder.
     */
    public static abstract class Builder<T extends AbstractCamelJBangAction, B extends Builder<T, B>> extends AbstractTestActionBuilder<T, B> implements ReferenceResolverAware {

        protected CamelJBang camelJBang;
        protected String camelVersion;
        protected String kameletsVersion;

        protected ReferenceResolver referenceResolver;

        public Builder() {
            actor(new CamelJBangTestActor());
        }

        /**
         * Sets explicit Camel version.
         * @param camelVersion
         * @return
         */
        public B camelVersion(String camelVersion) {
            this.camelVersion = camelVersion;
            return self;
        }

        /**
         * Sets explicit Kamelets version.
         * @param kameletsVersion
         * @return
         */
        public B kameletsVersion(String kameletsVersion) {
            this.kameletsVersion = kameletsVersion;
            return self;
        }

        public B withReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
            return self;
        }

        @Override
        public void setReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
        }

        @Override
        public final T build() {
            if (referenceResolver != null && referenceResolver.isResolvable(CamelJBang.class)) {
                this.camelJBang = referenceResolver.resolve(CamelJBang.class);
            } else {
                camelJBang = CamelJBang.camel();
            }

            return doBuild();
        }

        /**
         * Subclasses need to implement to create the
         * @return
         */
        protected abstract T doBuild();
    }
}
