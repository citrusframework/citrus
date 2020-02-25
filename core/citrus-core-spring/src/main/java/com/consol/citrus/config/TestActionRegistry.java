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

package com.consol.citrus.config;

import java.util.HashMap;
import java.util.Map;

import com.consol.citrus.config.xml.ActionParser;
import com.consol.citrus.config.xml.AntRunActionParser;
import com.consol.citrus.config.xml.AssertParser;
import com.consol.citrus.config.xml.AsyncParser;
import com.consol.citrus.config.xml.CallTemplateParser;
import com.consol.citrus.config.xml.CatchParser;
import com.consol.citrus.config.xml.ConditionalParser;
import com.consol.citrus.config.xml.CreateVariablesActionParser;
import com.consol.citrus.config.xml.EchoActionParser;
import com.consol.citrus.config.xml.ExecutePLSQLActionParser;
import com.consol.citrus.config.xml.FailActionParser;
import com.consol.citrus.config.xml.GroovyActionParser;
import com.consol.citrus.config.xml.InputActionParser;
import com.consol.citrus.config.xml.IterateParser;
import com.consol.citrus.config.xml.JavaActionParser;
import com.consol.citrus.config.xml.LoadPropertiesActionParser;
import com.consol.citrus.config.xml.ParallelParser;
import com.consol.citrus.config.xml.PurgeEndpointActionParser;
import com.consol.citrus.config.xml.ReceiveMessageActionParser;
import com.consol.citrus.config.xml.ReceiveTimeoutActionParser;
import com.consol.citrus.config.xml.RepeatOnErrorUntilTrueParser;
import com.consol.citrus.config.xml.RepeatUntilTrueParser;
import com.consol.citrus.config.xml.SQLActionParser;
import com.consol.citrus.config.xml.SendMessageActionParser;
import com.consol.citrus.config.xml.SequenceParser;
import com.consol.citrus.config.xml.SleepActionParser;
import com.consol.citrus.config.xml.StartServerActionParser;
import com.consol.citrus.config.xml.StopServerActionParser;
import com.consol.citrus.config.xml.StopTimeActionParser;
import com.consol.citrus.config.xml.StopTimerParser;
import com.consol.citrus.config.xml.TemplateParser;
import com.consol.citrus.config.xml.TimerParser;
import com.consol.citrus.config.xml.TraceVariablesActionParser;
import com.consol.citrus.config.xml.TransformActionParser;
import com.consol.citrus.config.xml.WaitParser;
import com.consol.citrus.spi.ResourcePathTypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.BeanDefinitionParser;

/**
 * Registers bean definition parser for actions in test case.
 *
 * @author Christoph Deppisch
 * @since 2007
 */
public final class TestActionRegistry {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(TestActionRegistry.class);

    /** Resource path where to find custom action parsers via lookup */
    private static final String RESOURCE_PATH = "META-INF/citrus/action/parser";

    /** Type resolver for dynamic action parser lookup via resource path */
    private static final ResourcePathTypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    /** Parser registry as map */
    private static Map<String, BeanDefinitionParser> parserCache = new HashMap<>();

    static {
        registerActionParser("send", new SendMessageActionParser());
        registerActionParser("receive", new ReceiveMessageActionParser());
        registerActionParser("sql", new SQLActionParser());
        registerActionParser("java", new JavaActionParser());
        registerActionParser("sleep", new SleepActionParser());
        registerActionParser("trace-variables", new TraceVariablesActionParser());
        registerActionParser("create-variables", new CreateVariablesActionParser());
        registerActionParser("trace-time", new StopTimeActionParser());
        registerActionParser("echo", new EchoActionParser());
        registerActionParser("expect-timeout", new ReceiveTimeoutActionParser());
        registerActionParser("purge-endpoint", new PurgeEndpointActionParser());
        registerActionParser("action", new ActionParser());
        registerActionParser("template", new TemplateParser());
        registerActionParser("call-template", new CallTemplateParser());
        registerActionParser("conditional", new ConditionalParser());
        registerActionParser("sequential", new SequenceParser());
        registerActionParser("async", new AsyncParser());
        registerActionParser("iterate", new IterateParser());
        registerActionParser("repeat-until-true", new RepeatUntilTrueParser());
        registerActionParser("repeat-onerror-until-true", new RepeatOnErrorUntilTrueParser());
        registerActionParser("fail", new FailActionParser());
        registerActionParser("input", new InputActionParser());
        registerActionParser("load", new LoadPropertiesActionParser());
        registerActionParser("parallel", new ParallelParser());
        registerActionParser("catch", new CatchParser());
        registerActionParser("assert", new AssertParser());
        registerActionParser("plsql", new ExecutePLSQLActionParser());
        registerActionParser("groovy", new GroovyActionParser());
        registerActionParser("transform", new TransformActionParser());
        registerActionParser("ant", new AntRunActionParser());
        registerActionParser("start", new StartServerActionParser());
        registerActionParser("stop", new StopServerActionParser());
        registerActionParser("wait", new WaitParser());
        registerActionParser("timer", new TimerParser());
        registerActionParser("stop-timer", new StopTimerParser());
    }

    /**
     * Prevent instantiation.
     */
    private TestActionRegistry() {
    }

    /**
     * Register method to add new action parser.
     * @param actionName
     * @param parserObject
     */
    public static void registerActionParser(String actionName, BeanDefinitionParser parserObject) {
        parserCache.put(actionName, parserObject);
    }

    /**
     * Getter for parser.
     * @return
     */
    public static Map<String, BeanDefinitionParser> getRegisteredActionParser() {
        return parserCache;
    }

    /**
     * Resolve test action parser for given action name. If not already present in the local parser cache try to locate
     * the parser through resource lookup.
     * @param name
     * @return
     */
    public static BeanDefinitionParser getActionParser(String name) {
        if (!parserCache.containsKey(name)) {
            try {
                parserCache.put(name, TYPE_RESOLVER.resolve(name));
            } catch (Exception e) {
                log.warn(String.format("Unable to locate test action parser for '%s'", name), e);
            }
        }

        return parserCache.get(name);
    }
}
