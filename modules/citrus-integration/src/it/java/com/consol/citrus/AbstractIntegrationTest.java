package com.consol.citrus;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;

import com.consol.citrus.util.FileUtils;

@ContextConfiguration(locations = {"/application-ctx.xml", "functions/citrus-function-ctx.xml"})
public abstract class AbstractIntegrationTest extends AbstractTestNGSpringContextTests {
    private static boolean afterSuite = false;
    private static boolean beforeSuite = false;
    
    @BeforeClass
    public void beforeSuite() {
        if(beforeSuite == false) {
            TestSuite testsuite = (TestSuite)applicationContext.getBean(applicationContext.getBeanNamesForType(TestSuite.class)[0]);
            testsuite.beforeSuite();
            beforeSuite = true;
        }
    }
    
    protected void executeTest() {
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
        
        TestSuite testsuite = (TestSuite)applicationContext.getBean(applicationContext.getBeanNamesForType(TestSuite.class)[0]);
        
        for (Object testCaseBean : ctx.getBeansOfType(TestCase.class).keySet()) {
            TestCase testCase = (TestCase)ctx.getBean(testCaseBean.toString());
            
            if(testCase.getMetaInfo().getStatus().equals("DRAFT") == false) {
                Assert.assertTrue(testsuite.run(testCase));
            }
        }
    }
    
    @AfterSuite
    public void afterSuite() {
        if(afterSuite == false) {
            TestSuite testsuite = (TestSuite)applicationContext.getBean(applicationContext.getBeanNamesForType(TestSuite.class)[0]);
            testsuite.afterSuite();
            afterSuite = true;
        }
    }
}
