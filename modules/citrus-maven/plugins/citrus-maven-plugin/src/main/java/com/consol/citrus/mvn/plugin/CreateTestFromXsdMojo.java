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

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.xsd2inst.SampleXmlUtil;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.consol.citrus.util.TestCaseCreator;
import com.consol.citrus.util.TestCaseCreator.UnitFramework;

/**
 * Creates new Citrus test cases with empty XML test file and executable Java class.
 * 
 * Mojo offers an interactive mode, where the plugin prompts for parameters during execution. In
 * non-interactive mode the parameters are given as command line arguments.
 *
 * @author Christian Wied
 * @goal create-test-from-xsd
 */
public class CreateTestFromXsdMojo extends AbstractMojo {
	
	/**
	 * Path of the xsd from which the sample request and response are get from
	 * @parameter 
	 *          expression="${pathToXsd}"
	 *          default-value="" */
	private String pathToXsd;
	
	/**
	 * Name of the xsd-element used to create the xml-sample-request
	 * @parameter 
	 *          expression="${xsdRequestElem}"
	 *          default-value="" */
	private String xsdRequestElem;
	
	/**
	 * Name of the xsd-element used to create the xml-sample-response
	 * @parameter 
	 *          expression="${xsdResponseElem}"
	 *          default-value="" */
	private String xsdResponseElem;

	/**
     * The name of the test case (must start with upper case letter). 
     * @parameter 
     *          expression="${name}" 
     *          default-value="" */
    private String name;
    
    /**
     * The test author
     * @parameter
     *          expression="${author}" 
     *          default-value="Unknown" */
    private String author;

    /**
     * Describes the test case and its actions
     * @parameter
     *          expression="${description}" 
     *          default-value="TODO: Description" */
    private String description;
    
    /** 
     * Which package (folder structure) is assigned to this test. Defaults to "com.consol.citrus"
     * @parameter 
     *          expression="${targetPackage}"
     *          default-value="com.consol.citrus" */
    private String targetPackage;
    
    /** 
     * Whether to run this command in interactive mode. Defaults to "true".
     * @parameter 
     *          expression="${interactiveMode}"
     *          default-value="true" */
    private boolean interactiveMode;

    /**
     * Which unit test framework to use for test execution (default: testng; options: testng, junit3, junit4)
     * @parameter 
     *          expression="${framework}"
     *          default-value="testng" */
    private String framework;
    
    /** @component
     *  @required */
    private Prompter prompter;

    /**
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
    public void execute() throws MojoExecutionException {
        try {
        	while(interactiveMode && !StringUtils.hasText(pathToXsd)) {
        		pathToXsd = prompter.prompt("Enter path to XSD", pathToXsd);
        	}
        	
        	// compile xsd already here, otherwise later input is useless:
        	SchemaTypeSystem sts = compileXsd();
			SchemaType[] globalElems = sts.documentTypes();
			
			SchemaType requestElem = null;
			SchemaType responseElem = null;
			
			if (interactiveMode) {
				xsdRequestElem = prompter.prompt("Enter request element name", xsdRequestElem);
				for (SchemaType elem : globalElems) {
					if (elem.getContentModel().getName().getLocalPart().equals(xsdRequestElem)) {
						requestElem = elem;
						break;
					}
				}
			
				// try to guess the response-element and the testname from the request:
				if (xsdRequestElem.endsWith("Req")) {
					xsdResponseElem = xsdRequestElem.substring(0, xsdRequestElem.indexOf("Req")) + "Res";
					name = xsdRequestElem.substring(0, xsdRequestElem.indexOf("Req")) + "Test";
				} else if (xsdRequestElem.endsWith("Request")) {
					xsdResponseElem = xsdRequestElem.substring(0, xsdRequestElem.indexOf("Request")) + "Response";
					name = xsdRequestElem.substring(0, xsdRequestElem.indexOf("Request")) + "Test";
				} else if (xsdRequestElem.endsWith("RequestMessage")) {
				    xsdResponseElem = xsdRequestElem.substring(0, xsdRequestElem.indexOf("RequestMessage")) + "ResponseMessage";
                    name = xsdRequestElem.substring(0, xsdRequestElem.indexOf("RequestMessage")) + "Test";
				}

				xsdResponseElem = prompter.prompt("Enter response element name", xsdResponseElem);
				for (SchemaType elem : globalElems) {
					if (elem.getContentModel().getName().getLocalPart().equals(xsdResponseElem)) {
						responseElem = elem;
						break;
					}
				}
			
				while (!StringUtils.hasText(name)) {
				    name = prompter.prompt("Enter test name", name);
	            }
				
        		author = prompter.prompt("Enter test author:", author);
        		description = prompter.prompt("Enter test description:", description);
        		targetPackage = prompter.prompt("Enter test package:", targetPackage);
        		framework = prompter.prompt("Choose unit test framework", framework);
        		
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
			
			if (!StringUtils.hasText(name)) {
                throw new MojoExecutionException("Please provide proper test name! Test name must not be empty starting with uppercase letter!");
            }
        	
			// Now generate it
            TestCaseCreator creator = getTestCaseCreator()
                .withFramework(UnitFramework.fromString(framework))
                .withName(name)
                .withAuthor(author)
                .withDescription(description)
                .usePackage(targetPackage)
                .withXmlRequest(SampleXmlUtil.createSampleForType(requestElem))
                .withXmlResponse(SampleXmlUtil.createSampleForType(responseElem));
            
            creator.createTestCase();
            
            getLog().info("Successfully created new test case " + targetPackage + "." + name);
        } catch (ArrayIndexOutOfBoundsException e) {
            getLog().info("Wrong parameter usage! See citrus:help for usage details (mvn citrus:help -Ddetail=true -Dgoal=create-test-from-xsd).");
        } catch (PrompterException e) {
			getLog().info(e);
			getLog().info("Failed to create test! See citrus:help for usage details (mvn citrus:help -Ddetail=true -Dgoal=create-test-from-xsd).");
		}
    }

	private SchemaTypeSystem compileXsd() throws MojoExecutionException {
		
		File xsdFile = new File(pathToXsd);
		if (!xsdFile.exists()) {
			throw new MojoExecutionException("Unable to read XSD - does not exist in " + xsdFile.getAbsolutePath());
		}
		if (!xsdFile.canRead()) {
			throw new MojoExecutionException("Unable to read XSD - could not open in read mode");
		}
		
		XmlObject xsd = null;
		try {
			xsd = XmlObject.Factory.parse(xsdFile, (new XmlOptions()).setLoadLineNumbers().setLoadMessageDigest().setCompileDownloadUrls());
		} catch (Exception e) {
			throw new MojoExecutionException("Failed to parse XSD schema", e);
		}
		XmlObject[] schemas = new XmlObject[] { xsd };
		SchemaTypeSystem schemaTypeSystem = null;
		try {
			schemaTypeSystem = XmlBeans.compileXsd(schemas, XmlBeans.getContextTypeLoader(), new XmlOptions());
		} catch (Exception e) {
			throw new MojoExecutionException("Failed to compile XSD schema", e);
		}
		return schemaTypeSystem;
	}
	
	/**
     * Method provides test creator instance. Basically introduced for better mocking capabilities in unit tests but
     * also useful for subclasses to provide customized creator instance.
     * .
     * @return test creator.
     */
    public TestCaseCreator getTestCaseCreator() {
        return TestCaseCreator.build();
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
