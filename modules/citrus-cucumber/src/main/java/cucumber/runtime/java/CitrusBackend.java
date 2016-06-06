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
import com.consol.citrus.cucumber.xml.XmlStepDefinition;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import cucumber.runtime.*;
import cucumber.runtime.io.ResourceLoader;
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
    private static Citrus citrus = Citrus.newInstance();

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
        citrus.beforeSuite(CitrusReporter.SUITE_NAME);

        for (String gluePath : gluePaths) {
            ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] { "classpath*:" + gluePath.replaceAll("\\.", "/") + "/**/*Steps.xml" }, true, citrus.getApplicationContext());

            try {
                for (StepTemplate stepTemplate : ctx.getBeansOfType(StepTemplate.class).values()) {
                    glue.addStepDefinition(new XmlStepDefinition(stepTemplate, CitrusObjectFactory.instance()));
                }
            } catch (IllegalAccessException e) {
                throw new CitrusRuntimeException("Failed to add XML step definition", e);
            }
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
    public String getSnippet(Step step, FunctionNameGenerator functionNameGenerator) {
        return "";
    }

    /**
     * Provide access to the Citrus instance.
     * @return
     */
    public static Citrus getCitrus() {
        return citrus;
    }
}
