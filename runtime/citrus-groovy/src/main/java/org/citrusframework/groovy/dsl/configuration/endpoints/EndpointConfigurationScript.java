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

package org.citrusframework.groovy.dsl.configuration.endpoints;

import org.citrusframework.Citrus;
import org.citrusframework.CitrusContext;
import org.citrusframework.common.InitializingPhase;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.groovy.dsl.GroovyShellUtils;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.PropertyUtils;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

public class EndpointConfigurationScript implements ReferenceResolverAware {

    private ReferenceResolver referenceResolver;

    private final String script;

    public EndpointConfigurationScript(String script, Citrus citrus) {
        this(script, citrus.getCitrusContext());
    }

    public EndpointConfigurationScript(String script, CitrusContext citrusContext) {
        this(script, citrusContext.getReferenceResolver());
    }

    public EndpointConfigurationScript(String script, ReferenceResolver referenceResolver) {
        this.script = script;
        this.referenceResolver = referenceResolver;
    }

    public EndpointConfigurationScript(String script) {
        this.script = script;
    }

    public void execute(TestContext context) {
        EndpointsConfiguration configuration = new EndpointsConfiguration();
        ImportCustomizer ic = new ImportCustomizer();
        GroovyShellUtils.run(ic, configuration, context.replaceDynamicContentInString(script), null, context);

        configuration.getEndpoints().forEach(endpoint -> {
            ReferenceResolver resolverToUse = referenceResolver;
            if (resolverToUse == null) {
                resolverToUse = context.getReferenceResolver();
            }

            if (endpoint instanceof ReferenceResolverAware referenceResolverAware) {
                referenceResolverAware.setReferenceResolver(resolverToUse);
            }

            if (endpoint instanceof InitializingPhase initializingBean) {
                initializingBean.initialize();
            }

            PropertyUtils.configure(endpoint.getName(), endpoint, resolverToUse);

            onCreate(endpoint);

            resolverToUse.bind(endpoint.getName(), endpoint);
        });
    }

    /**
     * Subclasses may add custom endpoint configuration logic here.
     * @param endpoint
     */
    protected void onCreate(Endpoint endpoint) {
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
