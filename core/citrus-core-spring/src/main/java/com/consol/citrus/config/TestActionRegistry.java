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

import com.consol.citrus.config.xml.*;
import org.springframework.beans.factory.xml.BeanDefinitionParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Registers bean definition parser for actions in test case.
 *
 * @author Christoph Deppisch
 * @since 2007
 */
public final class TestActionRegistry {
    /** Parser registry as map */
    private static Map<String, BeanDefinitionParser> parser = new HashMap<>();

    /**
     * Default constructor.
     */
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
        registerActionParser("purge-channel", new PurgeMessageChannelActionParser());
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
        parser.put(actionName, parserObject);
    }

    /**
     * Getter for parser.
     * @return
     */
    public static Map<String, BeanDefinitionParser> getRegisteredActionParser() {
        return parser;
    }
}
