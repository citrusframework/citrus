/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus;

import com.consol.citrus.config.CitrusSpringConfig;
import com.consol.citrus.container.SequenceAfterSuite;
import com.consol.citrus.container.SequenceBeforeSuite;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.report.TestSuiteListeners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Citrus main class initializes a new Citrus runtime environment with a Spring application context. Provides before/after suite action execution
 * and test execution methods.
 *
 * @author Christoph Deppisch
 * @since 2.1
 */
public final class Citrus {

    /** Citrus version */
    private static String version;

    /** Test context factory **/
    private TestContextFactory testContextFactory;
    private TestSuiteListeners testSuiteListener;

    private Collection<SequenceBeforeSuite> beforeSuite;
    private Collection<SequenceAfterSuite> afterSuite;

    /** Basic Spring application context */
    private ApplicationContext applicationContext;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(Citrus.class);

    /** Load Citrus version */
    static {
        try (final InputStream in = new ClassPathResource("META-INF/citrus.version").getInputStream()) {
            Properties versionProperties = new Properties();
            versionProperties.load(in);
            version = versionProperties.get("citrus.version").toString();
        } catch (IOException e) {
            log.warn("Unable to read Citrus version information", e);
            version = "";
        }
    }

    /** Optional application property file */
    private static final String APPLICATION_PROPERTY_FILE = System.getProperty("citrus.application.config", "citrus-application.properties");

    /** Load application properties */
    static {
        try (final InputStream in = new ClassPathResource(APPLICATION_PROPERTY_FILE).getInputStream()) {
            Properties applicationProperties = new Properties();
            applicationProperties.load(in);

            log.debug("Loading Citrus application properties");

            for (Map.Entry<Object, Object> property : applicationProperties.entrySet()) {
                if (StringUtils.isEmpty(System.getProperty(property.getKey().toString()))) {
                    log.debug(String.format("Setting application property %s=%s", property.getKey(), property.getValue()));
                    System.setProperty(property.getKey().toString(), property.getValue().toString());
                }
            }
        } catch (Exception e) {
            if (log.isTraceEnabled()) {
                log.trace("Unable to locate Citrus application properties", e);
            } else {
                log.info("Unable to locate Citrus application properties");
            }
        }
    }

    /** Default variable names */
    public static final String TEST_NAME_VARIABLE = "citrus.test.name";
    public static final String TEST_PACKAGE_VARIABLE = "citrus.test.package";

    /** File encoding system property */
    public static final String CITRUS_FILE_ENCODING_PROPERTY = "citrus.file.encoding";
    public static final String CITRUS_FILE_ENCODING = System.getProperty(CITRUS_FILE_ENCODING_PROPERTY, Charset.defaultCharset().displayName());

    /** Prefix/sufix used to identify variable expressions */
    public static final String VARIABLE_PREFIX = "${";
    public static final String VARIABLE_SUFFIX = "}";

    /** Default application context name */
    public static final String DEFAULT_APPLICATION_CONTEXT_PROPERTY = "citrus.spring.application.context";
    public static final String DEFAULT_APPLICATION_CONTEXT = System.getProperty(DEFAULT_APPLICATION_CONTEXT_PROPERTY, "classpath*:citrus-context.xml");

    /** Default application context class */
    public static final String DEFAULT_APPLICATION_CONTEXT_CLASS_PROPERTY = "citrus.spring.java.config";
    public static final String DEFAULT_APPLICATION_CONTEXT_CLASS = System.getProperty(DEFAULT_APPLICATION_CONTEXT_CLASS_PROPERTY);

    /** Default test directories */
    public static final String DEFAULT_TEST_SRC_DIRECTORY = "src" + File.separator + "test" + File.separator;

    /** Placeholder used in messages to ignore elements */
    public static final String IGNORE_PLACEHOLDER = "@ignore@";

    /** Prefix/suffix used to identify validation matchers */
    public static final String VALIDATION_MATCHER_PREFIX = "@";
    public static final String VALIDATION_MATCHER_SUFFIX = "@";

    public static final String XML_TEST_FILE_NAME_PATTERN_PROPERTY = "citrus.xml.file.name.pattern";
    public static final String XML_TEST_FILE_NAME_PATTERN = System.getProperty(XML_TEST_FILE_NAME_PATTERN_PROPERTY, "/**/*Test.xml,/**/*IT.xml");

    public static final String JAVA_TEST_FILE_NAME_PATTERN_PROPERTY = "citrus.java.file.name.pattern";
    public static final String JAVA_TEST_FILE_NAME_PATTERN = System.getProperty(JAVA_TEST_FILE_NAME_PATTERN_PROPERTY, "/**/*Test.java,/**/*IT.java");

    /** Default message type used in message validation mechanism */
    public static final String DEFAULT_MESSAGE_TYPE = MessageType.XML.toString();

    /**
     * Private constructor with Spring bean application context that holds all basic Citrus
     * components needed to run a Citrus project.
     * @param applicationContext
     */
    private Citrus(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

        this.testSuiteListener = applicationContext.getBean(TestSuiteListeners.class);
        this.testContextFactory = applicationContext.getBean(TestContextFactory.class);
        this.beforeSuite = applicationContext.getBeansOfType(SequenceBeforeSuite.class).values();
        this.afterSuite = applicationContext.getBeansOfType(SequenceAfterSuite.class).values();
    }

    /**
     * Initializing method loads Spring application context and reads bean definitions
     * such as test listeners and test context factory.
     * @return
     */
    public static Citrus newInstance() {
        return newInstance(new AnnotationConfigApplicationContext(CitrusSpringConfig.class));
    }

    /**
     * Initializing method with Spring application context Java configuration class
     * that gets loaded as application context.
     * @return
     */
    public static Citrus newInstance(Class<? extends CitrusSpringConfig> configClass) {
        return newInstance(new AnnotationConfigApplicationContext(configClass));
    }

    /**
     * Create new Citrus instance with given Spring bean application context.
     * @param applicationContext
     * @return
     */
    public static Citrus newInstance(ApplicationContext applicationContext) {
        return new Citrus(applicationContext);
    }

    /**
     * Performs before suite test actions.
     * @param suiteName
     * @param testGroups
     */
    public void beforeSuite(String suiteName, String ... testGroups) {
        if (!CollectionUtils.isEmpty(beforeSuite)) {
            for (SequenceBeforeSuite sequenceBeforeSuite : beforeSuite) {
                try {
                    if (sequenceBeforeSuite.shouldExecute(suiteName, testGroups)) {
                        sequenceBeforeSuite.execute(createTestContext());
                    }
                } catch (Exception e) {
                    org.testng.Assert.fail("Before suite failed with errors", e);
                }
            }
        } else {
            testSuiteListener.onStart();
            testSuiteListener.onStartSuccess();
        }
    }

    /**
     * Performs after suite test actions.
     * @param suiteName
     * @param testGroups
     */
    public void afterSuite(String suiteName, String ... testGroups) {
        if (!CollectionUtils.isEmpty(afterSuite)) {
            for (SequenceAfterSuite sequenceAfterSuite : afterSuite) {
                try {
                    if (sequenceAfterSuite.shouldExecute(suiteName, testGroups)) {
                        sequenceAfterSuite.execute(createTestContext());
                    }
                } catch (Exception e) {
                    org.testng.Assert.fail("After suite failed with errors", e);
                }
            }
        } else {
            testSuiteListener.onFinish();
            testSuiteListener.onFinishSuccess();
        }
    }

    /**
     * Gets set of file name patterns for XML test files.
     * @return
     */
    public static Set<String> getXmlTestFileNamePattern() {
        return StringUtils.commaDelimitedListToSet(XML_TEST_FILE_NAME_PATTERN);
    }

    /**
     * Gets set of file name patterns for Java test files.
     * @return
     */
    public static Set<String> getJavaTestFileNamePattern() {
        return StringUtils.commaDelimitedListToSet(JAVA_TEST_FILE_NAME_PATTERN);
    }

    /**
     * Runs a test action which can also be a whole test case.
     */
    public void run(TestAction action) {
        run(action, createTestContext());
    }

    /**
     * Runs test action with given test context. Test action can also be a whole test case.
     * @param action
     * @param testContext
     */
    public void run(TestAction action, TestContext testContext) {
        action.execute(testContext);
    }

    /**
     * Creates a new test context.
     * @return the new citrus test context.
     */
    public TestContext createTestContext() {
        return testContextFactory.getObject();
    }

    /**
     * Gets the basic Citrus Spring bean application context.
     * @return
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Gets the Citrus version from classpath resource properties.
     * @return
     */
    public static String getVersion() {
        return version;
    }
}
