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
import com.consol.citrus.cucumber.CitrusReporter;
import com.consol.citrus.cucumber.container.StepTemplate;
import com.consol.citrus.cucumber.step.xml.XmlStepDefinition;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import cucumber.api.java.ObjectFactory;
import cucumber.runtime.*;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.java.spring.CitrusSpringObjectFactory;
import cucumber.runtime.snippets.FunctionNameGenerator;
import gherkin.formatter.model.Step;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class CitrusBackend implements Backend {

    /** Citrus instance used by all scenarios */
    private static Citrus citrus;

    /** Basic resource loader */
    private ResourceLoader resourceLoader;

    /** Glue holding all step definitions */
    private Glue glue;

    /** Glue paths */
    private List<String> gluePaths;

    /**
     * Constructor using resource loader.
     * @param resourceLoader
     */
    public CitrusBackend(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void loadGlue(Glue glue, List<String> gluePaths) {
        this.glue = glue;
        this.gluePaths = gluePaths;
    }

    @Override
    public void setUnreportedStepExecutor(UnreportedStepExecutor executor) {
    }

    @Override
    public void buildWorld() {
        if (glue != null) {
            getCitrus().beforeSuite(CitrusReporter.SUITE_NAME);

            for (String gluePath : gluePaths) {
                ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{"classpath*:" + gluePath.replaceAll("\\.", "/") + "/**/*Steps.xml"}, true, getCitrus().getApplicationContext());

                try {
                    for (StepTemplate stepTemplate : ctx.getBeansOfType(StepTemplate.class).values()) {
                        glue.addStepDefinition(new XmlStepDefinition(stepTemplate, getObjectFactory()));
                    }
                } catch (IllegalAccessException e) {
                    throw new CitrusRuntimeException("Failed to add XML step definition", e);
                }
            }

            glue = null;
        }
    }

    @Override
    public void disposeWorld() {
    }

    @Override
    public String getSnippet(Step step, FunctionNameGenerator functionNameGenerator) {
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
            throw new IllegalStateException("Unable to initialize Citrus instance - instance has already been initialized");
        }

        citrus = Citrus.newInstance(applicationContext);
    }
}
