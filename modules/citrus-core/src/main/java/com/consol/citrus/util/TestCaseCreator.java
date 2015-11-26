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

package com.consol.citrus.util;

import com.consol.citrus.Citrus;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Properties;

/**
 * CLI creating a new test case from a template.
 * 
 * @author Christoph Deppisch
 * @since 2009
 */
public class TestCaseCreator {
    /** Test name */
    private String name;
    
    /** Test author */
    private String author;
    
    /** Test description */
    private String description;
    
    /** Target package of test case */
    private String targetPackage;

    /** Source directory for tests */
    private String srcDirectory = Citrus.DEFAULT_TEST_SRC_DIRECTORY;
    
    /** Sample XML-Request */
    private String xmlRequest;
    
    /** Sample XML-Request */
    private String xmlResponse;
    
    /** Target unit testing framework */
    private UnitFramework framework;
    
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(TestCaseCreator.class);
    
    /**
     * Unit testing framework can be either JUnit or TestNG. Test case creator
     * will create different Java classes according to the unit test framework.
     */
    public static enum UnitFramework {
        TESTNG, JUNIT;
        
        public static UnitFramework fromString(String value) {
            if (value.equalsIgnoreCase("testng")) {
                return TESTNG;
            } else if (value.equalsIgnoreCase("junit")) {
                return JUNIT;
            } else {
                throw new IllegalArgumentException("Found unsupported unit test framework '" + value + "'");
            }
        }
    };
    
    /**
     * Main CLI method.
     * @param args
     */
    public static void main(String[] args) {
        Options options = new TestCaseCreatorCliOptions();

        try {
            CommandLineParser cliParser = new GnuParser();
            CommandLine cmd = cliParser.parse(options, args);
            
            if (cmd.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("CITRUS test creation", options);
                return;
            }
            
            TestCaseCreator creator = TestCaseCreator.build()
                .withName(cmd.getOptionValue("name"))
                .withAuthor(cmd.getOptionValue("author", "Unknown"))
                .withDescription(cmd.getOptionValue("description", "TODO: Description"))
                .usePackage(cmd.getOptionValue("package", "com.consol.citrus"))
                .useSrcDirectory(cmd.getOptionValue("srcdir", Citrus.DEFAULT_TEST_SRC_DIRECTORY))
                .withFramework(UnitFramework.fromString(cmd.getOptionValue("framework", "testng")));
            
            creator.createTestCase();
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("\n **** CITRUS TESTCREATOR ****", "\n CLI options:", options, "");
        }
    }

    /**
     * Create the test case.
     */
    public void createTestCase() {
        if (Character.isLowerCase(name.charAt(0))) {
            throw new CitrusRuntimeException("Test name must start with an uppercase letter");
        }
        
        Properties properties = prepareTestCaseProperties();
        
        targetPackage = targetPackage.replace('.', '/');
        
        createFileFromTemplate(properties,
                srcDirectory + File.separator + "resources" + File.separator + targetPackage + File.separator + name + ".xml",
                getTemplateFileForXMLTest(xmlRequest != null && xmlResponse != null));
        
        createFileFromTemplate(properties,
                srcDirectory + File.separator + "java" + File.separator + targetPackage + File.separator + name + ".java",
                getTemplateFileForJavaClass());
    }
    
    /**
     * Prepares the test case properties for dynamic property replacement in
     * test case templates.
     * 
     * @return the prepared property set.
     */
    private Properties prepareTestCaseProperties() {
        Properties properties = new Properties();
        properties.put("test.name", name);
        properties.put("test.author", author);
        properties.put("test.description", description);
        
        properties.put("test.updatedon.datetime", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(GregorianCalendar.getInstance().getTime()));
        properties.put("test.creation.date", new SimpleDateFormat("yyyy-MM-dd").format(GregorianCalendar.getInstance().getTime()));
        
        properties.put("test.method.name", name.substring(0,1).toLowerCase() + name.substring(1));
        properties.put("test.package", targetPackage);

        properties.put("test.src.directory", srcDirectory);

        if (xmlRequest != null && xmlResponse != null) {
            properties.put("test.request", xmlRequest);
            properties.put("test.response", xmlResponse);
        }
        
        return properties;
    }

    /**
     * Builds the Java file content based on a template file.
     * 
     * @return the Java test file content.
     */
    public String buildJavaFileContent() {
        return buildFileContentFromTemplate(prepareTestCaseProperties(), getTemplateFileForJavaClass());
    }
    
    /**
     * Builds ther XML file content based on a template file.
     * 
     * @return the XML test file content.
     */
    public String buildXmlFileContent() {
        return buildFileContentFromTemplate(prepareTestCaseProperties(), getTemplateFileForXMLTest(xmlRequest != null && xmlResponse != null));
    }
    
    /**
     * Read the given template file and replace all test case properties.
     * 
     * @param properties the dynamic test case properties.
     * @param templateFilePath the template file to use as base.
     * @return the final rest file content.
     */
    private String buildFileContentFromTemplate(Properties properties, String templateFilePath) {
        BufferedReader reader = null;
        StringBuilder contentBuilder = new StringBuilder();
        
        try {
            reader = new BufferedReader(new InputStreamReader(TestCaseCreator.class.getResourceAsStream(templateFilePath)));
            
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(PropertyUtils.replacePropertiesInString(line, properties));
                contentBuilder.append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new CitrusRuntimeException("Failed to create test case, unable to find test case template", e);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to create test case, error while accessing test case template file", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                log.error("Error while closing test case template file", e);
            }
        }
        
        return contentBuilder.toString();
    }
    
    /**
     * Creates test case files from template files replacing
     * properties in template file.
     * @param properties to replace placeholders in template file
     * @param filePath target file path
     * @param templateFilePath template file path
     */
    private void createFileFromTemplate(Properties properties, String filePath, String templateFilePath) {
        OutputStream buffered = null;
        
        try {
            File file = new File(filePath);
            if (!file.getParentFile().exists()) {
                boolean success = file.getParentFile().mkdirs();
                
                if (!success) {
                    throw new CitrusRuntimeException("Unable to create folder structure for test case");
                }
            }
                
            buffered = new BufferedOutputStream(new FileOutputStream(file));
            buffered.write(buildFileContentFromTemplate(properties, templateFilePath).getBytes());
            buffered.flush();
        } catch (FileNotFoundException e) {
            throw new CitrusRuntimeException("Failed to create test case", e);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to create test case, unable to access test file", e);
        } finally {
            try {
                if (buffered != null) {
                    buffered.close();
                }
            } catch (IOException e) {
                log.error("Error while closing test case file", e);
            }
        }
    }
    
    /**
     * Get the Java class template file according to the 
     * used unit testing framework.
     * @return file path of template file.
     */
    private String getTemplateFileForJavaClass() {
        return "java-" + framework.toString().toLowerCase() + "-template.txt";
    }
    
    /**
     * Get the XML test case file template.
     * @return file path of template file.
     */
    private String getTemplateFileForXMLTest(boolean isWithRequestAndResponse) {
        return isWithRequestAndResponse ? "test-req-res-template.xml" : "test-template.xml";
    }

    /**
     * Builder method for this creator.
     * @return
     */
    public static TestCaseCreator build() {
        return new TestCaseCreator();
    }
    
    /**
     * Set name via builder method.
     * @param name
     * @return
     */
    public TestCaseCreator withName(String name) {
        this.name = name;
        return this;
    }
    
    /**
     * Set author via builder method.
     * @param author
     * @return
     */
    public TestCaseCreator withAuthor(String author) {
        this.author = author;
        return this;
    }
    
    /**
     * Set description via builder method.
     * @param description
     * @return
     */
    public TestCaseCreator withDescription(String description) {
        this.description = description;
        return this;
    }
    
    /**
     * Set package via builder method.
     * @param targetPackage
     * @return
     */
    public TestCaseCreator usePackage(String targetPackage) {
        this.targetPackage = targetPackage;
        return this;
    }

    /**
     * Set test source directory via builder method.
     * @param srcDirectory
     * @return
     */
    public TestCaseCreator useSrcDirectory(String srcDirectory) {
        this.srcDirectory = srcDirectory;
        return this;
    }
    
    /**
     * Set the unit testing framework to use.
     * @param framework
     * @return
     */
    public TestCaseCreator withFramework(UnitFramework framework) {
        this.framework = framework;
        return this;
    }
    
    /**
     * Set the request to use.
     * @param xmlRequest
     * @return
     */
    public TestCaseCreator withXmlRequest(String xmlRequest) {
    	this.xmlRequest = xmlRequest;
    	return this;
    }
    
    /**
     * Set the response to use.
     * @param xmlResponse
     * @return
     */
    public TestCaseCreator withXmlResponse(String xmlResponse) {
    	this.xmlResponse = xmlResponse;
    	return this;
    }
    
    /**
     * CLI options for test creation
     */
    private static class TestCaseCreatorCliOptions extends Options {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings("static-access")
        public TestCaseCreatorCliOptions() {
            this.addOption(new Option("help", "print usage help"));
            
            this.addOption(OptionBuilder.withArgName("name")
                    .hasArg()
                    .withDescription("the test name (required)")
                    .isRequired(true)
                    .create("name"));
            
            this.addOption(OptionBuilder.withArgName("author")
                    .hasArg()
                    .withDescription("the author of the test (optional)")
                    .isRequired(false)
                    .create("author"));
            
            this.addOption(OptionBuilder.withArgName("description")
                    .hasArg()
                    .withDescription("describes the test (optional)")
                    .isRequired(false)
                    .create("description"));
            
            this.addOption(OptionBuilder.withArgName("package")
                    .hasArg()
                    .withDescription("the package to use (optional)")
                    .isRequired(false)
                    .create("package"));

            this.addOption(OptionBuilder.withArgName("srcdir")
                    .hasArg()
                    .withDescription("the test source directory to use (optional)")
                    .isRequired(false)
                    .create("srcdir"));
            
            this.addOption(OptionBuilder.withArgName("framework")
                    .hasArg()
                    .withDescription("the framework to use (optional) [testng, junit4, junit3]")
                    .isRequired(false)
                    .create("framework"));
        }        
    }

    /**
     * Set the test name.
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the test name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the test author.
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Get the test author.
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Set the test description.
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the test description.
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the test package.
     * @param targetPackage the targetPackage to set
     */
    public void setPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    /**
     * Get the test source directory.
     * @return the srcDirectory
     */
    public String getSrcDirectory() {
        return srcDirectory;
    }

    /**
     * Set the test source directory.
     * @param srcDirectory the srcDirectory to set
     */
    public void setSrcDirectory(String srcDirectory) {
        this.srcDirectory = srcDirectory;
    }

    /**
     * Get the test package.
     * @return the targetPackage
     */
    public String getPackage() {
        return targetPackage;
    }

    /**
     * Get the target package.
     * @return the targetPackage
     */
    public String getTargetPackage() {
        return targetPackage;
    }

    /**
     * Set the target package.
     * @param targetPackage the targetPackage to set
     */
    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    /**
     * Get the unit test framework (usually TestNG or JUnit).
     * @return the framework
     */
    public UnitFramework getFramework() {
        return framework;
    }

    /**
     * Set the unit test framework.
     * @param framework the framework to set
     */
    public void setFramework(UnitFramework framework) {
        this.framework = framework;
    }
}
