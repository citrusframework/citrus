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

import com.consol.citrus.creator.*;
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

    private final XmlTestCreator xmlTestCreator;
    private final XsdXmlTestCreator xsdXmlTestCreator;
    private final WsdlXmlTestCreator wsdlXmlTestCreator;

    /**
     * Default constructor.
     */
    public CreateTestMojo() {
        this(new XmlTestCreator(), new XsdXmlTestCreator(), new WsdlXmlTestCreator());
    }

    /**
     * Constructor using final fields.
     * @param xmlTestCreator
     * @param xsdXmlTestCreator
     * @param wsdlXmlTestCreator
     */
    public CreateTestMojo(XmlTestCreator xmlTestCreator, XsdXmlTestCreator xsdXmlTestCreator, WsdlXmlTestCreator wsdlXmlTestCreator) {
        this.xmlTestCreator = xmlTestCreator;
        this.xsdXmlTestCreator = xsdXmlTestCreator;
        this.wsdlXmlTestCreator = wsdlXmlTestCreator;
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
                XsdXmlTestCreator creator = getXsdXmlTestCaseCreator();

                creator.withFramework(UnitFramework.fromString(framework))
                        .withName(name)
                        .withAuthor(author)
                        .withDescription(description)
                        .usePackage(targetPackage);

                createWithXsd(creator);
                return;
            }

            String useWsdl = prompter.prompt("Create test with WSDL?", CollectionUtils.arrayToList(new String[] {"y", "n"}), "n");

            if (useWsdl.equalsIgnoreCase("y")) {
                WsdlXmlTestCreator creator = getWsdlXmlTestCaseCreator();

                creator.withFramework(UnitFramework.fromString(framework))
                        .withName(name)
                        .withAuthor(author)
                        .withDescription(description)
                        .usePackage(targetPackage);

                createWithWsdl(creator);
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

            XmlTestCreator creator = getXmlTestCaseCreator()
                    .withFramework(UnitFramework.fromString(framework))
                    .withName(name)
                    .withAuthor(author)
                    .withDescription(description)
                    .usePackage(targetPackage);

            creator.create();

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
     * @param creator
     * @throws MojoExecutionException
     */
    public void createWithXsd(XsdXmlTestCreator creator) throws MojoExecutionException {
        try {
            String xsd = null;
            while(!StringUtils.hasText(xsd)) {
                xsd = prompter.prompt("Enter path to XSD");
            }
            creator.withXsd(xsd);

            String xsdRequestMessage = prompter.prompt("Enter request element name");
            creator.withRequestMessage(xsdRequestMessage);

            String xsdResponseMessage = prompter.prompt("Enter response element name", creator.getResponseMessageSuggestion());
            creator.withResponseMessage(xsdResponseMessage);

            String confirm = prompter.prompt("Confirm test creation:\n" +
                    "framework: " + creator.getFramework() + "\n" +
                    "name: " + creator.getName() + "\n" +
                    "author: " + creator.getAuthor() + "\n" +
                    "description: " + creator.getDescription() + "\n" +
                    "package: " + creator.getTargetPackage() + "\n", CollectionUtils.arrayToList(new String[] {"y", "n"}), "y");

            if (confirm.equalsIgnoreCase("n")) {
                return;
            }

            creator.create();

            getLog().info("Successfully created new test case " + creator.getTargetPackage() + "." + creator.getName());
        } catch (ArrayIndexOutOfBoundsException e) {
            getLog().info("Wrong parameter usage! See citrus:help for usage details (mvn citrus:help -Ddetail=true -Dgoal=create-test-from-xsd).");
        } catch (PrompterException e) {
            getLog().info(e);
            getLog().info("Failed to create test! See citrus:help for usage details (mvn citrus:help -Ddetail=true -Dgoal=create-test-from-xsd).");
        }
    }

    /**
     * Creates test case with request and response messages from XML schema.
     * @param creator
     * @throws MojoExecutionException
     */
    public void createWithWsdl(WsdlXmlTestCreator creator) throws MojoExecutionException {
        try {
            String wsdl = null;
            while (!StringUtils.hasText(wsdl)) {
                wsdl = prompter.prompt("Enter path to WSDL");
            }

            if (!StringUtils.hasText(wsdl)) {
                throw new MojoExecutionException("Please provide proper path to WSDL file");
            }

            creator.withWsdl(wsdl);

            String namePrefix = prompter.prompt("Enter test name prefix", creator.getName() + "_");
            creator.withNamePrefix(namePrefix);

            String nameSuffix = prompter.prompt("Enter test name suffix", creator.getNameSuffix());
            creator.withNameSuffix(nameSuffix);

            String confirm = prompter.prompt("Confirm test creation:\n" +
                    "framework: " + creator.getFramework() + "\n" +
                    "name: " + creator.getName() + "\n" +
                    "author: " + creator.getAuthor() + "\n" +
                    "description: " + creator.getDescription() + "\n" +
                    "package: " + creator.getTargetPackage() + "\n", CollectionUtils.arrayToList(new String[] {"y", "n"}), "y");

            if (confirm.equalsIgnoreCase("n")) {
                return;
            }

            creator.create();

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

    /**
     * Sets the prompter.
     * @param prompter the prompter to set
     */
    public void setPrompter(Prompter prompter) {
        this.prompter = prompter;
    }
}
