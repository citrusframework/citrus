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
import org.citrusframework.actions.camel.CamelCliActionBuilderBase;
import org.citrusframework.camel.cli.CamelCli;
import org.citrusframework.camel.cli.CamelCliTestActor;
import org.citrusframework.context.TestContext;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.IsJsonPredicate;
import org.citrusframework.util.IsXmlPredicate;
import org.citrusframework.util.IsYamlPredicate;

/**
 * Abstract action to access Camel CLI tooling. Action provides common Camel CLI settings such as explicit Camel version.
 */
public abstract class AbstractCamelCliAction extends AbstractTestAction {

    private final String camelVersion;
    private final String kameletsVersion;

    private final CamelCli camelCli;

    protected AbstractCamelCliAction(String name, Builder<?, ?> builder) {
        super(name.startsWith("camel:") ? name : "camel:cli:" + name, builder);

        this.camelVersion = builder.camelVersion;
        this.kameletsVersion = builder.kameletsVersion;
        this.camelCli = builder.camelCli;
    }

    @Override
    public void execute(TestContext context) {
        if (camelVersion != null) {
            camelCli.withSystemProperty("camel.jbang.version", context.replaceDynamicContentInString(camelVersion));
        }

        if (kameletsVersion != null) {
            camelCli.withSystemProperty("camel-kamelets.version", context.replaceDynamicContentInString(kameletsVersion));
        }

        super.execute(context);
    }

    protected static String getFileExt(String sourceCode) {
        if (IsXmlPredicate.getInstance().test(sourceCode)) {
            return "xml";
        } else if (IsJsonPredicate.getInstance().test(sourceCode)) {
            return "json";
        } else if (sourceCode.contains("static void main(")) {
            return "java";
        } else if (sourceCode.contains("- from:") || sourceCode.contains("- route:") ||
                sourceCode.contains("- routeConfiguration:") || sourceCode.contains("- rest:") || sourceCode.contains("- beans:")) {
            return "yaml";
        } else if (sourceCode.contains("kind: Kamelet") || sourceCode.contains("kind: KameletBinding") ||
                sourceCode.contains("kind: Pipe") || sourceCode.contains("kind: Integration")) {
            return "yaml";
        } else if (IsYamlPredicate.getInstance().test(sourceCode)) {
            return "yaml";
        } else {
            return "groovy";
        }
    }

    /**
     * Provides access to the configured Camel CLI instance.
     */
    protected CamelCli camelCli() {
        return camelCli;
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
    public static abstract class Builder<T extends AbstractCamelCliAction, B extends Builder<T, B>> extends AbstractTestActionBuilder<T, B>
            implements CamelCliActionBuilderBase<T, B>, ReferenceResolverAware {

        protected CamelCli camelCli;
        protected String camelVersion;
        protected String kameletsVersion;

        protected ReferenceResolver referenceResolver;

        public Builder() {
            actor(new CamelCliTestActor());
        }

        @Override
        public B camelVersion(String camelVersion) {
            this.camelVersion = camelVersion;
            return self;
        }

        @Override
        public B kameletsVersion(String kameletsVersion) {
            this.kameletsVersion = kameletsVersion;
            return self;
        }

        @Override
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
            if (referenceResolver != null && referenceResolver.isResolvable(CamelCli.class)) {
                this.camelCli = referenceResolver.resolve(CamelCli.class);
            } else {
                camelCli = CamelCli.camel();
            }

            return doBuild();
        }

        /**
         * Subclasses need to implement to create the
         */
        protected abstract T doBuild();
    }
}
