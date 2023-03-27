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

package org.citrusframework.citrus.config.handler;

import java.util.Map;

import org.citrusframework.citrus.config.xml.DefaultMessageQueueParser;
import org.citrusframework.citrus.config.xml.DirectEndpointAdapterParser;
import org.citrusframework.citrus.config.xml.DirectEndpointParser;
import org.citrusframework.citrus.config.xml.DirectSyncEndpointParser;
import org.citrusframework.citrus.config.xml.EmptyResponseEndpointAdapterParser;
import org.citrusframework.citrus.config.xml.FunctionLibraryParser;
import org.citrusframework.citrus.config.xml.GlobalVariablesParser;
import org.citrusframework.citrus.config.xml.MessageValidatorRegistryParser;
import org.citrusframework.citrus.config.xml.NamespaceContextParser;
import org.citrusframework.citrus.config.xml.RequestDispatchingEndpointAdapterParser;
import org.citrusframework.citrus.config.xml.SchemaParser;
import org.citrusframework.citrus.config.xml.SchemaRepositoryParser;
import org.citrusframework.citrus.config.xml.SequenceAfterSuiteParser;
import org.citrusframework.citrus.config.xml.SequenceAfterTestParser;
import org.citrusframework.citrus.config.xml.SequenceBeforeSuiteParser;
import org.citrusframework.citrus.config.xml.SequenceBeforeTestParser;
import org.citrusframework.citrus.config.xml.StaticResponseEndpointAdapterParser;
import org.citrusframework.citrus.config.xml.TestActorParser;
import org.citrusframework.citrus.config.xml.TimeoutProducingEndpointAdapterParser;
import org.citrusframework.citrus.config.xml.ValidationMatcherLibraryParser;
import org.citrusframework.citrus.config.xml.parser.CitrusXmlConfigParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Namespace handler for components in Citrus configuration.
 *
 * @author Christoph Deppisch
 */
public class CitrusConfigNamespaceHandler extends NamespaceHandlerSupport {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(CitrusConfigNamespaceHandler.class);

    @Override
    public void init() {
        registerBeanDefinitionParser("schema-repository", new SchemaRepositoryParser());
        registerBeanDefinitionParser("schema", new SchemaParser());
        registerBeanDefinitionParser("actor", new TestActorParser());
        registerBeanDefinitionParser("global-variables", new GlobalVariablesParser());
        registerBeanDefinitionParser("message-validators", new MessageValidatorRegistryParser());
        registerBeanDefinitionParser("namespace-context", new NamespaceContextParser());
        registerBeanDefinitionParser("function-library", new FunctionLibraryParser());
        registerBeanDefinitionParser("validation-matcher-library", new ValidationMatcherLibraryParser());
        registerBeanDefinitionParser("before-suite", new SequenceBeforeSuiteParser());
        registerBeanDefinitionParser("before-test", new SequenceBeforeTestParser());
        registerBeanDefinitionParser("after-suite", new SequenceAfterSuiteParser());
        registerBeanDefinitionParser("after-test", new SequenceAfterTestParser());
        registerBeanDefinitionParser("direct-endpoint", new DirectEndpointParser());
        registerBeanDefinitionParser("direct-sync-endpoint", new DirectSyncEndpointParser());
        registerBeanDefinitionParser("queue", new DefaultMessageQueueParser());
        registerBeanDefinitionParser("message-queue", new DefaultMessageQueueParser());
        registerBeanDefinitionParser("direct-endpoint-adapter", new DirectEndpointAdapterParser());
        registerBeanDefinitionParser("dispatching-endpoint-adapter", new RequestDispatchingEndpointAdapterParser());
        registerBeanDefinitionParser("static-response-adapter", new StaticResponseEndpointAdapterParser());
        registerBeanDefinitionParser("empty-response-adapter", new EmptyResponseEndpointAdapterParser());
        registerBeanDefinitionParser("timeout-producing-adapter", new TimeoutProducingEndpointAdapterParser());

        lookupBeanDefinitionParser();
    }

    /**
     * Lookup custom bean definition parser from resource path.
     */
    private void lookupBeanDefinitionParser() {
        Map<String, BeanDefinitionParser> actionParserMap = CitrusXmlConfigParser.lookup("core");

        actionParserMap.forEach((k, p) -> {
            registerBeanDefinitionParser(k, p);
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Register bean definition parser %s from resource %s", p.getClass(), k));
            }
        });
    }

}
