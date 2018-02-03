/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.mvn.plugin;

import com.consol.citrus.creator.*;
import com.consol.citrus.mvn.plugin.config.tests.TestConfiguration;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
@Mojo( name = "generate-test", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES)
public class GenerateTestMojo extends AbstractCitrusMojo {

    @Parameter(property = "citrus.skip.generate.test", defaultValue = "false")
    protected boolean skipGenerateTest;

    @Parameter(property = "citrus.build.directory", defaultValue= "${project.build.directory}/generated/citrus")
    protected String buildDirectory = "target/generated/citrus";

    private final XmlTestCreator xmlTestCreator;
    private final XsdXmlTestCreator xsdXmlTestCreator;
    private final WsdlXmlTestCreator wsdlXmlTestCreator;

    /**
     * Default constructor.
     */
    public GenerateTestMojo() {
        this(new XmlTestCreator(), new XsdXmlTestCreator(), new WsdlXmlTestCreator());
    }

    /**
     * Constructor using final fields.
     * @param xmlTestCreator
     * @param xsdXmlTestCreator
     * @param wsdlXmlTestCreator
     */
    public GenerateTestMojo(XmlTestCreator xmlTestCreator, XsdXmlTestCreator xsdXmlTestCreator, WsdlXmlTestCreator wsdlXmlTestCreator) {
        this.xmlTestCreator = xmlTestCreator;
        this.xsdXmlTestCreator = xsdXmlTestCreator;
        this.wsdlXmlTestCreator = wsdlXmlTestCreator;
    }

    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        if (skipGenerateTest) {
            return;
        }

        for (TestConfiguration test : getTests()) {
            if (test.getXsd() != null) {
                XsdXmlTestCreator creator = getXsdXmlTestCaseCreator();

                creator.withFramework(getFramework())
                        .withName(test.getName())
                        .withAuthor(test.getAuthor())
                        .withDescription(test.getDescription())
                        .usePackage(test.getPackageName())
                        .useSrcDirectory(buildDirectory);

                creator.withXsd(test.getXsd().getFile());
                creator.withRequestMessage(test.getXsd().getRequest());
                creator.withResponseMessage(test.getXsd().getResponse());

                creator.withEndpoint(test.getEndpoint());

                creator.withNameSuffix(test.getSuffix());

                creator.create();
            } else if (test.getWsdl() != null) {
                WsdlXmlTestCreator creator = getWsdlXmlTestCaseCreator();

                creator.withFramework(getFramework())
                        .withName(test.getName())
                        .withAuthor(test.getAuthor())
                        .withDescription(test.getDescription())
                        .usePackage(test.getPackageName())
                        .useSrcDirectory(buildDirectory);

                creator.withWsdl(test.getWsdl().getFile());
                creator.withOperation(test.getWsdl().getOperation());

                creator.withEndpoint(test.getEndpoint());

                creator.withNameSuffix(test.getSuffix());

                creator.create();
            } else {
                if (!StringUtils.hasText(test.getName())) {
                    throw new MojoExecutionException("Please provide proper test name! Test name must not be empty starting with uppercase letter!");
                }

                XmlTestCreator creator = getXmlTestCaseCreator()
                        .withFramework(getFramework())
                        .withName(test.getName())
                        .withAuthor(test.getAuthor())
                        .withDescription(test.getDescription())
                        .usePackage(test.getPackageName())
                        .useSrcDirectory(buildDirectory);

                creator.create();
                getLog().info("Successfully created new test case " + test.getPackageName() + "." + test.getName());
            }
        }
    }

    /**
     * Method provides test creator instance. Basically introduced for better mocking capabilities in unit tests but
     * also useful for subclasses to provide customized creator instance.
     * .
     * @return test creator.
     */
    public XmlTestCreator getXmlTestCaseCreator() {
        return Optional.ofNullable(xmlTestCreator).orElse(new XmlTestCreator());
    }

    /**
     * Method provides test creator instance. Basically introduced for better mocking capabilities in unit tests but
     * also useful for subclasses to provide customized creator instance.
     * .
     * @return test creator.
     */
    public WsdlXmlTestCreator getWsdlXmlTestCaseCreator() {
        return Optional.ofNullable(wsdlXmlTestCreator).orElse(new WsdlXmlTestCreator());
    }

    /**
     * Method provides test creator instance. Basically introduced for better mocking capabilities in unit tests but
     * also useful for subclasses to provide customized creator instance.
     * .
     * @return test creator.
     */
    public XsdXmlTestCreator getXsdXmlTestCaseCreator() {
        return Optional.ofNullable(xsdXmlTestCreator).orElse(new XsdXmlTestCreator());
    }
}
