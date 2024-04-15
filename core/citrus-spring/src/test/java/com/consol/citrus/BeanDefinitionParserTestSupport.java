package com.consol.citrus;

import com.consol.citrus.config.CitrusSpringConfig;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.context.TestContextFactoryBean;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.report.HtmlReporter;
import com.consol.citrus.report.JUnitReporter;
import com.consol.citrus.testng.AbstractBeanDefinitionParserTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Reporter;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import static java.util.Objects.nonNull;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = CitrusSpringConfig.class)
public class BeanDefinitionParserTestSupport extends AbstractBeanDefinitionParserTest {

    /**
     * Factory bean for test context
     */
    @Autowired
    protected TestContextFactoryBean testContextFactory;

    @Autowired
    private HtmlReporter htmlReporter;

    @Autowired
    private JUnitReporter jUnitReporter;

    /**
     * Citrus instance
     */
    protected Citrus citrus;

    @Override
    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() throws Exception {
        super.beforeSuite();

        citrus = Citrus.newInstance(new CitrusSpringContextProvider(applicationContext));
        citrus.beforeSuite(
                Reporter.getCurrentTestResult().getTestContext().getSuite().getName(),
                Reporter.getCurrentTestResult().getTestContext().getIncludedGroups());
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        if (nonNull(citrus)) {
            citrus.afterSuite(
                    Reporter.getCurrentTestResult().getTestContext().getSuite().getName(),
                    Reporter.getCurrentTestResult().getTestContext().getIncludedGroups());
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
