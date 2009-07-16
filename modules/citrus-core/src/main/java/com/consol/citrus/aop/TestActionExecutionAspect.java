package com.consol.citrus.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.TestAction;

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
