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

package com.consol.citrus;

import java.io.*;
import java.util.*;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.TestNG;
import org.testng.xml.*;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.TestEngineFailedException;

/**
 * Citrus command line application.
 *
 * @author Christoph Deppisch
 * @since 2008
 */
public final class Citrus {
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(Citrus.class);
    
    /** XML file extension */
    private static final String XML_FILE_EXTENSION = ".xml";

    /** Command line arguments */
    private CommandLine cmdArgs;
    
    /** TestNG */
    private TestNG testng = new TestNG(true);
    
    /**
     * Default constructor.
     * @param cmdArgs the command line arguments.
     */
    public Citrus(CommandLine cmdArgs) {
        this.cmdArgs = cmdArgs;
    }
    
    /**
     * Main CLI method.
     * @param args
     */
    public static void main(String[] args) {
        Options options = new CitrusCliOptions();
        HelpFormatter formatter = new HelpFormatter();
        
        try {
            CommandLine cmd = new GnuParser().parse(options, args);

            if (cmd.hasOption("help")) {
                formatter.printHelp("CITRUS TestFramework", options);
                return;
            }
            
            Citrus citrus = new Citrus(cmd);
            citrus.run();
        } catch (ParseException e) {
            log.error("Failed to parse command line arguments", e);
            formatter.printHelp("CITRUS TestFramework", options);
        }
    }
    
    /**
     * Runs all tests using TestNG.
     */
    public void run() {
        log.info("CITRUS TESTFRAMEWORK ");
        log.info("");
        
        String testDirectory = cmdArgs.getOptionValue("testdir", CitrusConstants.DEFAULT_TEST_DIRECTORY);
        
        if (!testDirectory.endsWith("/")) {
            testDirectory = testDirectory + "/";
        }
        
        XmlSuite suite = new XmlSuite();
        suite.setName(cmdArgs.getOptionValue("suitename", "citrus-test-suite"));
        
        if (cmdArgs.hasOption("test")) {
            for (String testName : cmdArgs.getOptionValues("test")) {
                addTest(testName, testDirectory, suite);
            }
        }
        
        if (cmdArgs.hasOption("package")) {
            for (String packageName : cmdArgs.getOptionValues("package")) {
                addTest(packageName, suite);
            }
        }

        if (cmdArgs.getArgList().size() > 0) {
            testng.setTestSuites(getTestSuites(cmdArgs.getArgs()));
        }
        
        List<XmlSuite> suites = new ArrayList<XmlSuite>();
        suites.add(suite);
        testng.setXmlSuites(suites);
        testng.run();
        
        if (testng.hasFailure()) {
            throw new TestEngineFailedException("Citrus test run failed!");
        }
    }
    
    /**
     * Get suites defined in external testng files.
     * @param testNgXmlArgs
     * @return
     */
    private List<String> getTestSuites(String[] testNgXmlArgs) {
        List<String> suites = new ArrayList<String>(); 
        for (String testNgXmlFile : testNgXmlArgs) {
            if (testNgXmlFile.endsWith(XML_FILE_EXTENSION)) {
                suites.add(testNgXmlFile);
            } else {
                log.warn("Unrecognized argument '" + testNgXmlFile + "'");
            }
        }
        
        return suites;
    }

    /**
     * Adds all tests in package to test suite.
     * @param packageName the test package.
     * @param suite the XML suite.
     */
    private void addTest(String packageName, XmlSuite suite) {
        XmlTest test = new XmlTest(suite);
        test.setName(packageName);
        
        XmlPackage xmlPackage = new XmlPackage();
        xmlPackage.setName(packageName);
        test.setXmlPackages(Collections.singletonList(xmlPackage));
    }

    /**
     * Adds a new XML class to test suite.
     * @param testName the test name.
     * @param testDirectory the test directory.
     * @param suite the XML suite.
     */
    private void addTest(String testName, String testDirectory, XmlSuite suite) {
        XmlTest test = new XmlTest(suite);
        test.setName(testName);
        try {
            test.setXmlClasses(Collections.singletonList(
                    new XmlClass(getClassNameForTest(testDirectory, testName.trim()))));
        } catch (FileNotFoundException e) {
            throw new TestEngineFailedException("TestSuite failed with error", e);
        }
    }

    /**
     * Method to retrieve the full class name for a test.
     * Hierarchy of folders is supported, too.
     *
     * @param startDir directory where to start the search
     * @param testName test name to search for
     * @throws CitrusRuntimeException
     * @return the class name of the test
     */
    private String getClassNameForTest(final String startDir, final String testName)
        throws FileNotFoundException {
        /* Stack to hold potential sub directories */
        final Stack<File> dirs = new Stack<File>();
        /* start directory */
        final File startdir = new File(startDir);

        if (startdir.isDirectory()) {
            dirs.push(startdir);
        }

        log.info("Starting test search in dir: " + startdir.getAbsolutePath());
        
        /* walk through the directories */
        while (dirs.size() > 0) {
            File file = dirs.pop();
            File[] found = file.listFiles(new TestCaseFileNameFilter());

            for (int i = 0; i < found.length; i++) {
                /* Subfolder support */
                if (found[i].isDirectory()) {
                    dirs.push(found[i]);
                } else {
                    if ((testName + XML_FILE_EXTENSION).equalsIgnoreCase(found[i].getName())) {
                        String fileName = found[i].getPath();
                        fileName = fileName.substring(0, (fileName.length() - XML_FILE_EXTENSION.length()));

                        if (fileName.startsWith(File.separator)) {
                            fileName = fileName.substring(File.separator.length());
                        }
                        
                        //replace operating system path separator and translate to class package string
                        fileName = fileName.substring(startDir.length()).replace(File.separatorChar, '.');
                        
                        if (log.isDebugEnabled()) {
                            log.debug("Found test '" + fileName + "'");
                        }
                        
                        return fileName;
                    }
                }
            }
        }
        
        throw new CitrusRuntimeException("Could not find test with name '"
                + testName + "'. Test directory is: " + startDir);
    }
    
    /**
     * Filter for test case files (usually .xml files)
     */
    private static final class TestCaseFileNameFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            File tmp = new File(dir.getPath() + "/" + name);

            /* Only allowing XML files as spring configuration files */
            return (name.endsWith(XML_FILE_EXTENSION) || tmp.isDirectory()) && !name.startsWith("CVS") && !name.startsWith(".svn");
        }
    }

    /**
     * Sets the testng.
     * @param testng the testng to set
     */
    public void setTestNG(TestNG testng) {
        this.testng = testng;
    }
}
