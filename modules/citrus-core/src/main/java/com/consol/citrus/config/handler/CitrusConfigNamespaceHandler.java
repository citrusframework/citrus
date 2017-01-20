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

package com.consol.citrus.config.handler;

import com.consol.citrus.config.xml.*;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Namespace handler for components in Citrus configuration.
 * 
 * @author Christoph Deppisch
 */
public class CitrusConfigNamespaceHandler extends NamespaceHandlerSupport {

    /**
     * @see org.springframework.beans.factory.xml.NamespaceHandler#init()
     */
    public void init() {
        registerBeanDefinitionParser("schema-repository", new SchemaRepositoryParser());
        registerBeanDefinitionParser("schema", new SchemaParser());
        registerBeanDefinitionParser("schema-collection", new SchemaCollectionParser());
        registerBeanDefinitionParser("actor", new TestActorParser());
        registerBeanDefinitionParser("global-variables", new GlobalVariablesParser());
        registerBeanDefinitionParser("xml-data-dictionary", new XmlDataDictionaryParser());
        registerBeanDefinitionParser("xpath-data-dictionary", new XpathDataDictionaryParser());
        registerBeanDefinitionParser("json-data-dictionary", new JsonDataDictionaryParser());
        registerBeanDefinitionParser("json-path-data-dictionary", new JsonPathDataDictionaryParser());
        registerBeanDefinitionParser("message-validators", new MessageValidatorRegistryParser());
        registerBeanDefinitionParser("namespace-context", new NamespaceContextParser());
        registerBeanDefinitionParser("function-library", new FunctionLibraryParser());
        registerBeanDefinitionParser("validation-matcher-library", new ValidationMatcherLibraryParser());
        registerBeanDefinitionParser("before-suite", new SequenceBeforeSuiteParser());
        registerBeanDefinitionParser("before-test", new SequenceBeforeTestParser());
        registerBeanDefinitionParser("after-suite", new SequenceAfterSuiteParser());
        registerBeanDefinitionParser("after-test", new SequenceAfterTestParser());
        registerBeanDefinitionParser("channel-endpoint", new ChannelEndpointParser());
        registerBeanDefinitionParser("channel-sync-endpoint", new ChannelSyncEndpointParser());
        registerBeanDefinitionParser("channel", new MessageSelectingQueueChannelParser());
        registerBeanDefinitionParser("message-channel", new MessageSelectingQueueChannelParser());
        registerBeanDefinitionParser("channel-endpoint-adapter", new ChannelEndpointAdapterParser());
        registerBeanDefinitionParser("dispatching-endpoint-adapter", new RequestDispatchingEndpointAdapterParser());
        registerBeanDefinitionParser("static-response-adapter", new StaticResponseEndpointAdapterParser());
        registerBeanDefinitionParser("empty-response-adapter", new EmptyResponseEndpointAdapterParser());
        registerBeanDefinitionParser("timeout-producing-adapter", new TimeoutProducingEndpointAdapterParser());
    }

}
