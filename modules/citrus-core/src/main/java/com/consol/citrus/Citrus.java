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
import com.consol.citrus.report.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
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

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(Citrus.class);

    /** Citrus version */
    private static String version;

    /** Strategy decides which instances are created */
    private static InstanceStrategy strategy = InstanceStrategy.NEW;

    /**
     * Citrus instance processor takes part in instance creation process.
     */
    public interface InstanceProcessor {
        void process(Citrus instance);
    }

    /**
     * Instance creation strategy.
     */
    public enum InstanceStrategy {
        NEW,
        SINGLETON
    }

    /**
     * Instance creation manager creates new Citrus instances or always a singleton based on instance creation strategy.
     */
    public static class CitrusInstanceManager {

        /** Singleton */
        private static Citrus citrus;

        /** List of instance resolvers capable of taking part in Citrus instance creation process */
        private static List<InstanceProcessor> instanceProcessors = new ArrayList<>();

        /**
         * Add instance processor.
         * @param processor
         */
        public static void addInstanceProcessor(InstanceProcessor processor) {
            instanceProcessors.add(processor);
        }

        /**
         * Initializing method loads Spring application context and reads bean definitions
         * such as test listeners and test context factory.
         * @return
         */
        public static Citrus newInstance() {
            if (strategy.equals(InstanceStrategy.NEW)) {
                Citrus instance = newInstance(new AnnotationConfigApplicationContext(CitrusSpringConfig.class));
                instanceProcessors.forEach(processor -> processor.process(instance));
                return instance;
            } else if (citrus == null) {
                citrus = newInstance(new AnnotationConfigApplicationContext(CitrusSpringConfig.class));
                instanceProcessors.forEach(processor -> processor.process(citrus));
            }

            return citrus;
        }

        /**
         * Initializing method with Spring application context Java configuration class
         * that gets loaded as application context.
         * @return
         */
        public static Citrus newInstance(Class<? extends CitrusSpringConfig> configClass) {
            if (strategy.equals(InstanceStrategy.NEW)) {
                Citrus instance = newInstance(new AnnotationConfigApplicationContext(configClass));
                instanceProcessors.forEach(processor -> processor.process(instance));
                return instance;
            } else if (citrus == null) {
                citrus = newInstance(new AnnotationConfigApplicationContext(configClass));
                instanceProcessors.forEach(processor -> processor.process(citrus));
            }

            return citrus;
        }

        /**
         * Create new Citrus instance with given Spring bean application context.
         * @param applicationContext
         * @return
         */
        public static Citrus newInstance(ApplicationContext applicationContext) {
            if (strategy.equals(InstanceStrategy.NEW)) {
                Citrus instance = new Citrus(applicationContext);
                instanceProcessors.forEach(processor -> processor.process(instance));
                return instance;
            } else if (citrus == null) {
                citrus = new Citrus(applicationContext);
                instanceProcessors.forEach(processor -> processor.process(citrus));
            }

            return citrus;
        }

        /**
         * Gets the singleton instance of Citrus.
         * @return
         */
        public static Citrus getSingleton() {
            return citrus;
        }
    }

    /* Load Citrus version */
    static {
        try (final InputStream in = new ClassPathResource("META-INF/citrus.version").getInputStream()) {
            Properties versionProperties = new Properties();
            versionProperties.load(in);
            version = versionProperties.get("citrus.version").toString();

            if (version.equals("${project.version}")) {
                log.warn("Citrus version has not been filtered with Maven project properties yet");
                version = "";
            }
        } catch (IOException e) {
            log.warn("Unable to read Citrus version information", e);
            version = "";
        }
    }

    /** Optional application property file */
    private static final String APPLICATION_PROPERTY_FILE_PROPERTY = "citrus.application.properties";
    private static final String APPLICATION_PROPERTY_FILE_ENV = "CITRUS_APPLICATION_PROPERTIES";
    private static final String APPLICATION_PROPERTY_FILE = System.getProperty(APPLICATION_PROPERTY_FILE_PROPERTY, System.getenv(APPLICATION_PROPERTY_FILE_ENV) != null ?
            System.getenv(APPLICATION_PROPERTY_FILE_ENV) : "classpath:citrus-application.properties");

    /* Load application properties */
    static {
        Resource appPropertiesResource = new PathMatchingResourcePatternResolver().getResource(APPLICATION_PROPERTY_FILE);
        if (appPropertiesResource.exists()) {
            try (final InputStream in = appPropertiesResource.getInputStream()) {
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
    }

    /** Default variable names */
    public static final String TEST_NAME_VARIABLE_PROPERTY = "citrus.test.name.variable";
    public static final String TEST_NAME_VARIABLE_ENV = "CITRUS_TEST_NAME_VARIABLE";
    public static final String TEST_NAME_VARIABLE = System.getProperty(TEST_NAME_VARIABLE_PROPERTY, System.getenv(TEST_NAME_VARIABLE_ENV) != null ?
            System.getenv(TEST_NAME_VARIABLE_ENV) : "citrus.test.name");

    public static final String TEST_PACKAGE_VARIABLE_PROPERTY = "citrus.test.package.variable";
    public static final String TEST_PACKAGE_VARIABLE_ENV = "CITRUS_TEST_PACKAGE_VARIABLE";
    public static final String TEST_PACKAGE_VARIABLE = System.getProperty(TEST_PACKAGE_VARIABLE_PROPERTY, System.getenv(TEST_PACKAGE_VARIABLE_ENV) != null ?
            System.getenv(TEST_PACKAGE_VARIABLE_ENV) : "citrus.test.package");

    /** File encoding system property */
    public static final String CITRUS_FILE_ENCODING_PROPERTY = "citrus.file.encoding";
    public static final String CITRUS_FILE_ENCODING_ENV = "CITRUS_FILE_ENCODING";
    public static final String CITRUS_FILE_ENCODING = System.getProperty(CITRUS_FILE_ENCODING_PROPERTY, System.getenv(CITRUS_FILE_ENCODING_ENV) != null ?
            System.getenv(CITRUS_FILE_ENCODING_ENV) : Charset.defaultCharset().displayName());

    /** Prefix/sufix used to identify variable expressions */
    public static final String VARIABLE_PREFIX = "${";
    public static final String VARIABLE_SUFFIX = "}";
    public static final String VARIABLE_ESCAPE = "//";

    /** Default application context name */
    public static final String DEFAULT_APPLICATION_CONTEXT_PROPERTY = "citrus.spring.application.context";
    public static final String DEFAULT_APPLICATION_CONTEXT_ENV = "CITRUS_SPRING_APPLICATION_CONTEXT";
    public static final String DEFAULT_APPLICATION_CONTEXT = System.getProperty(DEFAULT_APPLICATION_CONTEXT_PROPERTY, System.getenv(DEFAULT_APPLICATION_CONTEXT_ENV) != null ?
            System.getenv(DEFAULT_APPLICATION_CONTEXT_ENV) : "classpath*:citrus-context.xml");

    /** Default application context class */
    public static final String DEFAULT_APPLICATION_CONTEXT_CLASS_PROPERTY = "citrus.spring.java.config";
    public static final String DEFAULT_APPLICATION_CONTEXT_CLASS_ENV = "CITRUS_SPRING_JAVA_CONFIG";
    public static final String DEFAULT_APPLICATION_CONTEXT_CLASS = System.getProperty(DEFAULT_APPLICATION_CONTEXT_CLASS_PROPERTY, System.getenv(DEFAULT_APPLICATION_CONTEXT_CLASS_ENV));

    /** Default test directories */
    public static final String DEFAULT_TEST_SRC_DIRECTORY_PROPERTY = "citrus.default.src.directory";
    public static final String DEFAULT_TEST_SRC_DIRECTORY_ENV = "CITRUS_DEFAULT_SRC_DIRECTORY";
    public static final String DEFAULT_TEST_SRC_DIRECTORY = System.getProperty(DEFAULT_TEST_SRC_DIRECTORY_PROPERTY, System.getenv(DEFAULT_TEST_SRC_DIRECTORY_ENV) != null ?
            System.getenv(DEFAULT_TEST_SRC_DIRECTORY_ENV) : "src" + File.separator + "test" + File.separator);

    /** Placeholder used in messages to ignore elements */
    public static final String IGNORE_PLACEHOLDER = "@ignore@";

    /** Prefix/suffix used to identify validation matchers */
    public static final String VALIDATION_MATCHER_PREFIX = "@";
    public static final String VALIDATION_MATCHER_SUFFIX = "@";

    public static final String XML_TEST_FILE_NAME_PATTERN_PROPERTY = "citrus.xml.file.name.pattern";
    public static final String XML_TEST_FILE_NAME_PATTERN_ENV = "CITRUS_XML_FILE_NAME_PATTERN";
    public static final String XML_TEST_FILE_NAME_PATTERN = System.getProperty(XML_TEST_FILE_NAME_PATTERN_PROPERTY, System.getenv(XML_TEST_FILE_NAME_PATTERN_ENV) != null ?
            System.getenv(XML_TEST_FILE_NAME_PATTERN_ENV) : "/**/*Test.xml,/**/*IT.xml");

    public static final String JAVA_TEST_FILE_NAME_PATTERN_PROPERTY = "citrus.java.file.name.pattern";
    public static final String JAVA_TEST_FILE_NAME_PATTERN_ENV = "CITRUS_JAVA_FILE_NAME_PATTERN";
    public static final String JAVA_TEST_FILE_NAME_PATTERN = System.getProperty(JAVA_TEST_FILE_NAME_PATTERN_PROPERTY, System.getenv(JAVA_TEST_FILE_NAME_PATTERN_ENV) != null ?
            System.getenv(JAVA_TEST_FILE_NAME_PATTERN_ENV) : "/**/*Test.java,/**/*IT.java");

    /** Default message type used in message validation mechanism */
    public static final String DEFAULT_MESSAGE_TYPE_PROPERTY = "citrus.default.message.type";
    public static final String DEFAULT_MESSAGE_TYPE_ENV = "CITRUS_DEFAULT_MESSAGE_TYPE";
    public static final String DEFAULT_MESSAGE_TYPE = System.getProperty(DEFAULT_MESSAGE_TYPE_PROPERTY,  System.getenv(DEFAULT_MESSAGE_TYPE_ENV) != null ?
            System.getenv(DEFAULT_MESSAGE_TYPE_ENV) : MessageType.XML.toString());

    /** Test context factory **/
    private TestContextFactory testContextFactory;
    private TestSuiteListeners testSuiteListener;
    private TestListeners testListener;

    private Collection<SequenceBeforeSuite> beforeSuite;
    private Collection<SequenceAfterSuite> afterSuite;

    /** Basic Spring application context */
    private ApplicationContext applicationContext;

    /**
     * Private constructor with Spring bean application context that holds all basic Citrus
     * components needed to run a Citrus project.
     * @param applicationContext
     */
    private Citrus(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

        this.testListener = applicationContext.getBean(TestListeners.class);
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
        return CitrusInstanceManager.newInstance(new AnnotationConfigApplicationContext(CitrusSpringConfig.class));
    }

    /**
     * Initializing method with Spring application context Java configuration class
     * that gets loaded as application context.
     * @return
     */
    public static Citrus newInstance(Class<? extends CitrusSpringConfig> configClass) {
        return CitrusInstanceManager.newInstance(new AnnotationConfigApplicationContext(configClass));
    }

    /**
     * Create new Citrus instance with given Spring bean application context.
     * @param applicationContext
     * @return
     */
    public static Citrus newInstance(ApplicationContext applicationContext) {
        return CitrusInstanceManager.newInstance(applicationContext);
    }

    /**
     * Performs before suite test actions.
     * @param suiteName
     * @param testGroups
     */
    public void beforeSuite(String suiteName, String ... testGroups) {
        testSuiteListener.onStart();

        if (!CollectionUtils.isEmpty(beforeSuite)) {
            for (SequenceBeforeSuite sequenceBeforeSuite : beforeSuite) {
                try {
                    if (sequenceBeforeSuite.shouldExecute(suiteName, testGroups)) {
                        sequenceBeforeSuite.execute(createTestContext());
                    }
                } catch (Exception e) {
                    testSuiteListener.onStartFailure(e);
                    afterSuite(suiteName, testGroups);

                    throw new AssertionError("Before suite failed with errors", e);
                }
            }

            testSuiteListener.onStartSuccess();
        } else {
            testSuiteListener.onStartSuccess();
        }
    }

    /**
     * Performs after suite test actions.
     * @param suiteName
     * @param testGroups
     */
    public void afterSuite(String suiteName, String ... testGroups) {
        testSuiteListener.onFinish();

        if (!CollectionUtils.isEmpty(afterSuite)) {
            for (SequenceAfterSuite sequenceAfterSuite : afterSuite) {
                try {
                    if (sequenceAfterSuite.shouldExecute(suiteName, testGroups)) {
                        sequenceAfterSuite.execute(createTestContext());
                    }
                } catch (Exception e) {
                    testSuiteListener.onFinishFailure(e);
                    throw new AssertionError("After suite failed with errors", e);
                }
            }

            testSuiteListener.onFinishSuccess();
        } else {
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

    /**
     * Adds new test suite listener.
     * @param suiteListener
     */
    public void addTestSuiteListener(TestSuiteListener suiteListener) {
        this.testSuiteListener.addTestSuiteListener(suiteListener);
    }

    /**
     * Adds new test listener.
     * @param testListener
     */
    public void addTestListener(TestListener testListener) {
        this.testListener.addTestListener(testListener);
    }

    /**
     * Sets the instance creation strategy.
     * @param mode
     */
    public static void mode(InstanceStrategy mode) {
        strategy = mode;
    }

    /**
     * Closing Citrus and its application context.
     */
    public void close() {
        if (applicationContext instanceof ConfigurableApplicationContext) {
            if (((ConfigurableApplicationContext) applicationContext).isActive()) {
                ((ConfigurableApplicationContext) applicationContext).close();
            }
        }
    }
}
