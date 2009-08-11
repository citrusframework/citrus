package com.consol.citrus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.consol.citrus.exceptions.TestEngineFailedException;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;

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

    /** Common decimal format for percentage calculation in report **/
    private static DecimalFormat decFormat = new DecimalFormat("0.0");

    /**
     * Main method doing all work
     * @param args
     */
    public static void main(String[] args) {
        /* list to hold all test defining xml files */
        List<String> testFiles = new ArrayList<String>();

        /* Build root application context without any test files */
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] { TestConstants.DEFAULT_APPLICATIONCONTEXT,
                               "com/consol/citrus/functions/citrus-function-ctx.xml" });
        
        ctx.setAllowBeanDefinitionOverriding(false);
        
        log.info("CITRUS TESTFRAMEWORK ");
        log.info("");

        log.info("Loading configuration");

        Options options = new CitrusCliOptions();
        CommandLineParser cliParser = new GnuParser();
        
        CommandLine cmd = null;
        
        try {
            cmd = cliParser.parse(options, args);
            
            String testDirectory = null;
            if (cmd.hasOption("testdir")) {
                testDirectory = cmd.getOptionValue("testdir");
            } else {
                testDirectory = TestConstants.DEFAULT_TEST_DIRECTORY;
            }
            
            /* check if command line arguments contain test-names to be executed explicitly */
            if (cmd.hasOption("test")) {
                /* search test files in test directory for the specific tests and load add them to context list */
                for (String testName : cmd.getOptionValues("test")) {
                    testFiles.add(FileUtils.getTestFileForTest(testDirectory + "/", testName.trim(), FileUtils.XML_FILE_EXTENSION));
                }
            } else {
                /* no test-names in arguments - load all test files available */
                testFiles = FileUtils.getTestFiles(testDirectory + "/", FileUtils.XML_FILE_EXTENSION).getFileNames();
            }

            testFiles.add("com/consol/citrus/spring/internal-helper-ctx.xml");
            
            /* Load testFiles as applicationContext using root applicationContext as parent */
            ClassPathXmlApplicationContext testContext = new ClassPathXmlApplicationContext(testFiles.toArray(new String[testFiles.size()]), ctx);
            testContext.setAllowBeanDefinitionOverriding(false);
    
            /* Searching for at least one defined TestSuite bean to execute */
            String[] testSuites = null;
            if(cmd.hasOption("testsuite")) {
                testSuites = cmd.getOptionValues("testsuite");
            } else {
                testSuites = ctx.getBeanNamesForType(TestSuite.class);
            }
    
            if (testSuites == null || testSuites.length == 0) {
                log.error("Missing TestSuite configuration in spring context file");
                throw new TestEngineFailedException("TestSuite failed with error - Missing TestSuite configuration in spring context file");
            }
    
            /*
             * Either searching for all given tests by type TestCase or load names
             * of test cases from arguments
             */
            final TestCase[] tests;
            if (cmd.hasOption("test")) {
                tests = new TestCase[cmd.getOptionValues("test").length];
                for (int i = 0; i < cmd.getOptionValues("test").length; i++) {
                    tests[i] = (TestCase)testContext.getBean(cmd.getOptionValues("test")[i].trim(), TestCase.class);
                }
                log.info("Found " + tests.length + " tests in arguments");
            } else {
                final String[] testNames = testContext.getBeanNamesForType(TestCase.class);
    
                tests = new TestCase[testNames.length];
                for (int i = 0; i < testNames.length; i++) {
                    tests[i] = (TestCase)testContext.getBean(testNames[i], TestCase.class);
                }
    
                log.info("Found " + tests.length + " tests in testdirectory");
            }
    
            log.info("");
    
            boolean exitWithError = false;
            /* finally executing all found test suite instances */
            for (int i = 0; i < testSuites.length; i++) {
                TestSuite testSuite = (TestSuite) testContext.getBean(testSuites[i]);
    
                if (testSuite.beforeSuite()) {
                    if (tests.length == 1 && cmd.hasOption("test")) {
                        if (!testSuite.run(tests[0]))
                            exitWithError = true;
                    } else {
                        if (!testSuite.run(tests))
                            exitWithError = true;
                    }
    
                    if (!testSuite.afterSuite())
                        exitWithError =true;
                } else {
                    exitWithError = true;
                }
    
                if (testSuite.hasFailedTests())
                    exitWithError = true;
            }
    
            int cntSuccess = 0;
            int cntFail = 0;
    
            /* now printing result of all test suite instances */
            for (int i = 0; i < testSuites.length; i++) {
                TestSuite testSuite = (TestSuite) testContext.getBean(testSuites[i]);
    
                cntSuccess += testSuite.getCntCasesSuccess();
                cntFail += testSuite.getCntCasesFail();
            }
    
            if (testSuites.length > 1) {
                printResult(cntSuccess, cntFail);
            }
    
            if (exitWithError)
                throw new TestEngineFailedException("TestSuite failed with error - " + cntFail + " test(s) failed");
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("CITRUS TestFramework", options);
        } catch (FileNotFoundException e) {
            log.error("Failed to load test files", e);
            throw new TestEngineFailedException("TestSuite failed with error", e);
        } catch (IOException e) {
            log.error("Error while accessing test file", e);
            throw new TestEngineFailedException("TestSuite failed with error", e);
        }  catch (CitrusRuntimeException e) {
            log.error("TestEngine failed with error", e);
            throw new TestEngineFailedException("TestSuite failed with error", e);
        }
    }

    /**
     * Prints the test results to logger.
     */
    private static void printResult(int cntCasesSuccess, int cntCasesFail) {
        if (log.isInfoEnabled()) {
            log.info("________________________________________________________________________");
            log.info("");
            log.info("OVERALL TEST RESULTS");
            log.info("");

            log.info("");
            log.info("Found " + (cntCasesSuccess+cntCasesFail) + " test cases to execute");
            log.info("Executed " + (cntCasesFail+cntCasesSuccess) + " test cases");
            log.info("Tests failed: \t\t" + cntCasesFail + " (" + decFormat.format((double)cntCasesFail/(cntCasesFail+cntCasesSuccess)*100) + "%)");
            log.info("Tests successfully: \t" + cntCasesSuccess + " (" + decFormat.format((double)cntCasesSuccess/(cntCasesFail+cntCasesSuccess)*100) + "%)");

            log.info("________________________________________________________________________");
        }
    }
}
