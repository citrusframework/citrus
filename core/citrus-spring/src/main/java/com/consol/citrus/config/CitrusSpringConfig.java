/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.config;

import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.context.TestContextFactoryBean;
import com.consol.citrus.endpoint.DefaultEndpointFactory;
import com.consol.citrus.endpoint.EndpointFactory;
import com.consol.citrus.functions.FunctionConfig;
import com.consol.citrus.log.DefaultLogModifier;
import com.consol.citrus.log.LogModifier;
import com.consol.citrus.report.FailureStackTestListener;
import com.consol.citrus.report.MessageListenersFactory;
import com.consol.citrus.report.TestActionListenersFactory;
import com.consol.citrus.report.TestListenersFactory;
import com.consol.citrus.report.TestSuiteListenersFactory;
import com.consol.citrus.reporter.ReporterConfig;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.util.SpringBeanTypeConverter;
import com.consol.citrus.util.TypeConverter;
import com.consol.citrus.validation.MessageValidatorConfig;
import com.consol.citrus.validation.interceptor.MessageProcessorsFactory;
import com.consol.citrus.validation.matcher.ValidationMatcherConfig;
import com.consol.citrus.variable.SegmentVariableExtractorRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
@Configuration
@Import({ FunctionConfig.class,
          ValidationMatcherConfig.class,
          MessageValidatorConfig.class,
          ReporterConfig.class,
          CitrusConfigImport.class})
@ImportResource(locations = "${systemProperties['citrus.spring.application.context']?:classpath*:citrus-context.xml}", reader = CitrusBeanDefinitionReader.class)
public class CitrusSpringConfig {

    @Bean
    public TestContextFactoryBean testContextFactory() {
        return new TestContextFactoryBean();
    }

    @Bean
    public EndpointFactory endpointFactory() {
        return new DefaultEndpointFactory();
    }

    @Bean
    public ReferenceResolver referenceResolver() {
        return new SpringBeanReferenceResolver();
    }

    @Bean
    public TypeConverter typeConverter() {
        return SpringBeanTypeConverter.INSTANCE;
    }

    @Bean
    public LogModifier logModifier() {
        return new DefaultLogModifier();
    }

    @Bean
    public MessageProcessorsFactory messageProcessors() {
        return new MessageProcessorsFactory();
    }

    @Bean
    public TestListenersFactory testListeners() {
        return new TestListenersFactory();
    }

    @Bean
    public TestActionListenersFactory testActionListeners() {
        return new TestActionListenersFactory();
    }

    @Bean
    public TestSuiteListenersFactory testSuiteListeners() {
        return new TestSuiteListenersFactory();
    }

    @Bean
    public MessageListenersFactory messageListeners() {
        return new MessageListenersFactory();
    }

    @Bean
    public FailureStackTestListener failureStackTestListener() {
        return new FailureStackTestListener();
    }

    @Bean
    public ComponentLifecycleProcessor componentInitializer() {
        return new ComponentLifecycleProcessor();
    }

    @Bean
    public SegmentVariableExtractorRegistry segmentVariableExtractorRegistry() {
        return new SegmentVariableExtractorRegistry();
    }
}
