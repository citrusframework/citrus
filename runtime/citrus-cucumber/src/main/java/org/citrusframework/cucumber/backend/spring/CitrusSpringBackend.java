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

package org.citrusframework.cucumber.backend.spring;

import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.resource.ClasspathSupport;
import org.citrusframework.Citrus;
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.CitrusInstanceProcessor;
import org.citrusframework.CitrusSpringContext;
import org.citrusframework.cucumber.backend.CitrusBackend;
import org.citrusframework.cucumber.container.StepTemplate;
import org.citrusframework.cucumber.step.xml.XmlStepDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class CitrusSpringBackend extends CitrusBackend {

    private static final Logger logger = LoggerFactory.getLogger(CitrusSpringBackend.class);

    /**
     * Constructor using resource loader.
     */
    public CitrusSpringBackend(Lookup lookup, Container container) {
        super(lookup, container);
    }

    @Override
    public void loadGlue(Glue glue, List<URI> gluePaths) {
        if (CitrusInstanceManager.hasInstance()) {
            new XmlStepInstanceProcessor(glue, gluePaths, lookup).process(CitrusInstanceManager.getOrDefault());
        } else {
            CitrusInstanceManager.addInstanceProcessor(new XmlStepInstanceProcessor(glue, gluePaths, lookup));
        }

        super.loadGlue(glue, gluePaths);
    }

    /**
     * Initialization hook performs before suite actions and XML step initialization. Called as soon as citrus instance is requested
     * from outside for the first time. Performs only once.
     */
    private record XmlStepInstanceProcessor(Glue glue, List<URI> gluePaths,
                                            Lookup lookup) implements CitrusInstanceProcessor {

        @Override
        public void process(Citrus instance) {
            for (URI gluePath : gluePaths) {
                String xmlStepConfigLocation = "classpath*:" + ClasspathSupport.resourceNameOfPackageName(ClasspathSupport.packageName(gluePath)) + "/**/*Steps.xml";
                logger.info("Loading XML step definitions {}", xmlStepConfigLocation);

                ApplicationContext ctx;
                if (instance.getCitrusContext() instanceof CitrusSpringContext) {
                    ctx = new ClassPathXmlApplicationContext(new String[]{xmlStepConfigLocation}, true, ((CitrusSpringContext) instance.getCitrusContext()).getApplicationContext());
                } else {
                    ctx = new ClassPathXmlApplicationContext(new String[]{xmlStepConfigLocation}, true);
                }

                Map<String, StepTemplate> xmlSteps = ctx.getBeansOfType(StepTemplate.class);

                for (StepTemplate stepTemplate : xmlSteps.values()) {
                    if (logger.isDebugEnabled()) {
                        logger.info("Loading XML step definitions {}", xmlStepConfigLocation);
                    }
                    glue.addStepDefinition(new XmlStepDefinition(stepTemplate, lookup));
                }
            }
        }
    }
}
