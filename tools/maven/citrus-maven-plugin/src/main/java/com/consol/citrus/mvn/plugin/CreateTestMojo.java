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

package com.consol.citrus.mvn.plugin;

import com.consol.citrus.generate.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Optional;

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

    /**
     * Default constructor.
     */
    public CreateTestMojo() {
        this(new XmlTestGenerator(), new XsdXmlTestGenerator(), new WsdlXmlTestGenerator());
    }

    /**
     * Constructor using final fields.
     * @param xmlTestGenerator
     * @param xsdXmlTestGenerator
     * @param wsdlXmlTestGenerator
     */
    public CreateTestMojo(XmlTestGenerator xmlTestGenerator, XsdXmlTestGenerator xsdXmlTestGenerator, WsdlXmlTestGenerator wsdlXmlTestGenerator) {
        this.xmlTestGenerator = xmlTestGenerator;
        this.xsdXmlTestGenerator = xsdXmlTestGenerator;
        this.wsdlXmlTestGenerator = wsdlXmlTestGenerator;
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
            String targetPackage = prompter.prompt("Enter test package:", "com.consol.citrus");
            String framework = prompter.prompt("Choose unit test framework:", CollectionUtils.arrayToList(new String[] {"testng", "junit4", "junit5"}), UnitFramework.TESTNG.name().toLowerCase());

            String useXsd = prompter.prompt("Create test with XML schema?", CollectionUtils.arrayToList(new String[] {"y", "n"}), "n");

            if (useXsd.equalsIgnoreCase("y")) {
                XsdXmlTestGenerator generator = getXsdXmlTestGenerator();

                generator.withFramework(UnitFramework.fromString(framework))
                        .withName(name)
                        .withAuthor(author)
                        .withDescription(description)
                        .usePackage(targetPackage);

                createWithXsd(generator);
                return;
            }

            String useWsdl = prompter.prompt("Create test with WSDL?", CollectionUtils.arrayToList(new String[] {"y", "n"}), "n");

            if (useWsdl.equalsIgnoreCase("y")) {
                WsdlXmlTestGenerator generator = getWsdlXmlTestGenerator();

                generator.withFramework(UnitFramework.fromString(framework))
                        .withName(name)
                        .withAuthor(author)
                        .withDescription(description)
                        .usePackage(targetPackage);

                createWithWsdl(generator);
                return;
            }

            String confirm = prompter.prompt("Confirm test creation:\n" +
                    "framework: " + framework + "\n" +
                    "name: " + name + "\n" +
                    "author: " + author + "\n" +
                    "description: " + description + "\n" +
                    "package: " + targetPackage + "\n", CollectionUtils.arrayToList(new String[] {"y", "n"}), "y");

            if (confirm.equalsIgnoreCase("n")) {
                return;
            }

            XmlTestGenerator generator = getXmlTestGenerator()
                    .withFramework(UnitFramework.fromString(framework))
                    .withName(name)
                    .withAuthor(author)
                    .withDescription(description)
                    .usePackage(targetPackage);

            generator.create();

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
    public void createWithXsd(XsdXmlTestGenerator generator) throws MojoExecutionException {
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

            String actor = prompter.prompt("Actor as:", CollectionUtils.arrayToList(new String[] {"client", "server"}), "client");
            generator.withActor(actor);

            String confirm = prompter.prompt("Confirm test creation:\n" +
                    "framework: " + generator.getFramework() + "\n" +
                    "name: " + generator.getName() + "\n" +
                    "author: " + generator.getAuthor() + "\n" +
                    "description: " + generator.getDescription() + "\n" +
                    "xsd: " + generator.getXsd() + "\n" +
                    "request: " + generator.getRequestMessage() + "\n" +
                    "response: " + generator.getResponseMessage() + "\n" +
                    "actor: " + generator.getActor() + "\n" +
                    "package: " + generator.getTargetPackage() + "\n", CollectionUtils.arrayToList(new String[] {"y", "n"}), "y");

            if (confirm.equalsIgnoreCase("n")) {
                return;
            }

            generator.create();

            getLog().info("Successfully created new test case " + generator.getTargetPackage() + "." + generator.getName());
        } catch (ArrayIndexOutOfBoundsException e) {
            getLog().info("Wrong parameter usage! See citrus:help for usage details (mvn citrus:help -Ddetail=true -Dgoal=create-test-from-xsd).");
        } catch (PrompterException e) {
            getLog().info(e);
            getLog().info("Failed to create test! See citrus:help for usage details (mvn citrus:help -Ddetail=true -Dgoal=create-test-from-xsd).");
        }
    }

    /**
     * Creates test case with request and response messages from XML schema.
     * @param generator
     * @throws MojoExecutionException
     */
    public void createWithWsdl(WsdlXmlTestGenerator generator) throws MojoExecutionException {
        try {
            String wsdl = null;
            while (!StringUtils.hasText(wsdl)) {
                wsdl = prompter.prompt("Enter path to WSDL");
            }

            if (!StringUtils.hasText(wsdl)) {
                throw new MojoExecutionException("Please provide proper path to WSDL file");
            }

            generator.withWsdl(wsdl);

            String actor = prompter.prompt("Actor as:", CollectionUtils.arrayToList(new String[] {"client", "server"}), "client");
            generator.withActor(actor);

            String operation = prompter.prompt("Enter operation name", "all");
            if (!operation.equals("all")) {
                generator.withOperation(operation);
            }

            String namePrefix = prompter.prompt("Enter test name prefix", generator.getName() + "_");
            generator.withNamePrefix(namePrefix);

            String nameSuffix = prompter.prompt("Enter test name suffix", generator.getNameSuffix());
            generator.withNameSuffix(nameSuffix);

            String confirm = prompter.prompt("Confirm test creation:\n" +
                    "framework: " + generator.getFramework() + "\n" +
                    "name: " + generator.getName() + "\n" +
                    "author: " + generator.getAuthor() + "\n" +
                    "description: " + generator.getDescription() + "\n" +
                    "wsdl: " + generator.getWsdl() + "\n" +
                    "operation: " + Optional.ofNullable(generator.getOperation()).orElse("all") + "\n" +
                    "actor: " + generator.getActor() + "\n" +
                    "package: " + generator.getTargetPackage() + "\n", CollectionUtils.arrayToList(new String[] {"y", "n"}), "y");

            if (confirm.equalsIgnoreCase("n")) {
                return;
            }

            generator.create();

            getLog().info("Successfully created new test cases from WSDL");
        } catch (ArrayIndexOutOfBoundsException e) {
            getLog().info(e);
            getLog().info("Wrong parameter usage! See citrus:help for usage details (mvn citrus:help -Ddetail=true -Dgoal=create-suite-from-wsdl).");
        } catch (PrompterException e) {
            getLog().info(e);
            getLog().info("Failed to create suite! See citrus:help for usage details (mvn citrus:help -Ddetail=true -Dgoal=create-suite-from-wsdl).");
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
    public WsdlXmlTestGenerator getWsdlXmlTestGenerator() {
        return Optional.ofNullable(wsdlXmlTestGenerator).orElse(new WsdlXmlTestGenerator());
    }

    /**
     * Method provides test generator instance. Basically introduced for better mocking capabilities in unit tests but
     * also useful for subclasses to provide customized generator instance.
     * .
     * @return test generator.
     */
    public XsdXmlTestGenerator getXsdXmlTestGenerator() {
        return Optional.ofNullable(xsdXmlTestGenerator).orElse(new XsdXmlTestGenerator());
    }

    /**
     * Sets the prompter.
     * @param prompter the prompter to set
     */
    public void setPrompter(Prompter prompter) {
        this.prompter = prompter;
    }
}
