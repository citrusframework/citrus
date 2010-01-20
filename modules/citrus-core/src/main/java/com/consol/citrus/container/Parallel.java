/*
 * Copyright 2006-2010 ConSol* Software GmbH.
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

    private List<TestAction> actions = new ArrayList<TestAction>();

    private Stack<Thread> threads = new Stack<Thread>();

    private Stack<Exception> exceptions = new Stack<Exception>();
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(Parallel.class);

    @Override
    public void execute(TestContext context) {
        log.info("Executing action parallel - containing " + actions.size() + " actions");

        for (TestAction action : actions) {
            Thread t = new Thread(new ActionRunner(action, context) {
                @Override
                public void exceptionCallback(Exception e) {
                    exceptions.push(e);
                }
            });

            threads.push(t);
            t.start();
        }

        while (!threads.isEmpty()) {
            try {
                threads.pop().join();
            } catch (InterruptedException e) {
                log.error("Unable to join thread", e);
            }
        }
        
        if(!exceptions.isEmpty()) {
            if(exceptions.size() == 1) {
                Exception exception = exceptions.pop();
                throw new  CitrusRuntimeException("Parallel container failed with exception: " + exception, exception);
            } else {
                throw new  CitrusRuntimeException("Parallel container failed! Caused by several errors in embedded test actions");
            }
        }
    }

    private abstract static class ActionRunner implements Runnable {
        private TestAction action;
        
        private TestContext context;
        
        public ActionRunner(TestAction action, TestContext context) {
            this.action = action;
            this.context = context;
        }

        public void run() {
            try {
                action.execute(context);
            } catch (Exception e) {
                log.error("Parallel test action raised error", e);
                exceptionCallback(e);
            }
        }
        
        public abstract void exceptionCallback(Exception e);
    }

    /**
     * @param actions the actions to set
     */
    public void setActions(List<TestAction> actions) {
        this.actions = actions;
    }
}