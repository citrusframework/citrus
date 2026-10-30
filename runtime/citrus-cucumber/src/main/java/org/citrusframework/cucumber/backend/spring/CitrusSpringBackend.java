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

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.discovery.GlueDiscoveryRequest;
import io.cucumber.core.backend.discovery.UriGlueDiscoverySelector;
import io.cucumber.core.resource.ClasspathSupport;
import org.citrusframework.Citrus;
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.CitrusInstanceProcessor;
import org.citrusframework.spring.CitrusSpringContext;
import org.citrusframework.cucumber.backend.CitrusBackend;
import org.citrusframework.cucumber.container.StepTemplate;
import org.citrusframework.cucumber.step.xml.XmlStepDefinition;
import org.citrusframework.cucumber.step.xml.XmlSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CitrusSpringBackend extends CitrusBackend {

    private static final Logger logger = LoggerFactory.getLogger(CitrusSpringBackend.class);

    /** XML step definitions waiting for the Citrus instance, loaded on the runner thread */
    private volatile XmlStepInstanceProcessor pendingXmlSteps;

    /**
     * Constructor using resource loader.
     */
    public CitrusSpringBackend(Lookup lookup, Container container) {
        super(lookup, container);
    }

    @Override
    public void loadGlue(Glue glue, GlueDiscoveryRequest request) {
        List<URI> gluePaths = request.getSelectorsByType(UriGlueDiscoverySelector.class).stream()
                .map(UriGlueDiscoverySelector::uri)
                .toList();

        // XML step definitions run on XmlSteps, so object factories that only create registered glue classes need it
        container.addClass(XmlSteps.class);

        XmlStepInstanceProcessor xmlSteps = new XmlStepInstanceProcessor(glue, gluePaths, lookup);
        Optional<Citrus> citrus = CitrusInstanceManager.get();
        if (citrus.isPresent()) {
            xmlSteps.process(citrus.get());
        } else {
            logger.debug("No Citrus instance yet, deferring XML step definitions until buildWorld");
            pendingXmlSteps = xmlSteps;
        }

        super.loadGlue(glue, request);
    }

    /**
     * Loads pending XML step definitions as soon as the object factory has created the Citrus instance.
     * Cucumber calls this on the runner thread after starting the object factory and before preparing the glue,
     * so each runner adds its XML steps to its own glue, also when scenarios run in parallel.
     */
    @Override
    public void buildWorld() {
        XmlStepInstanceProcessor pending = pendingXmlSteps;
        if (pending != null) {
            Optional<Citrus> citrus = CitrusInstanceManager.get();
            if (citrus.isPresent()) {
                synchronized (this) {
                    if (pendingXmlSteps != null) {
                        pendingXmlSteps.process(citrus.get());
                        pendingXmlSteps = null;
                    }
                }
            } else {
                logger.debug("No Citrus instance yet, keeping deferred XML step definitions for the next buildWorld");
            }
        }

        super.buildWorld();
    }

    /**
     * Loads the XML step definitions found in the glue paths into the glue.
     */
    private record XmlStepInstanceProcessor(Glue glue, List<URI> gluePaths,
                                            Lookup lookup) implements CitrusInstanceProcessor {

        @Override
        public void process(Citrus instance) {
            for (URI gluePath : gluePaths) {
                String xmlStepConfigLocation = "classpath*:" + ClasspathSupport.resourceNameOfPackageName(ClasspathSupport.packageName(gluePath)) + "/**/*Steps.xml";
                logger.debug("Loading XML step definitions {}", xmlStepConfigLocation);

                ApplicationContext ctx;
                if (instance.getCitrusContext() instanceof CitrusSpringContext) {
                    ctx = new ClassPathXmlApplicationContext(new String[]{xmlStepConfigLocation}, true, ((CitrusSpringContext) instance.getCitrusContext()).getApplicationContext());
                } else {
                    ctx = new ClassPathXmlApplicationContext(new String[]{xmlStepConfigLocation}, true);
                }

                Map<String, StepTemplate> xmlSteps = ctx.getBeansOfType(StepTemplate.class);

                for (StepTemplate stepTemplate : xmlSteps.values()) {
                    logger.debug("Loading XML step definition: {}", stepTemplate.getName());
                    glue.addStepDefinition(new XmlStepDefinition(stepTemplate, lookup));
                }
            }
        }
    }
}
