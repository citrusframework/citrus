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

import com.consol.citrus.TestAction;

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

    @Pointcut("within(com.consol.citrus.actions.*) && execution(* com.consol.citrus.TestAction.execute())")
    public void inTestActionExecution() {}

    @Around("com.consol.citrus.aop.TestActionExecutionAspect.inTestActionExecution()")
    public Object doTestActionExecution(ProceedingJoinPoint pjp) throws Throwable {

        if (((TestAction)pjp.getThis()).getName() != null) {
            log.info("Executing action [" + ((TestAction)pjp.getThis()).getName() + "]");
        } else {
            log.info("Executing action [" + pjp.getTarget().getClass().getName() + "]");
        }

        if(log.isDebugEnabled()) {
            log.debug("TestAction class is " + pjp.getTarget().getClass().getName());
        }

        if (((TestAction)pjp.getThis()).getDescription() != null) {
            log.info(((TestAction)pjp.getThis()).getDescription());
        }

        return pjp.proceed();
    }
}
