package org.citrusframework.citrus;

import org.citrusframework.citrus.config.CitrusSpringConfig;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.context.TestContextFactory;
import org.citrusframework.citrus.context.TestContextFactoryBean;
import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.citrusframework.citrus.report.HtmlReporter;
import org.citrusframework.citrus.report.JUnitReporter;
import org.citrusframework.citrus.testng.AbstractBeanDefinitionParserTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.ITestContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = CitrusSpringConfig.class)
public class BeanDefinitionParserTestSupport extends AbstractBeanDefinitionParserTest {

    /** Factory bean for test context */
    @Autowired
    protected TestContextFactoryBean testContextFactory;

    @Autowired
    private HtmlReporter htmlReporter;

    @Autowired
    private JUnitReporter jUnitReporter;

    /** Citrus instance */
    protected Citrus citrus;

    @BeforeSuite(alwaysRun = true)
    @Override
    public void beforeSuite(ITestContext testContext) throws Exception {
        super.beforeSuite(testContext);

        citrus = Citrus.newInstance(new CitrusSpringContextProvider(applicationContext));
        citrus.beforeSuite(testContext.getSuite().getName(), testContext.getIncludedGroups());
    }

    /**
     * Runs tasks after test suite.
     * @param testContext the test context.
     */
    @AfterSuite(alwaysRun = true)
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
