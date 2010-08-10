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

package com.consol.citrus.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.consol.citrus.TestAction;
import com.consol.citrus.container.TestActionContainer;

/**
 * Aspect prints test action name and description before execution.
 * 
 * @author Christoph Deppisch
 */
@Aspect
public class TestActionExecutionAspect {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(TestActionExecutionAspect.class);

    @Pointcut("(within(com.consol.citrus.actions.*) || within(com.consol.citrus.container.*)) && execution(* com.consol.citrus.TestAction.execute(com.consol.citrus.context.TestContext))")
    public void inTestActionExecution() {}

    @Around("com.consol.citrus.aop.TestActionExecutionAspect.inTestActionExecution()")
    public Object doTestActionExecution(ProceedingJoinPoint pjp) throws Throwable {
        StringBuilder builder = new StringBuilder();
        
        builder.append("Executing: <");
        TestAction action = (TestAction)pjp.getThis();
        
        if (action.getName() != null) {
            builder.append(action.getName());
            
            if(log.isDebugEnabled() && StringUtils.hasText(action.getDescription())) {
                builder.append("(" + action.getDescription() + ")");
            }
        } else {
            builder.append(pjp.getTarget().getClass().getName());
        }

        builder.append(">");
        
        if(action instanceof TestActionContainer) {
            builder.append(" container with " + ((TestActionContainer)action).getActionCount() + " embedded actions");
        }
        
        log.info(builder.toString());
        
        return pjp.proceed();
    }
}
