package com.consol.citrus.cucumber;

import com.consol.citrus.Citrus;
import com.consol.citrus.CitrusSpringContextProvider;
import com.consol.citrus.config.CitrusSpringConfig;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.context.TestContextFactoryBean;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.report.HtmlReporter;
import com.consol.citrus.report.JUnitReporter;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = CitrusSpringConfig.class)
public class UnitTestSupport extends AbstractTestNGUnitTest {

    /** Factory bean for test context */
    @Autowired
    protected TestContextFactoryBean testContextFactory;

    @Autowired
    private HtmlReporter htmlReporter;

    @Autowired
    private JUnitReporter jUnitReporter;

    /** Citrus instance */
    protected Citrus citrus;

    @BeforeClass(alwaysRun = true)
    public void beforeSuite(ITestContext testContext) throws Exception {
        citrus = Citrus.newInstance(new CitrusSpringContextProvider(applicationContext));
        citrus.beforeSuite(testContext.getSuite().getName(), testContext.getIncludedGroups());
    }

    /**
     * Runs tasks after test suite.
     * @param testContext the test context.
     */
    @AfterClass(alwaysRun = true)
    public void afterSuite(ITestContext testContext) {
        if (citrus != null) {
            citrus.afterSuite(testContext.getSuite().getName(), testContext.getIncludedGroups());
        }
    }

    @BeforeMethod
    @Override
    public void prepareTest() {
        htmlReporter.setEnabled(false);
        jUnitReporter.setEnabled(false);
        super.prepareTest();
    }

    @Override
    protected TestContext createTestContext() {
        try {
            return super.createTestContext();
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to create test context", e);
        }
    }

    @Override
    protected TestContextFactory createTestContextFactory() {
        return testContextFactory;
    }
}
