/*
 * Copyright 2006-2023 the original author or authors.
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

package org.citrusframework.config;

import java.util.Map;
import org.citrusframework.context.SpringBeanReferenceResolver;
import org.citrusframework.context.TestContextFactoryBean;
import org.citrusframework.endpoint.DefaultEndpointFactory;
import org.citrusframework.endpoint.EndpointFactory;
import org.citrusframework.functions.FunctionConfig;
import org.citrusframework.io.CitrusResourceEditor;
import org.citrusframework.log.DefaultLogModifier;
import org.citrusframework.log.LogModifier;
import org.citrusframework.report.FailureStackTestListener;
import org.citrusframework.report.MessageListenersFactory;
import org.citrusframework.report.TestActionListenersFactory;
import org.citrusframework.report.TestListenersFactory;
import org.citrusframework.report.TestSuiteListenersFactory;
import org.citrusframework.reporter.ReporterConfig;
import org.citrusframework.spi.CitrusResourceWrapper;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.SpringResourceWrapper;
import org.citrusframework.spi.StringToResourceConverter;
import org.citrusframework.util.SpringBeanTypeConverter;
import org.citrusframework.util.TypeConverter;
import org.citrusframework.validation.MessageValidatorConfig;
import org.citrusframework.validation.interceptor.MessageProcessorsFactory;
import org.citrusframework.validation.matcher.ValidationMatcherConfig;
import org.citrusframework.variable.SegmentVariableExtractorRegistry;
import org.springframework.beans.factory.config.CustomEditorConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
@Configuration
@Import({
        FunctionConfig.class,
        ValidationMatcherConfig.class,
        MessageValidatorConfig.class,
        ReporterConfig.class,
        CitrusConfigImport.class
})
@ImportResource(locations = "${systemProperties['citrus.spring.application.context']?:classpath*:citrus-context.xml}", reader = CitrusBeanDefinitionReader.class)
public class CitrusSpringConfig {

    @Bean(name = "citrusTestContextFactory")
    public TestContextFactoryBean testContextFactory() {
        return new TestContextFactoryBean();
    }

    @Bean(name = "citrusEndpointFactory")
    public EndpointFactory endpointFactory() {
        return new DefaultEndpointFactory();
    }

    @Bean(name = "citrusReferenceResolver")
    public ReferenceResolver referenceResolver() {
        return new SpringBeanReferenceResolver();
    }

    @Bean(name = "citrusTypeConverter")
    public TypeConverter typeConverter() {
        return TypeConverter.lookupDefault(SpringBeanTypeConverter.INSTANCE);
    }

    @Bean(name = "citrusLogModifier")
    public LogModifier logModifier() {
        return new DefaultLogModifier();
    }

    @Bean(name = "citrusMessageProcessors")
    public MessageProcessorsFactory messageProcessors() {
        return new MessageProcessorsFactory();
    }

    @Bean(name = "citrusTestListeners")
    public TestListenersFactory testListeners() {
        return new TestListenersFactory();
    }

    @Bean(name = "citrusTestActionListeners")
    public TestActionListenersFactory testActionListeners() {
        return new TestActionListenersFactory();
    }

    @Bean(name = "citrusTestSuiteListeners")
    public TestSuiteListenersFactory testSuiteListeners() {
        return new TestSuiteListenersFactory();
    }

    @Bean(name = "citrusMessageListeners")
    public MessageListenersFactory messageListeners() {
        return new MessageListenersFactory();
    }

    @Bean(name = "citrusFailureStackTestListener")
    public FailureStackTestListener failureStackTestListener() {
        return new FailureStackTestListener();
    }

    @Bean(name = "citrusComponentInitializer")
    public ComponentLifecycleProcessor componentInitializer() {
        return new ComponentLifecycleProcessor();
    }

    @Bean(name = "citrusVariableExtractorRegistry")
    public SegmentVariableExtractorRegistry variableExtractorRegistry() {
        return new SegmentVariableExtractorRegistry();
    }

    @Bean
    public StringToResourceConverter stringToResourceConverter() {
        return new StringToResourceConverter();
    }

    @Bean
    public CitrusResourceWrapper.ResourceConverter citrusResourceConverter() {
        return new CitrusResourceWrapper.ResourceConverter();
    }

    @Bean
    public SpringResourceWrapper.ResourceConverter springResourceConverter() {
        return new SpringResourceWrapper.ResourceConverter();
    }

    @Bean
    public CustomEditorConfigurer citrusCustomEditorRegistrar() {
        CustomEditorConfigurer configurer = new CustomEditorConfigurer();
        configurer.setCustomEditors(Map.of(Resource.class, CitrusResourceEditor.class));
        return configurer;
    }
}
