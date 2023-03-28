/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.groovy.dsl.configuration.endpoints;

import org.citrusframework.Citrus;
import org.citrusframework.common.InitializingPhase;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.groovy.dsl.GroovyShellUtils;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

/**
 * @author Christoph Deppisch
 */
public class EndpointConfigurationScript {

    private final Citrus citrus;

    private final String script;

    public EndpointConfigurationScript(String script, Citrus citrus) {
        this.script = script;
        this.citrus = citrus;
    }

    public void execute(TestContext context) {
        EndpointsConfiguration configuration = new EndpointsConfiguration();
        ImportCustomizer ic = new ImportCustomizer();
        GroovyShellUtils.run(ic, configuration, context.replaceDynamicContentInString(script), citrus, context);

        configuration.getEndpoints().forEach(endpoint -> {
            onCreate(endpoint);
            if (endpoint instanceof InitializingPhase) {
                ((InitializingPhase) endpoint).initialize();
            }
            citrus.getCitrusContext().bind(endpoint.getName(), endpoint);
        });
    }

    /**
     * Subclasses may add custom endpoint configuration logic here.
     * @param endpoint
     */
    protected void onCreate(Endpoint endpoint) {
    }

}
