package org.citrusframework;

import org.citrusframework.config.CitrusSpringConfig;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.context.TestContextFactoryBean;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.report.HtmlReporter;
import org.citrusframework.report.JUnitReporter;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Reporter;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = CitrusSpringConfig.class)
public class UnitTestSupport extends AbstractTestNGUnitTest {

    /** Factory bean for test context */
    @Autowired
    protected TestContextFactoryBean testContextFactory;

    @Autowired(required = false)
    private HtmlReporter htmlReporter;

    @Autowired(required = false)
    private JUnitReporter jUnitReporter;

    /** Citrus instance */
    protected Citrus citrus;

    @BeforeSuite(alwaysRun = true)
    @Override
    public void beforeSuite() throws Exception {
        super.beforeSuite();

        citrus = Citrus.newInstance(new CitrusSpringContextProvider(applicationContext));
        citrus.beforeSuite(Reporter.getCurrentTestResult().getTestContext().getSuite().getName(),
                Reporter.getCurrentTestResult().getTestContext().getIncludedGroups());
    }

    /**
     * Runs tasks after test suite.
     */
    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        if (citrus != null) {
            citrus.afterSuite(Reporter.getCurrentTestResult().getTestContext().getSuite().getName(),
                    Reporter.getCurrentTestResult().getTestContext().getIncludedGroups());
        }
    }

    @BeforeMethod
    @Override
    public void prepareTest() {
        if (htmlReporter != null) {
            htmlReporter.setEnabled(false);
        }

        if (jUnitReporter != null) {
            jUnitReporter.setEnabled(false);
        }
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
