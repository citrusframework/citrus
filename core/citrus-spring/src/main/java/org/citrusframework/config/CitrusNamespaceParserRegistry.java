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

package org.citrusframework.config;

import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;
import org.citrusframework.config.handler.CitrusTestCaseNamespaceHandler;
import org.citrusframework.config.xml.*;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.BeanDefinitionParser;

/**
 * Registers bean definition parser for beans in test case, handled by {@link CitrusTestCaseNamespaceHandler}
 *
 * @author Christoph Deppisch
 * @since 2007
 */
public final class CitrusNamespaceParserRegistry {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CitrusNamespaceParserRegistry.class);

    /** Resource path where to find custom parsers via lookup */
    private static final String RESOURCE_PATH = "META-INF/citrus/action/parser";

    /** Type resolver for dynamic parser lookup via resource path */
    private static final ResourcePathTypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    /** Parser registry as map */
    private static final Map<String, BeanDefinitionParser> BEAN_PARSER = new ConcurrentHashMap<>();

    static {
        registerParser("testcase", new TestCaseParser());
        registerParser("meta-info", new TestCaseMetaInfoParser());
        registerParser("template", new TemplateParser());
        registerParser("send", new SendMessageActionParser());
        registerParser("receive", new ReceiveMessageActionParser());
        registerParser("java", new JavaActionParser());
        registerParser("sleep", new SleepActionParser());
        registerParser("delay", new DelayActionParser());
        registerParser("trace-variables", new TraceVariablesActionParser());
        registerParser("create-variables", new CreateVariablesActionParser());
        registerParser("trace-time", new StopTimeActionParser());
        registerParser("echo", new EchoActionParser());
        registerParser("print", new PrintActionParser());
        registerParser("expect-timeout", new ReceiveTimeoutActionParser());
        registerParser("purge-endpoint", new PurgeEndpointActionParser());
        registerParser("action", new ActionParser());
        registerParser("template", new TemplateParser());
        registerParser("call-template", new CallTemplateParser());
        registerParser("conditional", new ConditionalParser());
        registerParser("sequential", new SequenceParser());
        registerParser("async", new AsyncParser());
        registerParser("iterate", new IterateParser());
        registerParser("repeat-until-true", new RepeatUntilTrueParser());
        registerParser("repeat-onerror-until-true", new RepeatOnErrorUntilTrueParser());
        registerParser("fail", new FailActionParser());
        registerParser("input", new InputActionParser());
        registerParser("load", new LoadPropertiesActionParser());
        registerParser("parallel", new ParallelParser());
        registerParser("catch", new CatchParser());
        registerParser("assert", new AssertParser());
        registerParser("transform", new TransformActionParser());
        registerParser("ant", new AntRunActionParser());
        registerParser("start", new StartServerActionParser());
        registerParser("stop", new StopServerActionParser());
        registerParser("wait", new WaitParser());
        registerParser("timer", new TimerParser());
        registerParser("stop-timer", new StopTimerParser());
        registerParser("stop-timer", new StopTimerParser());
    }

    /**
     * Prevent instantiation.
     */
    private CitrusNamespaceParserRegistry() {
    }

    /**
     * Register method to add new action parser.
     * @param beanName
     * @param parserObject
     */
    public static void registerParser(String beanName, BeanDefinitionParser parserObject) {
        BEAN_PARSER.put(beanName, parserObject);
    }

    /**
     * Getter for parser.
     * @return
     */
    public static Map<String, BeanDefinitionParser> getRegisteredBeanParser() {
        return BEAN_PARSER;
    }

    /**
     * Resolve test bean parser for given bean name. If not already present in the local parser cache try to locate
     * the parser through resource lookup.
     * @param name
     * @return
     */
    public static BeanDefinitionParser getBeanParser(String name) {
        if (!BEAN_PARSER.containsKey(name)) {
            try {
                BEAN_PARSER.put(name, TYPE_RESOLVER.resolve(name));
            } catch (Exception e) {
                logger.warn(String.format("Unable to locate bean parser for '%s'", name), e);
            }
        }

        return BEAN_PARSER.get(name);
    }

    /**
     * Resolves all available bean  parsers from resource path lookup. Scans classpath for meta information
     * and instantiates those components.
     * @return map of custom bean parsers
     */
    public static Map<String, BeanDefinitionParser> lookupBeanParser() {
        return TYPE_RESOLVER.resolveAll();
    }
}
