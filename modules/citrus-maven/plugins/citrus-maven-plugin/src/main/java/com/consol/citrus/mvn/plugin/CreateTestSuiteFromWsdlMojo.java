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
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.xsd2inst.SampleXmlUtil;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.consol.citrus.util.TestCaseCreator;
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
public class CreateTestSuiteFromWsdlMojo extends AbstractMojo {
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
     *          default-value="_ITest" */
    private String nameSuffix;
    
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
        	while(interactiveMode && !StringUtils.hasText(pathToWsdl)) {
        		pathToWsdl = prompter.prompt("Enter path to WSDL");
        	}
        	
        	// compile wsdl and xsds already here, otherwise later input is useless:
        	XmlObject wsdl = compileWsdl();
        	SchemaTypeSystem sts = compileXsd(wsdl);
        	getLog().info("--------");
        	getLog().info("WSDL was compiled successfully.");
        	String serviceName = getAttributeContent(wsdl, "declare namespace wsdl='http://schemas.xmlsoap.org/wsdl/' .//wsdl:portType/@name"); 
        	getLog().info("Following service was detected: " + serviceName);
        	
        	getLog().info("--------");
        	getLog().info("Following operations were detected for test-creation:");
    		XmlObject[] operations = wsdl.selectPath("declare namespace wsdl='http://schemas.xmlsoap.org/wsdl/' .//wsdl:portType/wsdl:operation");
    		for (XmlObject operation : operations) {
    			getLog().info(getAttributeContent(operation, "declare namespace wsdl='http://schemas.xmlsoap.org/wsdl/' ./@name"));
			}
    		getLog().info("--------");
        	
        	if (interactiveMode) {
        		namePrefix = prompter.prompt("Enter prefix of all test names", namePrefix);
        		nameSuffix = prompter.prompt("Enter suffix of all test names", nameSuffix);
        		author = prompter.prompt("Enter test author:", author);
        		description = prompter.prompt("Enter test description:", description);
        		targetPackage = prompter.prompt("Enter test package:", targetPackage);
        		framework = prompter.prompt("Choose unit test framework", framework);
        		
        		String confirm = prompter.prompt("Confirm test creation:\n" +
        		        "framework: " + framework + "\n" +
    			        "name e.g.: " + namePrefix + getAttributeContent(operations[0], "declare namespace wsdl='http://schemas.xmlsoap.org/wsdl/' ./@name") + nameSuffix + "\n" +
    					"author: " + author + "\n" +
    					"description: " + description + "\n" +
    					"package: " + targetPackage + "\n", CollectionUtils.arrayToList(new String[] {"y", "n"}), "y");
    	
		    	if (confirm.equalsIgnoreCase("n")) {
		    		return;
		    	}
        	}
        	
        	SchemaType[] globalElems = sts.documentTypes();
			
			for (XmlObject operation : operations) {
				String operationName = getAttributeContent(operation, "declare namespace wsdl='http://schemas.xmlsoap.org/wsdl/' ./@name");
				String inputElem = getAttributeContent(operation, "declare namespace wsdl='http://schemas.xmlsoap.org/wsdl/' ./wsdl:input/@message");
				String outputElem = getAttributeContent(operation, "declare namespace wsdl='http://schemas.xmlsoap.org/wsdl/' ./wsdl:output/@message");
				//remove ns-prefix:
				inputElem = inputElem.indexOf(':') != -1 ? inputElem.substring(inputElem.indexOf(':') + 1) : inputElem;
				outputElem = outputElem.indexOf(':')  != -1 ? outputElem.substring(outputElem.indexOf(':') + 1) : outputElem;

				SchemaType requestElem = null;
				SchemaType responseElem = null;
				for (SchemaType elem : globalElems) {
					if (elem.toString().contains(inputElem)) {
						requestElem = elem;
					}
					if (elem.toString().contains(outputElem)) {
						responseElem = elem;
					}
				}
				
				String requestXml = SampleXmlUtil.createSampleForType(requestElem);
				String responseXml = SampleXmlUtil.createSampleForType(responseElem);
				String name = namePrefix + operationName + nameSuffix;
				
				// Now generate it
				TestCaseCreator creator = TestCaseCreator.build()
					.withFramework(UnitFramework.fromString(framework))
					.withName(name)
					.withAuthor(author)
					.withDescription(description)
					.usePackage(targetPackage)
					.withXmlRequest(requestXml)
					.withXmlResponse(responseXml);
				
				creator.createTestCase();
				
				getLog().info("Successfully created new test case " + targetPackage + "." + name);
			}
			
        } catch (ArrayIndexOutOfBoundsException e) {
        	getLog().info(e);
            getLog().info("Wrong parameter usage! See citrus:help for usage details (mvn citrus:help -Ddetail=true -Dgoal=create-suite-from-wsdl).");
        } catch (PrompterException e) {
			getLog().info(e);
			getLog().info("Failed to create suite! See citrus:help for usage details (mvn citrus:help -Ddetail=true -Dgoal=create-suite-from-wsdl).");
		}
    }
    
    private XmlObject compileWsdl() throws MojoExecutionException {
		
		File wsdlFile = new File(pathToWsdl);
		if (!wsdlFile.exists()) {
			throw new MojoExecutionException("WSDL could not be found at path " + wsdlFile.getAbsolutePath());
		}
		if (!wsdlFile.canRead()) {
			throw new MojoExecutionException("WSDL could not be opened in read mode");
		}
		
		try {
			return XmlObject.Factory.parse(wsdlFile, (new XmlOptions()).setLoadLineNumbers().setLoadMessageDigest().setCompileDownloadUrls());
		} catch (XmlException e) {
			for (Object error : e.getErrors()) {
				getLog().error(((XmlError)error).getLine() + "" + error.toString());
			}
			throw new MojoExecutionException("WSDL could not be parsed", e);
		} catch (Exception e) {
			throw new MojoExecutionException("WSDL could not be parsed", e);
		}
	}
    
    private SchemaTypeSystem compileXsd(XmlObject wsdl) throws MojoExecutionException {
    	// extract namespaces defined on wsdl-level:
		String[] namespacesWsdl = extractNamespacesDefinedOnWsdlLevel(wsdl);
		
		// calc the namespace-prefix of the schema-tag, default ""
		String schemaNsPrefix = extractSchemaNamespacePrefix(wsdl);
		String openedStartTag = "<" + schemaNsPrefix + "schema";
		String endTag = "</" + schemaNsPrefix + "schema>";
		
		String schema[] = new String[StringUtils.countOccurrencesOf(wsdl.xmlText(), openedStartTag)];

		// extract each schema-element and add missing namespaces defined on wsdl-level:
		addNamespacesDefinedOnWsdlLevelToSchemaTags(wsdl, namespacesWsdl, openedStartTag, endTag, schema);
		              
		XmlObject[] xsd = new XmlObject[schema.length];
		try {
			for (int i=0; i < schema.length; i++) {
				xsd[i] = XmlObject.Factory.parse(schema[i], (new XmlOptions()).setLoadLineNumbers().setLoadMessageDigest().setCompileDownloadUrls());
			}
		} catch (Exception e) {
			throw new MojoExecutionException("XSD could not be parsed", e);
		}
		
		SchemaTypeSystem sts = null;
		try {
			sts = XmlBeans.compileXsd(xsd, XmlBeans.getContextTypeLoader(), new XmlOptions());
		} catch (XmlException e) {
			for (Object error : e.getErrors()) {
				getLog().error("Line " + ((XmlError)error).getLine() + ": " + error.toString());
			}
			throw new MojoExecutionException("XSD could not be compiled", e);
		} catch (Exception e) {
			throw new MojoExecutionException("XSD could not be compiled", e);
		}
		return sts;
    }

	private void addNamespacesDefinedOnWsdlLevelToSchemaTags(XmlObject wsdl,
			String[] namespacesWsdl, String openedStartTag, String endTag,
			String[] schema) {
		int cursor = 0;
		for (int i = 0; i < schema.length; i++) {
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
			schema[i] =  buf.toString();
			cursor = end;
		}
	}

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
	 * returns a string array with all Namespaces, which are defined on wsdl-level
	 * 
	 * @param wsdl
	 * @return
	 */
	private String[] extractNamespacesDefinedOnWsdlLevel(XmlObject wsdl) {
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
     */
    private String getAttributeContent(XmlObject rootObject, String pathToAttribute) {
    	XmlObject[] xmlObject = rootObject.selectPath(pathToAttribute);
    	
    	int begin = xmlObject[0].xmlText().indexOf(">") + 1;
    	int end = xmlObject[0].xmlText().lastIndexOf("</");
    	return xmlObject[0].xmlText().substring(begin, end);
    }
}
