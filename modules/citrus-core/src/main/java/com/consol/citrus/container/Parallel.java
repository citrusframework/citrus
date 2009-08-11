package com.consol.citrus.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

public class Parallel extends AbstractTestAction {

    List actions = new ArrayList();

    Stack threads = new Stack();

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(Parallel.class);

    @Override
    public void execute(TestContext context) throws CitrusRuntimeException {
        log.info("Executing action parallel - containing " + actions.size() + " actions");

        for (int i = 0; i < actions.size(); i++) {
            TestAction action = (TestAction)actions.get(i);

            Thread t = new Thread(new ActionThread(action, context));

            threads.push(t);
            t.start();
        }

        while (!threads.isEmpty()) {
            try {
                ((Thread)threads.pop()).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ActionThread implements Runnable {
        TestAction action;
        TestContext context;
        
        public ActionThread(TestAction action, TestContext context) {
            this.action = action;
            this.context = context;
        }

        public void run() {
            action.execute(context);
        }
    }

    /**
     * @param actions the actions to set
     */
    public void setActions(List actions) {
        this.actions = actions;
    }
}