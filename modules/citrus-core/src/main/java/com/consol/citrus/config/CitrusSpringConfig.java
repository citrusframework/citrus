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

import com.consol.citrus.context.ReferenceResolver;
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.endpoint.DefaultEndpointFactory;
import com.consol.citrus.endpoint.EndpointFactory;
import com.consol.citrus.functions.FunctionConfig;
import com.consol.citrus.report.*;
import com.consol.citrus.validation.MessageValidatorConfig;
import com.consol.citrus.validation.interceptor.GlobalMessageConstructionInterceptors;
import com.consol.citrus.validation.matcher.ValidationMatcherConfig;
import org.springframework.context.annotation.*;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
@Configuration
@Import({ FunctionConfig.class,
          ValidationMatcherConfig.class,
          MessageValidatorConfig.class,
          CitrusConfigImport.class})
@ImportResource(locations = "${systemProperties['citrus.spring.application.context']?:classpath*:citrus-context.xml}", reader = CitrusBeanDefinitionReader.class)
public class CitrusSpringConfig {

    @Bean
    public TestContextFactory testContextFactory() {
        return new TestContextFactory();
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
    public GlobalMessageConstructionInterceptors globalMessageConstructionInterceptors() {
        return new GlobalMessageConstructionInterceptors();
    }

    @Bean
    public LoggingReporter loggingReporter() {
        return new LoggingReporter();
    }

    @Bean
    public HtmlReporter htmlReporter() {
        return new HtmlReporter();
    }

    @Bean
    public JUnitReporter junitReporter() {
        return new JUnitReporter();
    }

    @Bean
    public TestListeners testListeners() {
        return new TestListeners();
    }

    @Bean
    public TestActionListeners testActionListeners() {
        return new TestActionListeners();
    }

    @Bean
    public TestSuiteListeners testSuiteListeners() {
        return new TestSuiteListeners();
    }

    @Bean
    public MessageListeners messageListeners() {
        return new MessageListeners();
    }

    @Bean
    public FailureStackTestListener failureStackTestListener() {
        return new FailureStackTestListener();
    }
}
