/*
 * Copyright 2006-2010 the original author or authors.
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

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.citrusframework.generate.SwaggerTestGenerator;
import org.citrusframework.generate.TestGenerator;
import org.citrusframework.generate.UnitFramework;
import org.citrusframework.generate.WsdlTestGenerator;
import org.citrusframework.generate.XsdTestGenerator;
import org.citrusframework.generate.javadsl.JavaDslTestGenerator;
import org.citrusframework.generate.javadsl.SwaggerJavaTestGenerator;
import org.citrusframework.generate.javadsl.WsdlJavaTestGenerator;
import org.citrusframework.generate.javadsl.XsdJavaTestGenerator;
import org.citrusframework.generate.xml.SwaggerXmlTestGenerator;
import org.citrusframework.generate.xml.WsdlXmlTestGenerator;
import org.citrusframework.generate.xml.XmlTestGenerator;
import org.citrusframework.generate.xml.XsdXmlTestGenerator;
import org.citrusframework.util.StringUtils;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;

/**
 * Creates new Citrus test cases with empty XML test file and executable Java class.
 *
 * Mojo offers an interactive mode, where the plugin prompts for parameters during execution.
 *
 * @author Christoph Deppisch
 * @since 2.7.4
 */
@Mojo(name = "create-test")
public class CreateTestMojo extends AbstractCitrusMojo {

    @Parameter(property = "citrus.skip.create.test", defaultValue = "false")
    protected boolean skipCreateTest;

    @Component
    private Prompter prompter;

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
    public CreateTestMojo() {
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
    public CreateTestMojo(XmlTestGenerator xmlTestGenerator,
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
        if (skipCreateTest) {
            return;
        }

        try {
            String name = null;
        	while (!StringUtils.hasText(name)) {
        		name = prompter.prompt("Enter test name:");
        	}

        	if (!StringUtils.hasText(name)) {
        		throw new MojoExecutionException("Please provide proper test name! Test name must not be empty starting with uppercase letter!");
        	}

            String author = prompter.prompt("Enter test author:", "Unknown");
            String description = prompter.prompt("Enter test description:", "");
            String targetPackage = prompter.prompt("Enter test package:", "org.citrusframework");
            String framework = prompter.prompt("Choose unit test framework:", Arrays.asList("testng", "junit4", "junit5"), UnitFramework.TESTNG.name().toLowerCase());
            String type = prompter.prompt("Choose target code base type:", Arrays.asList("java", "xml"), "java");

            setType(type);

            String useXsd = prompter.prompt("Create test with XML schema?", Arrays.asList("y", "n"), "n");

            if (useXsd.equalsIgnoreCase("y")) {
                XsdTestGenerator generator = getXsdTestGenerator();

                generator.withFramework(UnitFramework.fromString(framework))
                        .withName(name)
                        .withAuthor(author)
                        .withDescription(description)
                        .usePackage(targetPackage);

                createWithXsd(generator);
                return;
            }

            String useWsdl = prompter.prompt("Create test with WSDL?", Arrays.asList("y", "n"), "n");

            if (useWsdl.equalsIgnoreCase("y")) {
                WsdlTestGenerator generator = getWsdlTestGenerator();

                generator.withFramework(UnitFramework.fromString(framework))
                        .withName(name)
                        .withAuthor(author)
                        .withDescription(description)
                        .usePackage(targetPackage);

                createWithWsdl(generator);
                return;
            }

            String useSwagger = prompter.prompt("Create test with Swagger API?", Arrays.asList("y", "n"), "n");

            if (useSwagger.equalsIgnoreCase("y")) {
                SwaggerTestGenerator generator = getSwaggerTestGenerator();

                generator.withFramework(UnitFramework.fromString(framework))
                        .withName(name)
                        .withAuthor(author)
                        .withDescription(description)
                        .usePackage(targetPackage);

                createWithSwagger(generator);
                return;
            }

            String confirm = prompter.prompt("Confirm test creation:\n" +
                    "type: " + getType() + "\n" +
                    "framework: " + framework + "\n" +
                    "name: " + name + "\n" +
                    "author: " + author + "\n" +
                    "description: " + description + "\n" +
                    "package: " + targetPackage + "\n", Arrays.asList("y", "n"), "y");

            if (confirm.equalsIgnoreCase("n")) {
                return;
            }

            if (getType().equals("java")) {
                JavaDslTestGenerator generator = (JavaDslTestGenerator) getJavaTestGenerator()
                        .withFramework(UnitFramework.fromString(framework))
                        .withName(name)
                        .withAuthor(author)
                        .withDescription(description)
                        .usePackage(targetPackage);

                generator.create();
            } else {
                XmlTestGenerator generator = (XmlTestGenerator) getXmlTestGenerator()
                        .withFramework(UnitFramework.fromString(framework))
                        .withName(name)
                        .withAuthor(author)
                        .withDescription(description)
                        .usePackage(targetPackage);

                generator.create();
            }

            getLog().info("Successfully created new test case " + targetPackage + "." + name);
        } catch (ArrayIndexOutOfBoundsException e) {
            getLog().info("Wrong parameter usage! See citrus:help for usage details (mvn citrus:help -Ddetail=true -Dgoal=create-test).");
        } catch (PrompterException e) {
			getLog().info(e);
			getLog().info("Failed to create test! See citrus:help for usage details (mvn citrus:help -Ddetail=true -Dgoal=create-test).");
		}
    }

    /**
     * Creates test case with request and response messages from XML schema.
     * @param generator
     * @throws MojoExecutionException
     */
    public void createWithXsd(XsdTestGenerator generator) throws MojoExecutionException {
        try {
            String xsd = null;
            while(!StringUtils.hasText(xsd)) {
                xsd = prompter.prompt("Enter path to XSD");
            }
            generator.withXsd(xsd);

            String xsdRequestMessage = prompter.prompt("Enter request element name");
            generator.withRequestMessage(xsdRequestMessage);

            String xsdResponseMessage = prompter.prompt("Enter response element name", generator.getResponseMessageSuggestion());
            generator.withResponseMessage(xsdResponseMessage);

            String mode = prompter.prompt("Choose mode:", Arrays.stream(TestGenerator.GeneratorMode.values()).map(TestGenerator.GeneratorMode::name).collect(Collectors.toList()), TestGenerator.GeneratorMode.CLIENT.name());
            generator.withMode(TestGenerator.GeneratorMode.valueOf(mode.toUpperCase()));

            String confirm = prompter.prompt("Confirm test creation:\n" +
                    "type: " + getType() + "\n" +
                    "framework: " + generator.getFramework() + "\n" +
                    "name: " + generator.getName() + "\n" +
                    "author: " + generator.getAuthor() + "\n" +
                    "description: " + generator.getDescription() + "\n" +
                    "xsd: " + generator.getXsd() + "\n" +
                    "request: " + generator.getRequestMessage() + "\n" +
                    "response: " + generator.getResponseMessage() + "\n" +
                    "actor: " + generator.getMode() + "\n" +
                    "package: " + generator.getTargetPackage() + "\n", Arrays.asList("y", "n"), "y");

            if (confirm.equalsIgnoreCase("n")) {
                return;
            }

            generator.create();

            getLog().info("Successfully created new test case " + generator.getTargetPackage() + "." + generator.getName());
        } catch (ArrayIndexOutOfBoundsException e) {
            getLog().info("Wrong parameter usage! See citrus:help for usage details (mvn citrus:help -Ddetail=true -Dgoal=create-test).");
        } catch (PrompterException e) {
            getLog().info(e);
            getLog().info("Failed to create test! See citrus:help for usage details (mvn citrus:help -Ddetail=true -Dgoal=create-test).");
        }
    }

    /**
     * Creates test case with request and response messages from WSDL definition.
     * @param generator
     * @throws MojoExecutionException
     */
    public void createWithWsdl(WsdlTestGenerator generator) throws MojoExecutionException {
        try {
            String wsdl = null;
            while (!StringUtils.hasText(wsdl)) {
                wsdl = prompter.prompt("Enter path to WSDL");
            }

            if (!StringUtils.hasText(wsdl)) {
                throw new MojoExecutionException("Please provide proper path to WSDL file");
            }

            generator.withWsdl(wsdl);

            String mode = prompter.prompt("Choose mode:", Arrays.stream(TestGenerator.GeneratorMode.values()).map(TestGenerator.GeneratorMode::name).collect(Collectors.toList()), TestGenerator.GeneratorMode.CLIENT.name());
            generator.withMode(TestGenerator.GeneratorMode.valueOf(mode.toUpperCase()));

            String operation = prompter.prompt("Enter operation name", "all");
            if (!operation.equals("all")) {
                generator.withOperation(operation);
            }

            String namePrefix = prompter.prompt("Enter test name prefix", generator.getName() + "_");
            generator.withNamePrefix(namePrefix);

            String nameSuffix = prompter.prompt("Enter test name suffix", generator.getNameSuffix());
            generator.withNameSuffix(nameSuffix);

            String confirm = prompter.prompt("Confirm test creation:\n" +
                    "type: " + getType() + "\n" +
                    "framework: " + generator.getFramework() + "\n" +
                    "name: " + generator.getName() + "\n" +
                    "author: " + generator.getAuthor() + "\n" +
                    "description: " + generator.getDescription() + "\n" +
                    "wsdl: " + generator.getWsdl() + "\n" +
                    "operation: " + Optional.ofNullable(generator.getOperation()).orElse("all") + "\n" +
                    "actor: " + generator.getMode() + "\n" +
                    "package: " + generator.getTargetPackage() + "\n", Arrays.asList("y", "n"), "y");

            if (confirm.equalsIgnoreCase("n")) {
                return;
            }

            generator.create();

            getLog().info("Successfully created new test cases from WSDL");
        } catch (ArrayIndexOutOfBoundsException e) {
            getLog().info(e);
            getLog().info("Wrong parameter usage! See citrus:help for usage details (mvn citrus:help -Ddetail=true -Dgoal=create-test).");
        } catch (PrompterException e) {
            getLog().info(e);
            getLog().info("Failed to create suite! See citrus:help for usage details (mvn citrus:help -Ddetail=true -Dgoal=create-test).");
        }
    }

    /**
     * Creates test case with request and response messages from Swagger API.
     * @param generator
     * @throws MojoExecutionException
     */
    public void createWithSwagger(SwaggerTestGenerator generator) throws MojoExecutionException {
        try {
            String swagger = null;
            while (!StringUtils.hasText(swagger)) {
                swagger = prompter.prompt("Enter path to Swagger API");
            }

            if (!StringUtils.hasText(swagger)) {
                throw new MojoExecutionException("Please provide proper path to Swagger API file");
            }

            generator.withSpec(swagger);

            String mode = prompter.prompt("Choose mode:", Arrays.stream(TestGenerator.GeneratorMode.values()).map(TestGenerator.GeneratorMode::name).collect(Collectors.toList()), TestGenerator.GeneratorMode.CLIENT.name());
            generator.withMode(TestGenerator.GeneratorMode.valueOf(mode.toUpperCase()));

            String operation = prompter.prompt("Enter operation name", "all");
            if (!operation.equals("all")) {
                generator.withOperation(operation);
            }

            String namePrefix = prompter.prompt("Enter test name prefix", generator.getName() + "_");
            generator.withNamePrefix(namePrefix);

            String nameSuffix = prompter.prompt("Enter test name suffix", generator.getNameSuffix());
            generator.withNameSuffix(nameSuffix);

            String confirm = prompter.prompt("Confirm test creation:\n" +
                    "type: " + getType() + "\n" +
                    "framework: " + generator.getFramework() + "\n" +
                    "name: " + generator.getName() + "\n" +
                    "author: " + generator.getAuthor() + "\n" +
                    "description: " + generator.getDescription() + "\n" +
                    "swagger-api: " + generator.getSwaggerResource() + "\n" +
                    "operation: " + Optional.ofNullable(generator.getOperation()).orElse("all") + "\n" +
                    "actor: " + generator.getMode() + "\n" +
                    "package: " + generator.getTargetPackage() + "\n", Arrays.asList("y", "n"), "y");

            if (confirm.equalsIgnoreCase("n")) {
                return;
            }

            generator.create();

            getLog().info("Successfully created new test cases from Swagger API");
        } catch (ArrayIndexOutOfBoundsException e) {
            getLog().info(e);
            getLog().info("Wrong parameter usage! See citrus:help for usage details (mvn citrus:help -Ddetail=true -Dgoal=create-test).");
        } catch (PrompterException e) {
            getLog().info(e);
            getLog().info("Failed to create suite! See citrus:help for usage details (mvn citrus:help -Ddetail=true -Dgoal=create-test).");
        }
    }

    /**
     * Method provides test generator instance. Basically introduced for better mocking capabilities in unit tests but
     * also useful for subclasses to provide customized generator instance.
     * .
     * @return test generator.
     */
    public XmlTestGenerator getXmlTestGenerator() {
        return Optional.ofNullable(xmlTestGenerator).orElseGet(XmlTestGenerator::new);
    }

    /**
     * Method provides test generator instance. Basically introduced for better mocking capabilities in unit tests but
     * also useful for subclasses to provide customized generator instance.
     * .
     * @return test generator.
     */
    public JavaDslTestGenerator getJavaTestGenerator() {
        return Optional.ofNullable(javaTestGenerator).orElseGet(JavaDslTestGenerator::new);
    }

    /**
     * Method provides test generator instance. Basically introduced for better mocking capabilities in unit tests but
     * also useful for subclasses to provide customized generator instance.
     * .
     * @return test generator.
     */
    public SwaggerTestGenerator getSwaggerTestGenerator() {
        if (getType().equals("java")) {
            return Optional.ofNullable(swaggerJavaTestGenerator).orElseGet(SwaggerJavaTestGenerator::new);
        } else {
            return Optional.ofNullable(swaggerXmlTestGenerator).orElseGet(SwaggerXmlTestGenerator::new);
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
            return Optional.ofNullable(wsdlJavaTestGenerator).orElseGet(WsdlJavaTestGenerator::new);
        } else {
            return Optional.ofNullable(wsdlXmlTestGenerator).orElseGet(WsdlXmlTestGenerator::new);
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
            return Optional.ofNullable(xsdJavaTestGenerator).orElseGet(XsdJavaTestGenerator::new);
        } else {
            return Optional.ofNullable(xsdXmlTestGenerator).orElseGet(XsdXmlTestGenerator::new);
        }
    }

    /**
     * Sets the prompter.
     * @param prompter the prompter to set
     */
    public void setPrompter(Prompter prompter) {
        this.prompter = prompter;
    }
}
