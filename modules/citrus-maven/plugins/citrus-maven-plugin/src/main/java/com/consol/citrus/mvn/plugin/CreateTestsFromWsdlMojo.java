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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.xsd2inst.SampleXmlUtil;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.consol.citrus.util.*;
import com.consol.citrus.util.TestCaseCreator.UnitFramework;

/**
 * Creates new Citrus test suite from a given WSDL with XML test file and executable Java class.
 * 
 * Mojo offers an interactive mode, where the plugin prompts for parameters during execution. In
 * non-interactive mode the parameters are given as command line arguments.
 *
 * @author Christian Wied
 * @goal create-suite-from-wsdl
 */
public class CreateTestsFromWsdlMojo extends AbstractMojo {
	/**
	 * The path to the wsdl from which the suite is generated. 
	 * @parameter 
	 *          expression="${pathToWsdl}" 
	 *          default-value="" */
	private String pathToWsdl;

	/**
     * The name-prefix of all test cases (must start with upper case letter). 
     * @parameter 
     *          expression="${namePrefix}" 
     *          default-value="" */
    private String namePrefix = "";
    
    /**
     * The name-suffix of all test cases. 
     * @parameter 
     *          expression="${nameSuffix}" 
     *          default-value="_Test" */
    private String nameSuffix = "_Test";
    
    /**
     * The test author
     * @parameter
     *          expression="${author}" 
     *          default-value="Unknown" */
    private String author = "Unknown";

    /**
     * Describes the test case and its actions
     * @parameter
     *          expression="${description}" 
     *          default-value="TODO: Description" */
    private String description = "TODO: Description";
    
    /** 
     * Which package (folder structure) is assigned to this test. Defaults to "com.consol.citrus"
     * @parameter 
     *          expression="${targetPackage}"
     *          default-value="com.consol.citrus" */
    private String targetPackage = "com.consol.citrus";
    
    /** 
     * Whether to run this command in interactive mode. Defaults to "true".
     * @parameter 
     *          expression="${interactiveMode}"
     *          default-value="true" */
    private boolean interactiveMode = true;

    /**
     * Which unit test framework to use for test execution (default: testng; options: testng, junit3, junit4)
     * @parameter 
     *          expression="${framework}"
     *          default-value="testng" */
    private String framework = "testng";
    
    /** @component
     *  @required */
    private Prompter prompter;
    
    private final String separator = "+++++++++++++++++++++++++++++++++++";

    /**
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
    public void execute() throws MojoExecutionException {
        try {
        	while (interactiveMode && !StringUtils.hasText(pathToWsdl)) {
        		pathToWsdl = prompter.prompt("Enter path to WSDL");
        	}
        	
        	if (!StringUtils.hasText(pathToWsdl)) {
        	    throw new MojoExecutionException("Please provide proper path to WSDL file");
        	}
        	
        	String wsdlNsDelaration = "declare namespace wsdl='http://schemas.xmlsoap.org/wsdl/' ";
        	
        	// compile wsdl and xsds right now, otherwise later input is useless:
        	XmlObject wsdl = compileWsdl();
        	SchemaTypeSystem schemaTypeSystem = compileXsd(wsdl);
        	
            getLog().info(separator);
        	getLog().info("WSDL compilation successful");
        	String serviceName = evaluateAsString(wsdl, wsdlNsDelaration + ".//wsdl:portType/@name"); 
        	getLog().info("Found service: " + serviceName);
        	
        	getLog().info(separator);
        	getLog().info("Found service operations:");
        	XmlObject[] messages = wsdl.selectPath(wsdlNsDelaration + ".//wsdl:message");
    		XmlObject[] operations = wsdl.selectPath(wsdlNsDelaration + ".//wsdl:portType/wsdl:operation");
    		for (XmlObject operation : operations) {
    			getLog().info(evaluateAsString(operation, wsdlNsDelaration + "./@name"));
			}
    		getLog().info(separator);
        	
    		getLog().info("Generating test cases for service operations ...");
        	if (interactiveMode) {
        		namePrefix = prompter.prompt("Enter test name prefix", namePrefix);
        		nameSuffix = prompter.prompt("Enter test name suffix", nameSuffix);
        		author = prompter.prompt("Enter test author:", author);
        		description = prompter.prompt("Enter test description:", description);
        		targetPackage = prompter.prompt("Enter test package:", targetPackage);
        		framework = prompter.prompt("Choose unit test framework", framework);
        		
        		String confirm = prompter.prompt("Confirm test creation:\n" +
        		        "framework: " + framework + "\n" +
    			        "name e.g.: " + namePrefix + evaluateAsString(operations[0], wsdlNsDelaration + "./@name") + nameSuffix + "\n" +
    					"author: " + author + "\n" +
    					"description: " + description + "\n" +
    					"package: " + targetPackage + "\n", CollectionUtils.arrayToList(new String[] {"y", "n"}), "y");
    	
		    	if (confirm.equalsIgnoreCase("n")) {
		    		return;
		    	}
        	}
        	
			for (XmlObject operation : operations) {
				String operationName = evaluateAsString(operation, wsdlNsDelaration + "./@name");
				String inputMessage = removeNsPrefix(evaluateAsString(operation, wsdlNsDelaration + "./wsdl:input/@message"));
				String outputMessage = removeNsPrefix(evaluateAsString(operation, wsdlNsDelaration + "./wsdl:output/@message"));
				
				String inputElement = null;
				String outputElement = null;
				for (XmlObject message : messages) {
				    String messageName = evaluateAsString(message, wsdlNsDelaration + "./@name");
				    
				    if (messageName.equals(inputMessage)) {
				        inputElement = removeNsPrefix(evaluateAsString(message, wsdlNsDelaration + "./wsdl:part/@element"));
				    }
				    
				    if (messageName.equals(outputMessage)) {
                        outputElement = removeNsPrefix(evaluateAsString(message, wsdlNsDelaration + "./wsdl:part/@element"));
                    }
                }
				
				SchemaType requestElem = getSchemaType(schemaTypeSystem, operationName, inputElement);
				SchemaType responseElem = getSchemaType(schemaTypeSystem, operationName, outputElement);
				
				String testName = namePrefix + operationName + nameSuffix;
				
				// Now generate it
				TestCaseCreator creator = getTestCaseCreator()
					.withFramework(UnitFramework.fromString(framework))
					.withName(testName)
					.withAuthor(author)
					.withDescription(description)
					.usePackage(targetPackage)
					.withXmlRequest(SampleXmlUtil.createSampleForType(requestElem))
					.withXmlResponse(SampleXmlUtil.createSampleForType(responseElem));
				
				creator.createTestCase();
				
				getLog().info("Successfully created new test case " + targetPackage + "." + testName);
			}
			
        } catch (ArrayIndexOutOfBoundsException e) {
        	getLog().info(e);
            getLog().info("Wrong parameter usage! See citrus:help for usage details (mvn citrus:help -Ddetail=true -Dgoal=create-suite-from-wsdl).");
        } catch (PrompterException e) {
			getLog().info(e);
			getLog().info("Failed to create suite! See citrus:help for usage details (mvn citrus:help -Ddetail=true -Dgoal=create-suite-from-wsdl).");
		} catch (IOException e) {
            getLog().info(e);
        }
    }
    
    /**
     * @param schemaTypeSystem
     * @param operation
     * @param elementName
     * @return
     * @throws MojoExecutionException 
     */
    private SchemaType getSchemaType(SchemaTypeSystem schemaTypeSystem, String operation, String elementName) 
            throws MojoExecutionException {
        
        for (SchemaType elem : schemaTypeSystem.documentTypes()) {
            if (elem.getContentModel().getName().getLocalPart().equals(elementName)) {
                return elem;
            }
        }
        
        throw new MojoExecutionException("Unable to find schema type declaration '" + elementName + "'" +
                " for WSDL operation '" + operation + "'");
    }
    
    /**
     * Removes namespace prefix if present.
     * @param elementName
     * @return
     */
    private String removeNsPrefix(String elementName) {
        return elementName.indexOf(':') != -1 ? elementName.substring(elementName.indexOf(':') + 1) : elementName;
    }

    /**
     * Compiles WSDL file resource to a XmlObject.
     * @return
     * @throws MojoExecutionException
     * @throws IOException
     */
    private XmlObject compileWsdl() throws MojoExecutionException, IOException {
		Resource wsdlFile = new PathMatchingResourcePatternResolver().getResource(pathToWsdl);
		if (!wsdlFile.exists()) {
			throw new MojoExecutionException("Unable to read WSDL - does not exist in " + wsdlFile.getFile().getAbsolutePath());
		}
		
		if (!wsdlFile.getFile().canRead()) {
			throw new MojoExecutionException("Unable to read WSDL - could not open in read mode");
		}
		
		try {
			return XmlObject.Factory.parse(wsdlFile.getFile(), (new XmlOptions()).setLoadLineNumbers().setLoadMessageDigest().setCompileDownloadUrls());
		} catch (XmlException e) {
			for (Object error : e.getErrors()) {
				getLog().error(((XmlError)error).getLine() + "" + error.toString());
			}
			throw new MojoExecutionException("WSDL could not be parsed", e);
		} catch (Exception e) {
			throw new MojoExecutionException("WSDL could not be parsed", e);
		}
	}
    
    /**
     * Finds nested XML schema definition and compiles it to a schema type system instance.
     * @param wsdl
     * @return
     * @throws MojoExecutionException
     */
    private SchemaTypeSystem compileXsd(XmlObject wsdl) throws MojoExecutionException {
    	// extract namespaces defined on wsdl-level:
		String[] namespacesWsdl = extractNamespacesOnWsdlLevel(wsdl);
		
		// calc the namespace-prefix of the schema-tag, default ""
		String schemaNsPrefix = extractSchemaNamespacePrefix(wsdl);
		
		// extract each schema-element and add missing namespaces defined on wsdl-level
		String[] schemas = getNestedSchemas(wsdl, namespacesWsdl, schemaNsPrefix);
		              
		XmlObject[] xsd = new XmlObject[schemas.length];
		try {
			for (int i=0; i < schemas.length; i++) {
				xsd[i] = XmlObject.Factory.parse(schemas[i], (new XmlOptions()).setLoadLineNumbers().setLoadMessageDigest().setCompileDownloadUrls());
			}
		} catch (Exception e) {
			throw new MojoExecutionException("Failed to parse XSD schema", e);
		}
		
		SchemaTypeSystem schemaTypeSystem = null;
		try {
			schemaTypeSystem = XmlBeans.compileXsd(xsd, XmlBeans.getContextTypeLoader(), new XmlOptions());
		} catch (XmlException e) {
			for (Object error : e.getErrors()) {
				getLog().error("Line " + ((XmlError)error).getLine() + ": " + error.toString());
			}
			throw new MojoExecutionException("Failed to compile XSD schema", e);
		} catch (Exception e) {
			throw new MojoExecutionException("Failed to compile XSD schema", e);
		}
		return schemaTypeSystem;
    }

    /**
     * Finds nested schema definitions and puts globally WSDL defined namespaces to schema level.
     * 
     * @param wsdl
     * @param namespacesWsdl
     * @param schemaNsPrefix
     */
	private String[] getNestedSchemas(XmlObject wsdl, String[] namespacesWsdl, String schemaNsPrefix) {
	    List<String> schemas = new ArrayList<String>();
	    String openedStartTag = "<" + schemaNsPrefix + "schema";
        String endTag = "</" + schemaNsPrefix + "schema>";
        
		int cursor = 0;
		while (wsdl.xmlText().indexOf(openedStartTag, cursor) != -1) {
			int begin = wsdl.xmlText().indexOf(openedStartTag, cursor);
			int end = wsdl.xmlText().indexOf(endTag, begin) + endTag.length();
			int insertPointNamespacesWsdl = wsdl.xmlText().indexOf(" ", begin);
			
			StringBuffer buf = new StringBuffer();
			buf.append(wsdl.xmlText().substring(begin, insertPointNamespacesWsdl)).append(" ");
			
			for (String nsWsdl : namespacesWsdl) {
				String nsPrefix = nsWsdl.substring(0, nsWsdl.indexOf("="));
				if (!wsdl.xmlText().substring(begin, end).contains(nsPrefix)) {
					buf.append(nsWsdl).append(" ");
				}
			}
			
			buf.append(wsdl.xmlText().substring(insertPointNamespacesWsdl, end));
			schemas.add(buf.toString());
			cursor = end;
		}
		
		return schemas.toArray(new String[] {});
	}

	/**
	 * Finds schema tag and extracts the namespace prefix.
	 * @param wsdl
	 * @return
	 */
	private String extractSchemaNamespacePrefix(XmlObject wsdl) {
		String schemaNsPrefix = "";
		if (wsdl.xmlText().contains(":schema")) {
			int cursor = wsdl.xmlText().indexOf(":schema");
			for (int i = cursor; i > cursor - 100; i--) {
				schemaNsPrefix = wsdl.xmlText().substring(i, cursor);
				if (schemaNsPrefix.startsWith("<")) {
					return schemaNsPrefix.substring(1) + ":";
				}
			}
		}
		return schemaNsPrefix;
	}

	/**
	 * Returns an array of all namespace declarations, found on wsdl-level.
	 * 
	 * @param wsdl
	 * @return
	 */
	private String[] extractNamespacesOnWsdlLevel(XmlObject wsdl) {
		int cursor = wsdl.xmlText().indexOf(":") + ":definitions ".length();
		String nsWsdlOrig = wsdl.xmlText().substring(cursor, wsdl.xmlText().indexOf(">", cursor));
		int noNs = StringUtils.countOccurrencesOf(nsWsdlOrig, "xmlns:");
		String[] namespacesWsdl = new String[noNs];
		cursor = 0;
		for (int i=0; i<noNs; i++) {
			int begin = nsWsdlOrig.indexOf("xmlns:", cursor);
			int end = nsWsdlOrig.indexOf("\"", begin + 20);
			namespacesWsdl[i] = nsWsdlOrig.substring(begin, end) + "\"";
			cursor = end;
		}
		return namespacesWsdl;
	}
    
    /**
     * Returns the value of an xml-attribute
     * 
     * @param rootObject
     * @param pathToAttribute
     * @return
     * @throws MojoExecutionException 
     */
    private String evaluateAsString(XmlObject rootObject, String pathToAttribute) throws MojoExecutionException {
    	XmlObject[] xmlObject = rootObject.selectPath(pathToAttribute);
    	
    	if (xmlObject.length == 0) {
    	    throw new MojoExecutionException("Unable to find element attribute " + pathToAttribute);
    	}
    	
    	int begin = xmlObject[0].xmlText().indexOf(">") + 1;
    	int end = xmlObject[0].xmlText().lastIndexOf("</");
    	return xmlObject[0].xmlText().substring(begin, end);
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
     * Sets the prompter.
     * @param prompter the prompter to set
     */
    public void setPrompter(Prompter prompter) {
        this.prompter = prompter;
    }

    /**
     * Sets the interactiveMode.
     * @param interactiveMode the interactiveMode to set
     */
    public void setInteractiveMode(boolean interactiveMode) {
        this.interactiveMode = interactiveMode;
    }
}
