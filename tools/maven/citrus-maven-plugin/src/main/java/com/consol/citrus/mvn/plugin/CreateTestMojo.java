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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Creates new Citrus test cases with empty XML test file and executable Java class.
 * 
 * Mojo offers an interactive mode, where the plugin prompts for parameters during execution. In
 * non-interactive mode the parameters are given as command line arguments.
 *
 * @author Christoph Deppisch
 */
@Mojo( name = "create-test")
public class CreateTestMojo extends AbstractMojo {

    /**
     * Whether to run this command in interactive mode. Defaults to "true".
     */
    @Parameter(property = "interactiveMode", defaultValue = "true")
    private boolean interactiveMode;

    /**
     * The name of the test case (must start with upper case letter).
     */
    @Parameter(property = "name", defaultValue = "")
    private String name;

    /**
     * The name-prefix of all test cases.
     */
    @Parameter(property = "namePrefix", defaultValue = "")
    private String namePrefix = "";

    /**
     * The name-suffix of all test cases.
     */
    @Parameter(property = "nameSuffix", defaultValue = "_Test")
    private String nameSuffix = "_Test";

    /**
     * The test author
     */
    @Parameter(property = "author", defaultValue = "Unknown")
    private String author;

    /**
     * Describes the test case and its actions
     */
    @Parameter(property = "description", defaultValue = "TODO: Description")
    private String description;

    /**
     * Which package (folder structure) is assigned to this test. Defaults to "com.consol.citrus"
     */
    @Parameter(property = "targetPackage", defaultValue = "com.consol.citrus")
    private String targetPackage;

    /**
     * Which unit test framework to use for test execution (default: testng; options: testng, junit)
     */
    @Parameter(property = "framework", defaultValue = "testng")
    private String framework;

    /**
     * Path of the xsd from which the sample request and response are get from
     */
    @Parameter(property = "xsd", defaultValue = "")
    private String xsd;

    /**
     * Name of the xsd-element used to create the xml-sample-request
     */
    @Parameter(property = "xsdRequestMessage", defaultValue = "")
    private String xsdRequestMessage;

    /**
     * Name of the xsd-element used to create the xml-sample-response
     */
    @Parameter(property = "xsdResponseMessage", defaultValue = "")
    private String xsdResponseMessage;

    /**
     * The path to the wsdl from which the suite is generated.
     */
    @Parameter(property = "wsdl", defaultValue = "")
    private String wsdl;

    @Component
    private Prompter prompter;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            String name = this.name;
        	while (interactiveMode && !StringUtils.hasText(name)) {
        		name = prompter.prompt("Enter test name:");
        	}
        	
        	if (!StringUtils.hasText(name)) {
        		throw new MojoExecutionException("Please provide proper test name! Test name must not be empty starting with uppercase letter!");
        	}

        	String author = this.author;
        	String description = this.description;
        	String targetPackage = this.targetPackage;
        	String framework = this.framework;
        	if (interactiveMode) {
        		author = prompter.prompt("Enter test author:", this.author);
        		description = prompter.prompt("Enter test description:", this.targetPackage);
        		targetPackage = prompter.prompt("Enter test package:", this.targetPackage);
        		framework = prompter.prompt("Choose unit test framework:", CollectionUtils.arrayToList(new String[] {"testng", "junit"}), this.framework);
        	}
        	
            if (interactiveMode) {
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
            }

            if (interactiveMode) {
                String confirm = prompter.prompt("Confirm test creation:\n" +
                        "framework: " + framework + "\n" +
                        "name: " + name + "\n" +
                        "author: " + author + "\n" +
                        "description: " + description + "\n" +
                        "package: " + targetPackage + "\n", CollectionUtils.arrayToList(new String[] {"y", "n"}), "y");

                if (confirm.equalsIgnoreCase("n")) {
                    return;
                }
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
            String xsd = this.xsd;
            while(interactiveMode && !StringUtils.hasText(xsd)) {
                xsd = prompter.prompt("Enter path to XSD", this.xsd);
            }
            creator.withXsd(xsd);

            String xsdRequestMessage = this.xsdRequestMessage;
            String xsdResponseMessage = this.xsdResponseMessage;
            if (interactiveMode) {
                xsdRequestMessage = prompter.prompt("Enter request element name", this.xsdRequestMessage);
                creator.withRequestMessage(xsdRequestMessage);

                xsdResponseMessage = prompter.prompt("Enter response element name", creator.getResponseMessageSuggestion());
            }

            creator.withRequestMessage(xsdRequestMessage);
            creator.withResponseMessage(xsdResponseMessage);

            if (interactiveMode) {
                String confirm = prompter.prompt("Confirm test creation:\n" +
                        "framework: " + creator.getFramework() + "\n" +
                        "name: " + creator.getName() + "\n" +
                        "author: " + creator.getAuthor() + "\n" +
                        "description: " + creator.getDescription() + "\n" +
                        "package: " + creator.getTargetPackage() + "\n", CollectionUtils.arrayToList(new String[] {"y", "n"}), "y");

                if (confirm.equalsIgnoreCase("n")) {
                    return;
                }
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
            String wsdl = this.wsdl;
            while (interactiveMode && !StringUtils.hasText(wsdl)) {
                wsdl = prompter.prompt("Enter path to WSDL", this.wsdl);
            }

            if (!StringUtils.hasText(wsdl)) {
                throw new MojoExecutionException("Please provide proper path to WSDL file");
            }

            creator.withWsdl(wsdl);

            String namePrefix = this.namePrefix;
            if (interactiveMode) {
                namePrefix = prompter.prompt("Enter test name prefix", this.name + "_");
            }
            creator.withNamePrefix(namePrefix);

            String nameSuffix = this.nameSuffix;
            if (interactiveMode) {
                nameSuffix = prompter.prompt("Enter test name suffix", this.nameSuffix);
            }
            creator.withNameSuffix(nameSuffix);

            if (interactiveMode) {
                String confirm = prompter.prompt("Confirm test creation:\n" +
                        "framework: " + creator.getFramework() + "\n" +
                        "name: " + creator.getName() + "\n" +
                        "author: " + creator.getAuthor() + "\n" +
                        "description: " + creator.getDescription() + "\n" +
                        "package: " + creator.getTargetPackage() + "\n", CollectionUtils.arrayToList(new String[] {"y", "n"}), "y");

                if (confirm.equalsIgnoreCase("n")) {
                    return;
                }
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
        return new XmlTestCreator();
    }

    /**
     * Method provides test creator instance. Basically introduced for better mocking capabilities in unit tests but
     * also useful for subclasses to provide customized creator instance.
     * .
     * @return test creator.
     */
    public WsdlXmlTestCreator getWsdlXmlTestCaseCreator() {
        return new WsdlXmlTestCreator();
    }

    /**
     * Method provides test creator instance. Basically introduced for better mocking capabilities in unit tests but
     * also useful for subclasses to provide customized creator instance.
     * .
     * @return test creator.
     */
    public XsdXmlTestCreator getXsdXmlTestCaseCreator() {
        return new XsdXmlTestCreator();
    }

    /**
     * Sets the interactiveMode.
     * @param interactiveMode the interactiveMode to set
     */
    public void setInteractiveMode(boolean interactiveMode) {
        this.interactiveMode = interactiveMode;
    }

    /**
     * Sets the prompter.
     * @param prompter the prompter to set
     */
    public void setPrompter(Prompter prompter) {
        this.prompter = prompter;
    }
}
