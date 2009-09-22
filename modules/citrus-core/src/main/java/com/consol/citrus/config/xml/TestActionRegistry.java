/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.config.xml;

import java.util.HashMap;
import java.util.Map;

/**
 * Registers BeanDefinitionParser for action in testcase.
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2007
 */
public class TestActionRegistry {
    private static Map parser = new HashMap();

    /**
     * Default constructor.
     */
    static {
        registerActionParser("send", new SendMessageActionParser());
        registerActionParser("receive", new ReceiveMessageActionParser());
        registerActionParser("sql", new SQLActionParser());
        registerActionParser("java", new JavaActionParser());
        registerActionParser("sleep", new SleepActionParser());
        registerActionParser("traceVariables", new TraceVariablesActionParser());
        registerActionParser("createVariables", new CreateVariablesActionParser());
        registerActionParser("traceTime", new StopTimeActionParser());
        registerActionParser("echo", new EchoActionParser());
        registerActionParser("expectTimeout", new ReceiveTimeoutActionParser());
        registerActionParser("purgeJmsQueues", new PurgeJmsQueuesActionParser());
        registerActionParser("action", new ActionParser());
        registerActionParser("template", new TemplateParser());
        registerActionParser("call-template", new CallTemplateParser());
        registerActionParser("sequential", new SequenceParser());
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
    }

    /**
     * Register method to add new action parser.
     * @param actionName
     * @param parserObject
     */
    public static void registerActionParser(String actionName, Object parserObject) {
        parser.put(actionName, parserObject);
    }

    /**
     * Getter for parser.
     * @return
     */
    public static Map getRegisteredActionParser() {
        return parser;
    }
}
