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

package org.citrusframework.mvn.plugin;

import org.citrusframework.generate.*;
import org.citrusframework.generate.javadsl.*;
import org.citrusframework.generate.xml.*;
import org.citrusframework.mvn.plugin.config.tests.TestConfiguration;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
@Mojo( name = "generate-tests", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES)
public class GenerateTestMojo extends AbstractCitrusMojo {

    @Parameter(property = "citrus.skip.generate.test", defaultValue = "false")
    protected boolean skipGenerateTest;

    @Parameter(property = "citrus.build.directory", defaultValue= "${project.build.directory}/generated/citrus")
    protected String buildDirectory = "target/generated/citrus";

    private final XmlTestGenerator xmlTestGenerator;
    private final XsdXmlTestGenerator xsdXmlTestGenerator;
    private final WsdlXmlTestGenerator wsdlXmlTestGenerator;
    private final SwaggerXmlTestGenerator swaggerXmlTestGenerator;

    private final JavaDslTestGenerator javaTestGenerator;
    private final XsdJavaTestGenerator xsdJavaTestGenerator;
    private final WsdlJavaTestGenerator wsdlJavaTestGenerator;
    private final SwaggerJavaTestGenerator swaggerJavaTestGenerator;

    /**
     * Default constructor.
     */
    public GenerateTestMojo() {
        this(new XmlTestGenerator(),
                new XsdXmlTestGenerator(),
                new WsdlXmlTestGenerator(),
                new SwaggerXmlTestGenerator(),
                new JavaDslTestGenerator(),
                new XsdJavaTestGenerator(),
                new WsdlJavaTestGenerator(),
                new SwaggerJavaTestGenerator());
    }

    /**
     * Constructor using final fields.
     * @param xmlTestGenerator
     * @param xsdXmlTestGenerator
     * @param wsdlXmlTestGenerator
     * @param swaggerXmlTestGenerator
     * @param javaTestGenerator
     * @param xsdJavaTestGenerator
     * @param wsdlJavaTestGenerator
     * @param swaggerJavaTestGenerator
     */
    public GenerateTestMojo(XmlTestGenerator xmlTestGenerator,
                          XsdXmlTestGenerator xsdXmlTestGenerator,
                          WsdlXmlTestGenerator wsdlXmlTestGenerator,
                          SwaggerXmlTestGenerator swaggerXmlTestGenerator,
                          JavaDslTestGenerator javaTestGenerator,
                          XsdJavaTestGenerator xsdJavaTestGenerator,
                          WsdlJavaTestGenerator wsdlJavaTestGenerator,
                          SwaggerJavaTestGenerator swaggerJavaTestGenerator) {
        this.xmlTestGenerator = xmlTestGenerator;
        this.xsdXmlTestGenerator = xsdXmlTestGenerator;
        this.wsdlXmlTestGenerator = wsdlXmlTestGenerator;
        this.swaggerXmlTestGenerator = swaggerXmlTestGenerator;
        this.javaTestGenerator = javaTestGenerator;
        this.xsdJavaTestGenerator = xsdJavaTestGenerator;
        this.wsdlJavaTestGenerator = wsdlJavaTestGenerator;
        this.swaggerJavaTestGenerator = swaggerJavaTestGenerator;
    }

    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        if (skipGenerateTest) {
            return;
        }

        for (TestConfiguration test : getTests()) {
            if (test.getXsd() != null) {
                XsdTestGenerator generator = getXsdTestGenerator();

                generator.withFramework(getFramework())
                        .withName(test.getName())
                        .withAuthor(test.getAuthor())
                        .withDescription(test.getDescription())
                        .usePackage(test.getPackageName())
                        .useSrcDirectory(buildDirectory);

                generator.withDisabled(test.isDisabled());
                generator.withMode(TestGenerator.GeneratorMode.valueOf(test.getXsd().getMode()));
                generator.withXsd(test.getXsd().getFile());
                generator.withRequestMessage(test.getXsd().getRequest());
                generator.withResponseMessage(test.getXsd().getResponse());

                if (test.getXsd().getMappings() != null) {
                    generator.withInboundMappings(test.getXsd().getMappings().getInbound());
                    generator.withOutboundMappings(test.getXsd().getMappings().getOutbound());
                    generator.withInboundMappingFile(test.getXsd().getMappings().getInboundFile());
                    generator.withOutboundMappingFile(test.getXsd().getMappings().getOutboundFile());
                }
                
                generator.withEndpoint(test.getEndpoint());

                generator.withNameSuffix(test.getSuffix());

                generator.create();
            } else if (test.getWsdl() != null) {
                WsdlTestGenerator generator = getWsdlTestGenerator();

                generator.withFramework(getFramework())
                        .withName(test.getName())
                        .withAuthor(test.getAuthor())
                        .withDescription(test.getDescription())
                        .usePackage(test.getPackageName())
                        .useSrcDirectory(buildDirectory);

                generator.withDisabled(test.isDisabled());
                generator.withMode(TestGenerator.GeneratorMode.valueOf(test.getWsdl().getMode()));
                generator.withWsdl(test.getWsdl().getFile());
                generator.withOperation(test.getWsdl().getOperation());

                if (test.getWsdl().getMappings() != null) {
                    generator.withInboundMappings(test.getWsdl().getMappings().getInbound());
                    generator.withOutboundMappings(test.getWsdl().getMappings().getOutbound());
                    generator.withInboundMappingFile(test.getWsdl().getMappings().getInboundFile());
                    generator.withOutboundMappingFile(test.getWsdl().getMappings().getOutboundFile());
                }

                generator.withEndpoint(test.getEndpoint());

                generator.withNameSuffix(test.getSuffix());

                generator.create();
            } else if (test.getSwagger() != null) {
                SwaggerTestGenerator generator = getSwaggerTestGenerator();

                generator.withFramework(getFramework())
                        .withName(test.getName())
                        .withAuthor(test.getAuthor())
                        .withDescription(test.getDescription())
                        .usePackage(test.getPackageName())
                        .useSrcDirectory(buildDirectory);

                generator.withDisabled(test.isDisabled());
                generator.withMode(TestGenerator.GeneratorMode.valueOf(test.getSwagger().getMode()));
                generator.withSpec(test.getSwagger().getFile());
                generator.withOperation(test.getSwagger().getOperation());


                if (test.getSwagger().getMappings() != null) {
                    generator.withInboundMappings(test.getSwagger().getMappings().getInbound());
                    generator.withOutboundMappings(test.getSwagger().getMappings().getOutbound());
                    generator.withInboundMappingFile(test.getSwagger().getMappings().getInboundFile());
                    generator.withOutboundMappingFile(test.getSwagger().getMappings().getOutboundFile());
                }

                generator.withEndpoint(test.getEndpoint());

                generator.withNameSuffix(test.getSuffix());

                generator.create();
            } else {
                if (!StringUtils.hasText(test.getName())) {
                    throw new MojoExecutionException("Please provide proper test name! Test name must not be empty starting with uppercase letter!");
                }

                if (getType().equals("java")) {
                    JavaDslTestGenerator generator = (JavaDslTestGenerator) getJavaTestGenerator()
                            .withDisabled(test.isDisabled())
                            .withFramework(getFramework())
                            .withName(test.getName())
                            .withAuthor(test.getAuthor())
                            .withDescription(test.getDescription())
                            .usePackage(test.getPackageName())
                            .useSrcDirectory(buildDirectory);

                    generator.create();
                } else {
                    XmlTestGenerator generator = (XmlTestGenerator) getXmlTestGenerator()
                            .withDisabled(test.isDisabled())
                            .withFramework(getFramework())
                            .withName(test.getName())
                            .withAuthor(test.getAuthor())
                            .withDescription(test.getDescription())
                            .usePackage(test.getPackageName())
                            .useSrcDirectory(buildDirectory);

                    generator.create();
                }

                getLog().info("Successfully created new test case " + test.getPackageName() + "." + test.getName());
            }
        }
    }

    /**
     * Method provides test generator instance. Basically introduced for better mocking capabilities in unit tests but
     * also useful for subclasses to provide customized generator instance.
     * .
     * @return test generator.
     */
    public XmlTestGenerator getXmlTestGenerator() {
        return Optional.ofNullable(xmlTestGenerator).orElse(new XmlTestGenerator());
    }

    /**
     * Method provides test generator instance. Basically introduced for better mocking capabilities in unit tests but
     * also useful for subclasses to provide customized generator instance.
     * .
     * @return test generator.
     */
    public JavaDslTestGenerator getJavaTestGenerator() {
        return Optional.ofNullable(javaTestGenerator).orElse(new JavaDslTestGenerator());
    }

    /**
     * Method provides test generator instance. Basically introduced for better mocking capabilities in unit tests but
     * also useful for subclasses to provide customized generator instance.
     * .
     * @return test generator.
     */
    public SwaggerTestGenerator getSwaggerTestGenerator() {
        if (getType().equals("java")) {
            return Optional.ofNullable(swaggerJavaTestGenerator).orElse(new SwaggerJavaTestGenerator());
        } else {
            return Optional.ofNullable(swaggerXmlTestGenerator).orElse(new SwaggerXmlTestGenerator());
        }
    }

    /**
     * Method provides test generator instance. Basically introduced for better mocking capabilities in unit tests but
     * also useful for subclasses to provide customized generator instance.
     * .
     * @return test generator.
     */
    public WsdlTestGenerator getWsdlTestGenerator() {
        if (getType().equals("java")) {
            return Optional.ofNullable(wsdlJavaTestGenerator).orElse(new WsdlJavaTestGenerator());
        } else {
            return Optional.ofNullable(wsdlXmlTestGenerator).orElse(new WsdlXmlTestGenerator());
        }
    }

    /**
     * Method provides test generator instance. Basically introduced for better mocking capabilities in unit tests but
     * also useful for subclasses to provide customized generator instance.
     * .
     * @return test generator.
     */
    public XsdTestGenerator getXsdTestGenerator() {
        if (getType().equals("java")) {
            return Optional.ofNullable(xsdJavaTestGenerator).orElse(new XsdJavaTestGenerator());
        } else {
            return Optional.ofNullable(xsdXmlTestGenerator).orElse(new XsdXmlTestGenerator());
        }
    }
}
