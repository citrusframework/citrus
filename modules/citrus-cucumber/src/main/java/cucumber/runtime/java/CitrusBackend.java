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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class CitrusBackend implements Backend {

    /** Citrus instance used by all scenarios */
    private static Citrus citrus;
    private static InitializationHook initializationHook;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CitrusBackend.class);

    /** Basic resource loader */
    private ResourceLoader resourceLoader;

    /**
     * Constructor using resource loader.
     * @param resourceLoader
     */
    public CitrusBackend(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void loadGlue(Glue glue, List<String> gluePaths) {
        try {
            initializationHook = new InitializationHook(glue, gluePaths, getObjectFactory());
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
    public void setUnreportedStepExecutor(UnreportedStepExecutor executor) {
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

            if (initializationHook != null) {
                initializationHook.run();
            }
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

        if (initializationHook != null) {
            initializationHook.run();
        }
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
    private static class InitializationHook implements Runnable {

        private final Glue glue;
        private final List<String> gluePaths;
        private final ObjectFactory objectFactory;

        private boolean performed = false;

        InitializationHook(Glue glue, List<String> gluePaths, ObjectFactory objectFactory) {
            this.glue = glue;
            this.gluePaths = gluePaths;
            this.objectFactory = objectFactory;
        }

        @Override
        public void run() {
            if (!performed) {
                getCitrus().beforeSuite(CitrusReporter.SUITE_NAME);

                for (String gluePath : gluePaths) {
                    ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{"classpath*:" + gluePath.replaceAll("\\.", "/").replaceAll("^classpath:", "") + "/**/*Steps.xml"}, true, getCitrus().getApplicationContext());

                    for (StepTemplate stepTemplate : ctx.getBeansOfType(StepTemplate.class).values()) {
                        glue.addStepDefinition(new XmlStepDefinition(stepTemplate, objectFactory));
                    }
                }

                performed = true;
            }
        }
    }
}
