package com.consol.citrus;

import java.io.*;
import java.util.Collections;
import java.util.Stack;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.TestNG;
import org.testng.xml.*;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.TestEngineFailedException;

/**
 * Runs the test suite using applicationContext.xml and test.properties.
 *
 * @author deppisch Christoph Deppisch Consol* Software GmbH
 * @since 04.11.2008
 */
public class CitrusApplication {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(CitrusApplication.class);

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
            
            if(testDirectory.endsWith("/") == false) {
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
            
            testNG.setXmlSuites(Collections.singletonList(suite));
            testNG.run();
            
            System.exit(testNG.getStatus());
        } catch (ParseException e) {
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
     * Method to retrieve the full class name for the test name searched for.
     * Subfolders are supported.
     *
     * @param startDir directory where to start the search
     * @param testName test name to search for
     * @throws CitrusRuntimeException
     * @return the class name of the test
     */
    public static String getClassNameForTest(final String startDir, final String testName)
        throws IOException, FileNotFoundException {
        /* Stack to hold potential sub directories */
        final Stack dirs = new Stack();
        /* start directory */
        final File startdir = new File(startDir);

        if (startdir.isDirectory())
            dirs.push(startdir);

        /* walk through the directories */
        while (dirs.size() > 0) {
            File file = (File) dirs.pop();
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
                if (found[i].isDirectory())
                    dirs.push(found[i]);
                else {
                    if ((testName + ".xml").equalsIgnoreCase(found[i].getName())) {
                        String fileName = found[i].getPath();
                        fileName = fileName.substring(0, (fileName.length()-".xml".length()));
                        fileName = fileName.substring(startDir.length()).replace('\\', '.');
                        
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
