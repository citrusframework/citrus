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

package com.consol.citrus.container;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.TestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ParallelContainerException;

/**
 * Test action will execute nested actions in parallel. Each action is executed in a
 * separate thread. Container joins all threads and waiting for them to end successfully.
 * 
 * @author Christoph Deppisch
 */
public class Parallel extends AbstractActionContainer {

    /** Store created threads in stack */
    private Stack<Thread> threads = new Stack<Thread>();

    /** Collect exceptions in list */
    private List<CitrusRuntimeException> exceptions = new ArrayList<CitrusRuntimeException>();
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(Parallel.class);

    /**
     * @see com.consol.citrus.actions.AbstractTestAction#execute(com.consol.citrus.context.TestContext)
     */
    @Override
    public void execute(TestContext context) {
        for (final TestAction action : actions) {
            Thread t = new Thread(new ActionRunner(action, context) {
                @Override
                public void exceptionCallback(CitrusRuntimeException e) {
                    if(exceptions.isEmpty()) {
                        setLastExecutedAction(action);
                    }
                    
                    exceptions.add(e);
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
                throw exceptions.get(0);
            } else {
                throw new ParallelContainerException(exceptions);
            }
        }
    }

    /**
     * Runnable wrapper for executing an action in separate Thread.
     */
    private abstract static class ActionRunner implements Runnable {
        /** Test action to execute */
        private TestAction action;
        
        /** Test context */
        private TestContext context;
        
        public ActionRunner(TestAction action, TestContext context) {
            this.action = action;
            this.context = context;
        }

        /**
         * Run the test action
         */
        public void run() {
            try {
                action.execute(context);
            } catch (Exception e) {
                log.error("Parallel test action raised error", e);
                if(e instanceof CitrusRuntimeException) {
                    exceptionCallback((CitrusRuntimeException)e);
                } else {
                    exceptionCallback(new CitrusRuntimeException(e));
                }
            }
        }
        
        /**
         * Callback for exception tracking.
         * @param exception
         */
        public abstract void exceptionCallback(CitrusRuntimeException e);
    }
}