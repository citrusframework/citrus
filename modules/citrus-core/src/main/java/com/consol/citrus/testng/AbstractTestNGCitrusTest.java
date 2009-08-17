package com.consol.citrus.testng;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import com.consol.citrus.TestCase;
import com.consol.citrus.TestSuite;
import com.consol.citrus.TestCaseMetaInfo.Status;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.util.FileUtils;

@ContextConfiguration(locations = {"/com/consol/citrus/spring/root-application-ctx.xml", 
                                   "/application-ctx.xml", 
                                   "/com/consol/citrus/functions/citrus-function-ctx.xml"})
public abstract class AbstractTestNGCitrusTest extends AbstractTestNGSpringContextTests {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(AbstractTestNGCitrusTest.class);
    
    @Autowired
    private TestListeners testListener;
    
    @BeforeSuite(alwaysRun = true)
    public void beforeSuite(ITestContext testContext) throws Exception {
        /*
         * Fix for problem with Spring's TestNG support. 
         * In order to have access to applicationContext in BeforeSuite annotated methods.
         * Fixed with version 3.1.RC1
         */
        springTestContextPrepareTestInstance();
        
        Assert.notNull(applicationContext);
        
        TestSuite suite= getTestSuite(testContext.getSuite().getName());
        
        if(!suite.beforeSuite()) {
            org.testng.Assert.fail("Before suite failed with errors");
        }
    }
    
    @BeforeClass
    public void beforeTest(ITestContext testContext) {
        TestSuite suite= getTestSuite(testContext.getSuite().getName());
        
        suite.beforeTest();
    }
    
    protected void executeTest(ITestContext testContext) {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] {
                        this.getClass().getPackage().getName()
                                .replace('.', '/')
                                + "/"
                                + this.getClass().getSimpleName()
                                + "."
                                + FileUtils.XML_FILE_EXTENSION,
                        "com/consol/citrus/spring/internal-helper-ctx.xml" },
                true, applicationContext);
        
        TestCase testCase = null;
        try {
            testCase = (TestCase)ctx.getBean(this.getClass().getSimpleName(), TestCase.class);
        } catch (NoSuchBeanDefinitionException e) {
            org.testng.Assert.fail("Could not find test with name '" + this.getClass().getSimpleName() + "'", e);
        }
        
        if(testCase.getMetaInfo().getStatus().equals(Status.DRAFT) == false 
                && testCase.getMetaInfo().getStatus().equals(Status.DISABLED) == false ) {
            testListener.onTestStart(testCase);
            
            try {
                testCase.execute();
                testCase.finish();
                
                testListener.onTestSuccess(testCase);
            } catch (Exception e) {
                testListener.onTestFailure(testCase, e);
                
                org.testng.Assert.fail("Test failed with errors", e);
            } finally {
                testListener.onTestFinish(testCase);
            }
        } else {
            testListener.onTestSkipped(testCase);
        }
    }
    
    @AfterSuite(alwaysRun = true)
    public void afterSuite(ITestContext testContext) {
        TestSuite suite= getTestSuite(testContext.getSuite().getName());
        
        if(!suite.afterSuite()) {
            org.testng.Assert.fail("After suite failed with errors");
        }
    }
    
    private TestSuite getTestSuite(String name) {
        if(name.endsWith(" by packages")) {
            name = name.substring(0, name.length() - " by packages".length());
        }
        
        TestSuite suite = null;
        try {
            suite = (TestSuite)applicationContext.getBean(name, TestSuite.class);
        } catch (NoSuchBeanDefinitionException e) {
            log.warn("Could not find test suite with name '" + name + "'");
            
            suite = new TestSuite();
            suite.setBeanName("default-suite");
        }
        
        return suite;
    }
}
