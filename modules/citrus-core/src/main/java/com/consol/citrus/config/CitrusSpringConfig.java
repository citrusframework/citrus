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
import com.consol.citrus.validation.interceptor.MessageConstructionInterceptors;
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

    @Bean(name = "testContextFactory")
    public TestContextFactory getTestContextFactory() {
        return new TestContextFactory();
    }

    @Bean(name = "endpointFactory")
    public EndpointFactory getEndpointFactory() {
        return new DefaultEndpointFactory();
    }

    @Bean(name = "referenceResolver")
    public ReferenceResolver getReferenceResolver() {
        return new SpringBeanReferenceResolver();
    }

    @Bean(name = "messageConstructionInterceptors")
    public MessageConstructionInterceptors getMessageConstructionInterceptors() {
        return new MessageConstructionInterceptors();
    }

    @Bean(name = "loggingReporter")
    public LoggingReporter getLoggingReporter() {
        return new LoggingReporter();
    }

    @Bean(name = "htmlReporter")
    public HtmlReporter getHtmlReporter() {
        return new HtmlReporter();
    }

    @Bean(name = "testListeners")
    public TestListeners getTestListeners() {
        return new TestListeners();
    }

    @Bean(name = "testActionListeners")
    public TestActionListeners getTestActionListeners() {
        return new TestActionListeners();
    }

    @Bean(name = "testSuiteListeners")
    public TestSuiteListeners getTestSuiteListeners() {
        return new TestSuiteListeners();
    }

    @Bean(name = "messageListeners")
    public MessageListeners getMessageListeners() {
        return new MessageListeners();
    }

    @Bean(name = "failureStackTestListener")
    public FailureStackTestListener getFailureStackTestListener() {
        return new FailureStackTestListener();
    }
}
