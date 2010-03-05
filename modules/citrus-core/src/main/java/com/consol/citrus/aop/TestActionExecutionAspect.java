/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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

    @Pointcut("(within(com.consol.citrus.actions.*) || within(com.consol.citrus.group.*) || within(com.consol.citrus.container.*)) && execution(* com.consol.citrus.TestAction.execute(com.consol.citrus.context.TestContext))")
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
