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

package cucumber.runtime.java;

import com.consol.citrus.Citrus;
import com.consol.citrus.cucumber.CitrusLifecycleHooks;
import com.consol.citrus.cucumber.CitrusReporter;
import com.consol.citrus.cucumber.container.StepTemplate;
import com.consol.citrus.cucumber.step.xml.XmlStepDefinition;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import cucumber.api.Scenario;
import cucumber.api.java.*;
import cucumber.runtime.*;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.java.spring.CitrusSpringObjectFactory;
import cucumber.runtime.snippets.FunctionNameGenerator;
import gherkin.pickles.PickleStep;
import io.cucumber.stepexpression.TypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

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
    private ResourceLoader resourceLoader;
    private TypeRegistry typeRegistry;

    /**
     * Constructor using resource loader.
     * @param resourceLoader
     */
    public CitrusBackend(ResourceLoader resourceLoader, TypeRegistry typeRegistry) {
        this.resourceLoader = resourceLoader;
        this.typeRegistry = typeRegistry;

        Citrus.CitrusInstanceManager.addInstanceProcessor(instance -> instance.beforeSuite(CitrusReporter.SUITE_NAME));
    }

    @Override
    public void loadGlue(Glue glue, List<String> gluePaths) {
        try {
            Citrus.CitrusInstanceManager.addInstanceProcessor(new XmlStepInstanceProcessor(glue, gluePaths, getObjectFactory(), typeRegistry));
        } catch (IllegalAccessException e) {
            throw new CitrusRuntimeException("Failed to add XML step definition", e);
        }

        try {
            if (!gluePaths.contains(CitrusLifecycleHooks.class.getPackage().getName()) && getObjectFactory().addClass(CitrusLifecycleHooks.class)) {
                Method beforeMethod = CitrusLifecycleHooks.class.getMethod("before", Scenario.class);
                Before beforeAnnotation = beforeMethod.getAnnotation(Before.class);
                glue.addBeforeHook(new JavaHookDefinition(beforeMethod, beforeAnnotation.value(), beforeAnnotation.order(), beforeAnnotation.timeout(), getObjectFactory()));

                Method afterMethod = CitrusLifecycleHooks.class.getMethod("after", Scenario.class);
                After afterAnnotation = afterMethod.getAnnotation(After.class);
                glue.addAfterHook(new JavaHookDefinition(afterMethod, afterAnnotation.value(), afterAnnotation.order(), afterAnnotation.timeout(), getObjectFactory()));
            }
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new CucumberException("Unable to add Citrus lifecylce hooks");
        }
    }

    @Override
    public void buildWorld() {
    }

    @Override
    public void disposeWorld() {
    }

    @Override
    public String getSnippet(PickleStep step, String keyword, FunctionNameGenerator functionNameGenerator) {
        return "";
    }

    /**
     * Gets the object factory instance that is configured in environment.
     * @return
     */
    private ObjectFactory getObjectFactory() throws IllegalAccessException {
        if (Env.INSTANCE.get(ObjectFactory.class.getName()).equals(CitrusObjectFactory.class.getName())) {
            return CitrusObjectFactory.instance();
        } else if (Env.INSTANCE.get(ObjectFactory.class.getName()).equals(CitrusSpringObjectFactory.class.getName())) {
            return CitrusSpringObjectFactory.instance();
        }

        return ObjectFactoryLoader.loadObjectFactory(new ResourceLoaderClassFinder(resourceLoader, Thread.currentThread().getContextClassLoader()),
                Env.INSTANCE.get(ObjectFactory.class.getName()));
    }

    /**
     * Provide access to the Citrus instance.
     * @return
     */
    public static Citrus getCitrus() {
        if (citrus == null) {
            citrus = Citrus.newInstance();
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
            if (!citrus.getApplicationContext().equals(applicationContext)) {
                log.warn("Citrus instance has already been initialized - creating new instance and shutting down current instance");

                if (citrus.getApplicationContext() instanceof ConfigurableApplicationContext) {
                    ((ConfigurableApplicationContext) citrus.getApplicationContext()).stop();
                }
            } else {
                return;
            }
        }

        citrus = Citrus.newInstance(applicationContext);
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
    private static class XmlStepInstanceProcessor implements Citrus.InstanceProcessor {

        private final Glue glue;
        private final List<String> gluePaths;
        private final ObjectFactory objectFactory;
        private TypeRegistry typeRegistry;

        XmlStepInstanceProcessor(Glue glue, List<String> gluePaths, ObjectFactory objectFactory, TypeRegistry typeRegistry) {
            this.glue = glue;
            this.gluePaths = gluePaths;
            this.objectFactory = objectFactory;
            this.typeRegistry = typeRegistry;
        }

        @Override
        public void process(Citrus instance) {
            for (String gluePath : gluePaths) {
                String xmlStepConfigLocation = "classpath*:" + gluePath.replaceAll("\\.", "/").replaceAll("^classpath:", "") + "/**/*Steps.xml";

                log.info(String.format("Loading XML step definitions %s", xmlStepConfigLocation));

                ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{ xmlStepConfigLocation }, true, instance.getApplicationContext());

                Map<String, StepTemplate> xmlSteps = ctx.getBeansOfType(StepTemplate.class);

                for (StepTemplate stepTemplate : xmlSteps.values()) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Found XML step definition: %s %s", stepTemplate.getName(), stepTemplate.getPattern().pattern()));
                    }
                    glue.addStepDefinition(new XmlStepDefinition(stepTemplate, objectFactory, typeRegistry));
                }
            }
        }
    }
}
