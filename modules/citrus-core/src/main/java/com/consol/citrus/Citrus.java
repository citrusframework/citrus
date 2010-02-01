/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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
public class Citrus {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(Citrus.class);

    /**
     * Prevent instanciation.
     */
    private Citrus() {}
    
    /**
     * Main CLI method.
     * @param args
     */
    public static void main(String[] args) {
        log.info("CITRUS TESTFRAMEWORK ");
        log.info("");

        Options options = new CitrusCliOptions();
        CommandLineParser cliParser = new GnuParser();
        
        CommandLine cmd = null;
        
        try {
            cmd = cliParser.parse(options, args);
            
            if(cmd.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("CITRUS TestFramework", options);
                
                return;
            }
            
            String testDirectory = cmd.getOptionValue("testdir", CitrusConstants.DEFAULT_TEST_DIRECTORY);
            
            if(!testDirectory.endsWith("/")) {
                testDirectory = testDirectory + "/";
            }
            
            TestNG testNG = new TestNG(false);
            
            XmlSuite suite = new XmlSuite();
            suite.setName(cmd.getOptionValue("suitename", CitrusConstants.DEFAULT_SUITE_NAME));
            
            if(cmd.hasOption("test")) {
                for (String testName : cmd.getOptionValues("test")) {
                    XmlTest test = new XmlTest(suite);
                    test.setName(testName);
                    
                    test.setXmlClasses(Collections.singletonList(new XmlClass(getClassNameForTest(testDirectory, testName.trim()))));
                }
            }
            
            if(cmd.hasOption("package")) {
                for (String packageName : cmd.getOptionValues("package")) {
                    XmlTest test = new XmlTest(suite);
                    test.setName(packageName);
                    
                    XmlPackage xmlPackage = new XmlPackage();
                    xmlPackage.setName(packageName);
                    test.setXmlPackages(Collections.singletonList(xmlPackage));
                }
            }

            if(cmd.getArgList().size() > 0) {
                List<String> testNgXml = new ArrayList<String>(); 
                for (String testNgXmlFile : cmd.getArgs()) {
                    if(testNgXmlFile.endsWith(".xml")) {
                        testNgXml.add(testNgXmlFile);
                    } else {
                        log.warn("Unrecognized argument '" + testNgXmlFile + "'");
                    }
                }
                testNG.setTestSuites(testNgXml);
            }
            
            List<XmlSuite> suites = new ArrayList<XmlSuite>();
            suites.add(suite);
            testNG.setXmlSuites(suites);
            testNG.run();
            
            System.exit(testNG.getStatus());
        } catch (ParseException e) {
            log.error("Failed to parse command line arguments", e);
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("CITRUS TestFramework", options);
        } catch (FileNotFoundException e) {
            log.error("Failed to load test files", e);
            throw new TestEngineFailedException("TestSuite failed with error", e);
        } catch (IOException e) {
            log.error("Error while accessing test file", e);
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
    public static String getClassNameForTest(final String startDir, final String testName)
        throws IOException, FileNotFoundException {
        /* Stack to hold potential sub directories */
        final Stack<File> dirs = new Stack<File>();
        /* start directory */
        final File startdir = new File(startDir);

        if (startdir.isDirectory()) {
            dirs.push(startdir);
        }

        /* walk through the directories */
        while (dirs.size() > 0) {
            File file = dirs.pop();
            File[] found = file.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    File tmp = new File(dir.getPath() + "/" + name);

                    /* Only allowing XML files as spring configuration files */
                    if ((name.endsWith(".xml") || tmp.isDirectory()) && !name.startsWith("CVS") && !name.startsWith(".svn")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            for (int i = 0; i < found.length; i++) {
                /* Subfolder support */
                if (found[i].isDirectory()) {
                    dirs.push(found[i]);
                } else {
                    if ((testName + ".xml").equalsIgnoreCase(found[i].getName())) {
                        String fileName = found[i].getPath();
                        fileName = fileName.substring(0, (fileName.length()-".xml".length()));

                        if(fileName.startsWith(File.separator)) {
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
}
