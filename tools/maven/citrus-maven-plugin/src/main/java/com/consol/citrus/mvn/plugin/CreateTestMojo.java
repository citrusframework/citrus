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
import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.xsd2inst.SampleXmlUtil;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
                    ReqResXmlTestCreator creator = getReqResXmlTestCaseCreator();

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
                    ReqResXmlTestCreator creator = getReqResXmlTestCaseCreator();

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
    public void createWithXsd(ReqResXmlTestCreator creator) throws MojoExecutionException {
        try {
            String xsd = this.xsd;
            while(interactiveMode && !StringUtils.hasText(xsd)) {
                xsd = prompter.prompt("Enter path to XSD", this.xsd);
            }

            // compile xsd already here, otherwise later input is useless:
            SchemaTypeSystem sts = compileXsd(xsd);
            SchemaType[] globalElems = sts.documentTypes();

            SchemaType requestElem = null;
            SchemaType responseElem = null;

            String xsdRequestMessage = this.xsdRequestMessage;
            String xsdResponseMessage = this.xsdResponseMessage;
            if (interactiveMode) {
                xsdRequestMessage = prompter.prompt("Enter request element name", this.xsdRequestMessage);

                // try to guess the response-element and the testname from the request:
                String xsdResponseMessageSuggestion = xsdResponseMessage;
                if (xsdRequestMessage.endsWith("Req")) {
                    xsdResponseMessageSuggestion = xsdRequestMessage.substring(0, xsdRequestMessage.indexOf("Req")) + "Res";
                    creator.withName(xsdRequestMessage.substring(0, xsdRequestMessage.indexOf("Req")) + "Test");
                } else if (xsdRequestMessage.endsWith("Request")) {
                    xsdResponseMessageSuggestion = xsdRequestMessage.substring(0, xsdRequestMessage.indexOf("Request")) + "Response";
                    creator.withName(xsdRequestMessage.substring(0, xsdRequestMessage.indexOf("Request")) + "Test");
                } else if (xsdRequestMessage.endsWith("RequestMessage")) {
                    xsdResponseMessageSuggestion = xsdRequestMessage.substring(0, xsdRequestMessage.indexOf("RequestMessage")) + "ResponseMessage";
                    creator.withName(xsdRequestMessage.substring(0, xsdRequestMessage.indexOf("RequestMessage")) + "Test");
                }

                xsdResponseMessage = prompter.prompt("Enter response element name", xsdResponseMessageSuggestion);
            }

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

            for (SchemaType elem : globalElems) {
                if (elem.getContentModel().getName().getLocalPart().equals(xsdRequestMessage)) {
                    requestElem = elem;
                    break;
                }
            }

            for (SchemaType elem : globalElems) {
                if (elem.getContentModel().getName().getLocalPart().equals(xsdResponseMessage)) {
                    responseElem = elem;
                    break;
                }
            }

            // Now generate it
            creator.withRequest(SampleXmlUtil.createSampleForType(requestElem))
                   .withResponse(SampleXmlUtil.createSampleForType(responseElem));

            creator.create();

            getLog().info("Successfully created new test case " + creator.getTargetPackage() + "." + creator.getName());
        } catch (ArrayIndexOutOfBoundsException e) {
            getLog().info("Wrong parameter usage! See citrus:help for usage details (mvn citrus:help -Ddetail=true -Dgoal=create-test-from-xsd).");
        } catch (PrompterException e) {
            getLog().info(e);
            getLog().info("Failed to create test! See citrus:help for usage details (mvn citrus:help -Ddetail=true -Dgoal=create-test-from-xsd).");
        } catch (IOException e) {
            getLog().info(e);
        }
    }

    /**
     * Creates test case with request and response messages from XML schema.
     * @param creator
     * @throws MojoExecutionException
     */
    public void createWithWsdl(ReqResXmlTestCreator creator) throws MojoExecutionException {
        String separator = "+++++++++++++++++++++++++++++++++++";

        try {
            String wsdl = this.wsdl;
            while (interactiveMode && !StringUtils.hasText(wsdl)) {
                wsdl = prompter.prompt("Enter path to WSDL", this.wsdl);
            }

            if (!StringUtils.hasText(wsdl)) {
                throw new MojoExecutionException("Please provide proper path to WSDL file");
            }

            String wsdlNsDelaration = "declare namespace wsdl='http://schemas.xmlsoap.org/wsdl/' ";

            // compile wsdl and xsds right now, otherwise later input is useless:
            XmlObject wsdlObject = compileWsdl(wsdl);
            SchemaTypeSystem schemaTypeSystem = compileXsd(wsdlObject);

            getLog().info(separator);
            getLog().info("WSDL compilation successful");
            String serviceName = evaluateAsString(wsdlObject, wsdlNsDelaration + ".//wsdl:portType/@name");
            getLog().info("Found service: " + serviceName);

            getLog().info(separator);
            getLog().info("Found service operations:");
            XmlObject[] messages = wsdlObject.selectPath(wsdlNsDelaration + ".//wsdl:message");
            XmlObject[] operations = wsdlObject.selectPath(wsdlNsDelaration + ".//wsdl:portType/wsdl:operation");
            for (XmlObject operation : operations) {
                getLog().info(evaluateAsString(operation, wsdlNsDelaration + "./@name"));
            }
            getLog().info(separator);

            getLog().info("Generating test cases for service operations ...");
            String nameSuffix = this.nameSuffix;
            if (interactiveMode) {
                nameSuffix = prompter.prompt("Enter test name suffix", this.nameSuffix);
            }

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

                String testName = creator.getName() + operationName + nameSuffix;

                // Now generate it
                creator.withName(testName);
                creator.withRequest(SampleXmlUtil.createSampleForType(requestElem))
                       .withResponse(SampleXmlUtil.createSampleForType(responseElem));

                creator.create();

                getLog().info("Successfully created new test case " + creator.getTargetPackage() + "." + testName);
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
    private XmlObject compileWsdl(String wsdl) throws MojoExecutionException, IOException {
        File wsdlFile;
        try {
            wsdlFile = new PathMatchingResourcePatternResolver().getResource(wsdl).getFile();
        } catch (FileNotFoundException e) {
            wsdlFile = new File(wsdl);
        }

        if (!wsdlFile.exists()) {
            throw new MojoExecutionException("Unable to read WSDL - does not exist in " + wsdlFile.getAbsolutePath());
        }

        if (!wsdlFile.canRead()) {
            throw new MojoExecutionException("Unable to read WSDL - could not open in read mode");
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
     * Finds nested XML schema definition and compiles it to a schema type system instance
     * @param xsd
     * @return
     * @throws MojoExecutionException
     */
    private SchemaTypeSystem compileXsd(String xsd) throws MojoExecutionException, IOException {
        File xsdFile;
        try {
            xsdFile = new PathMatchingResourcePatternResolver().getResource(xsd).getFile();
        } catch (FileNotFoundException e) {
            xsdFile = new File(xsd);
        }

        if (!xsdFile.exists()) {
            throw new MojoExecutionException("Unable to read XSD - does not exist in " + xsdFile.getAbsolutePath());
        }
        if (!xsdFile.canRead()) {
            throw new MojoExecutionException("Unable to read XSD - could not open in read mode");
        }

        XmlObject xsdObject;
        try {
            xsdObject = XmlObject.Factory.parse(xsdFile, (new XmlOptions()).setLoadLineNumbers().setLoadMessageDigest().setCompileDownloadUrls());
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to parse XSD schema", e);
        }
        XmlObject[] schemas = new XmlObject[] { xsdObject };
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
    public XmlTestCreator getXmlTestCaseCreator() {
        return new XmlTestCreator();
    }

    /**
     * Method provides test creator instance. Basically introduced for better mocking capabilities in unit tests but
     * also useful for subclasses to provide customized creator instance.
     * .
     * @return test creator.
     */
    public ReqResXmlTestCreator getReqResXmlTestCaseCreator() {
        return new ReqResXmlTestCreator();
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
