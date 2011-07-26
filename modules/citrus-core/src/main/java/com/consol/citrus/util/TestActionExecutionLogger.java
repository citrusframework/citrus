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

package com.consol.citrus.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.consol.citrus.TestAction;
import com.consol.citrus.container.TestActionContainer;

/**
 * Logger prints test action anem and description to the logging console.
 * Usually done before execution in test case.
 * 
 * @author Christoph Deppisch
 */
public final class TestActionExecutionLogger {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(TestActionExecutionLogger.class);
    
    /**
     * Prevent instantiation.
     */
    private TestActionExecutionLogger() {
    }
    
    /**
     * Print test action information to the console.
     * 
     * @param action the current test action.
     */
    public static void logTestAction(TestAction action) {
        StringBuilder builder = new StringBuilder();
        
        builder.append("Executing: <");
        
        if (action.getName() != null) {
            builder.append(action.getName());
            
            if (log.isDebugEnabled() && StringUtils.hasText(action.getDescription())) {
                builder.append("(" + action.getDescription() + ")");
            }
        } else {
            builder.append(action.getClass().getName());
        }

        builder.append(">");
        
        if (action instanceof TestActionContainer) {
            builder.append(" container with " + ((TestActionContainer)action).getActionCount() + " embedded actions");
        }
        
        log.info(builder.toString());
    }
}
