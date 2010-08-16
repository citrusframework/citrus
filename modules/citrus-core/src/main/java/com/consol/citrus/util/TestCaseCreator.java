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

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Properties;

import org.apache.commons.cli.*;

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.exceptions.CitrusRuntimeException;

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
    
    /** Target unit testing framework */
    private UnitFramework framework;
    
    /**
     * Unit testing framework can be either JUnit or TestNG. Test case creator
     * will create different Java classes according to the unit test framework.
     */
    public static enum UnitFramework {
        TESTNG, JUNIT3, JUNIT4;
        
        public static UnitFramework fromString(String value) {
            if(value.equalsIgnoreCase("testng")) {
                return TESTNG;
            } else if(value.equalsIgnoreCase("junit3")) {
                return JUNIT3;
            } else if(value.equalsIgnoreCase("junit4")) {
                return JUNIT4;
            } else if(value.equalsIgnoreCase("junit")) {
                return JUNIT3;
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
        CommandLineParser cliParser = new GnuParser();
        
        CommandLine cmd = null;
        
        try {
            cmd = cliParser.parse(options, args);
            
            if(cmd.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("CITRUS test creation", options);
                return;
            }
            
            TestCaseCreator creator = TestCaseCreator.build()
                .withName(cmd.getOptionValue("name"))
                .withAuthor(cmd.getOptionValue("author", "Unknown"))
                .withDescription(cmd.getOptionValue("description", "TODO: Description"))
                .usePackage(cmd.getOptionValue("package", "com.consol.citrus"))
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
        if(Character.isLowerCase(name.charAt(0))) {
            throw new CitrusRuntimeException("Test name must start with an uppercase letter");
        }
        
        Properties properties = new Properties();
        properties.put("test.name", name);
        properties.put("test.author", author);
        properties.put("test.description", description);
        
        properties.put("test.updatedon.datetime", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(GregorianCalendar.getInstance().getTime()));
        properties.put("test.creation.date", new SimpleDateFormat("yyyy-MM-dd").format(GregorianCalendar.getInstance().getTime()));
        
        properties.put("test.method.name", name.substring(0,1).toLowerCase() + name.substring(1));
        properties.put("test.package", targetPackage);
        
        targetPackage = targetPackage.replace('.', '/');
        
        createFileFromTemplate(properties,
                CitrusConstants.DEFAULT_TEST_DIRECTORY + targetPackage + "/" + name + ".xml",
                getTemplateFileForXMLTest());
        
        createFileFromTemplate(properties, 
                CitrusConstants.DEFAULT_JAVA_DIRECTORY + targetPackage + "/" + name + ".java", 
                getTemplateFileForJavaClass());
    }
    
    /**
     * Creates test case files from template files replacing
     * properties in template file.
     * @param properties to replace placeholders in template file
     * @param filePath target file path
     * @param templateFilePath template file path
     */
    private void createFileFromTemplate(Properties properties, String filePath, String templateFilePath) {
        BufferedReader reader = null;
        OutputStream buffered = null;
        
        try {
            File file = new File(filePath);
            if(!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
                
            buffered = new BufferedOutputStream(new FileOutputStream(file));
            reader = new BufferedReader(new InputStreamReader(TestCaseCreator.class.getResourceAsStream(templateFilePath)));
            
            StringWriter sWriter = new StringWriter();
            
            String line;
            while ((line = reader.readLine()) != null) {
                sWriter.append(PropertyUtils.replacePropertiesInString(line, properties) + "\n");
            }
    
            buffered.write(sWriter.toString().getBytes());
            buffered.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(buffered != null) {
                    buffered.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            try {
                if(reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
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
    private String getTemplateFileForXMLTest() {
        return "test-template.xml";
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
     * Set the unit testing framework to use.
     * @param framework
     * @return
     */
    public TestCaseCreator withFramework(UnitFramework framework) {
        this.framework = framework;
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
