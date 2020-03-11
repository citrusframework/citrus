/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.cucumber.backend;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Map;

import com.consol.citrus.Citrus;
import com.consol.citrus.CitrusInstanceManager;
import com.consol.citrus.CitrusInstanceProcessor;
import com.consol.citrus.CitrusSpringContext;
import com.consol.citrus.cucumber.CitrusLifecycleHooks;
import com.consol.citrus.cucumber.CitrusReporter;
import com.consol.citrus.cucumber.container.StepTemplate;
import com.consol.citrus.cucumber.step.xml.XmlStepDefinition;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.Snippet;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.resource.ClasspathSupport;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class CitrusBackend implements Backend {

    /** Citrus instance used by all scenarios */
    private static Citrus citrus;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CitrusBackend.class);

    /** Basic resource loader */
    private final Lookup lookup;
    private final Container container;

    /**
     * Constructor using resource loader.
     * @param lookup
     * @param container
     */
    public CitrusBackend(Lookup lookup, Container container) {
        this.lookup = lookup;
        this.container = container;

        CitrusInstanceManager.addInstanceProcessor(instance -> instance.beforeSuite(CitrusReporter.SUITE_NAME));
    }

    @Override
    public void loadGlue(Glue glue, List<URI> gluePaths) {
        if (citrus != null) {
            new XmlStepInstanceProcessor(glue, gluePaths, lookup).process(citrus);
        } else {
            CitrusInstanceManager.addInstanceProcessor(new XmlStepInstanceProcessor(glue, gluePaths, lookup));
        }

        try {
            if (!gluePaths.contains(getLifecycleHooksGluePath()) && container.addClass(CitrusLifecycleHooks.class)) {
                Method beforeMethod = CitrusLifecycleHooks.class.getMethod("before", Scenario.class);
                Before beforeAnnotation = beforeMethod.getAnnotation(Before.class);
                glue.addBeforeHook(new CitrusHookDefinition(beforeMethod, beforeAnnotation.value(), beforeAnnotation.order(), lookup));

                Method afterMethod = CitrusLifecycleHooks.class.getMethod("after", Scenario.class);
                After afterAnnotation = afterMethod.getAnnotation(After.class);
                glue.addAfterHook(new CitrusHookDefinition(afterMethod, afterAnnotation.value(), afterAnnotation.order(), lookup));
            }
        } catch (NoSuchMethodException e) {
            throw new CucumberException("Unable to add Citrus lifecylce hooks");
        }
    }

    /**
     * Helper to create proper URI pointing to {@link CitrusLifecycleHooks}.
     * @return
     */
    private static URI getLifecycleHooksGluePath() {
        return URI.create(ClasspathSupport.CLASSPATH_SCHEME_PREFIX +
                ClasspathSupport.resourceNameOfPackageName(CitrusLifecycleHooks.class.getPackage().getName()));
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

    /**
     * Provide access to the Citrus instance.
     * @return
     */
    public static Citrus getCitrus() {
        if (citrus == null) {
            citrus = Citrus.newInstance(CitrusSpringContext.create());
        }

        return citrus;
    }

    /**
     * Initializes Citrus instance with given application context.
     * @param applicationContext
     * @return
     */
    public static void initializeCitrus(ApplicationContext applicationContext) {
        if (citrus != null) {
            if (citrus.getCitrusContext() instanceof CitrusSpringContext &&
                    !((CitrusSpringContext) citrus.getCitrusContext()).getApplicationContext().equals(applicationContext)) {
                log.warn("Citrus instance has already been initialized - creating new instance and shutting down current instance");

                citrus.getCitrusContext().close();
            } else {
                return;
            }
        }

        citrus = Citrus.newInstance(CitrusSpringContext.create(applicationContext));
    }

    /**
     * Reset Citrus instance. Use this method with special care. Usually Citrus instance should only be instantiated
     * once throughout the whole test suite run.
     */
    public static void resetCitrus() {
        citrus = null;
    }

    /**
     * Initialization hook performs before suite actions and XML step initialization. Called as soon as citrus instance is requested
     * from outside for the first time. Performs only once.
     */
    private static class XmlStepInstanceProcessor implements CitrusInstanceProcessor {

        private final Glue glue;
        private final List<URI> gluePaths;
        private final Lookup lookup;

        XmlStepInstanceProcessor(Glue glue, List<URI> gluePaths, Lookup lookup) {
            this.glue = glue;
            this.gluePaths = gluePaths;
            this.lookup = lookup;
        }

        @Override
        public void process(Citrus instance) {
            for (URI gluePath : gluePaths) {
                String xmlStepConfigLocation = "classpath*:" + ClasspathSupport.resourceNameOfPackageName(ClasspathSupport.packageName(gluePath)) + "/**/*Steps.xml";

                log.info(String.format("Loading XML step definitions %s", xmlStepConfigLocation));

                ApplicationContext ctx;
                if (instance.getCitrusContext() instanceof CitrusSpringContext) {
                    ctx = new ClassPathXmlApplicationContext(new String[]{ xmlStepConfigLocation }, true, ((CitrusSpringContext) instance.getCitrusContext()).getApplicationContext());
                } else {
                    ctx = new ClassPathXmlApplicationContext(new String[]{ xmlStepConfigLocation }, true);
                }

                Map<String, StepTemplate> xmlSteps = ctx.getBeansOfType(StepTemplate.class);

                for (StepTemplate stepTemplate : xmlSteps.values()) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Found XML step definition: %s %s", stepTemplate.getName(), stepTemplate.getPattern().pattern()));
                    }
                    glue.addStepDefinition(new XmlStepDefinition(stepTemplate, lookup));
                }
            }
        }
    }
}
