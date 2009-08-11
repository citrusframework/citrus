package com.consol.citrus.testng;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;

import com.consol.citrus.TestCase;
import com.consol.citrus.TestSuite;
import com.consol.citrus.TestCaseMetaInfo.Status;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;

@ContextConfiguration(locations = {"/application-ctx.xml", "/com/consol/citrus/functions/citrus-function-ctx.xml"})
public abstract class AbstractIntegrationTest extends AbstractTestNGSpringContextTests {
    private static boolean afterTests = false;
    private static boolean beforeTests = false;
    
    private static final Object before = new Object();
    private static final Object after = new Object();
    
    @BeforeClass
    public void beforeTests(ITestContext testContext) {
        synchronized (before) {
            if(beforeTests == false) {
                String suiteName = testContext.getSuite().getName();
                
                TestSuite suite;
                try {
                    suite = (TestSuite)applicationContext.getBean(suiteName, TestSuite.class);
                } catch (NoSuchBeanDefinitionException e) {
                    throw new CitrusRuntimeException("Could not find test suite with name '" + suiteName + "'", e);
                }
                
                suite.beforeSuite();
                beforeTests = true;
            }
        }
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
        
        String suiteName = testContext.getSuite().getName();
        
        TestSuite suite;
        try {
            suite = (TestSuite)applicationContext.getBean(suiteName, TestSuite.class);
        } catch (NoSuchBeanDefinitionException e) {
            throw new CitrusRuntimeException("Could not find test suite with name '" + suiteName + "'", e);
        }
        
        for (Object testCaseBean : ctx.getBeansOfType(TestCase.class).keySet()) {
            TestCase testCase = (TestCase)ctx.getBean(testCaseBean.toString());
            
            if(testCase.getMetaInfo().getStatus().equals(Status.DRAFT) == false 
                    && testCase.getMetaInfo().getStatus().equals(Status.DISABLED) == false ) {
                Assert.assertTrue(suite.run(testCase));
            }
        }
    }
    
    @AfterSuite
    public void afterTests(ITestContext testContext) {
        synchronized (after) {
            if(afterTests == false) {
                String suiteName = testContext.getSuite().getName();
                
                TestSuite suite;
                try {
                    suite = (TestSuite)applicationContext.getBean(suiteName, TestSuite.class);
                } catch (NoSuchBeanDefinitionException e) {
                    throw new CitrusRuntimeException("Could not find test suite with name '" + suiteName + "'", e);
                }
                
                suite.afterSuite();
                afterTests = true;
            }
        }
    }
}
