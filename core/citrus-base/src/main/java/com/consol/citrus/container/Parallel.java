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

package com.consol.citrus.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.consol.citrus.AbstractTestContainerBuilder;
import com.consol.citrus.TestAction;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ParallelContainerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test action will execute nested actions in parallel. Each action is executed in a
 * separate thread. Container joins all threads and waiting for them to end successfully.
 *
 * @author Christoph Deppisch
 */
public class Parallel extends AbstractActionContainer {

    /** Store created threads in stack */
    private Stack<Thread> threads = new Stack<>();

    /** Collect exceptions in list */
    private List<CitrusRuntimeException> exceptions = new ArrayList<>();

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(Parallel.class);

    /**
     * Default constructor.
     */
    public Parallel(Builder builder) {
        super("parallel", builder);
    }

    @Override
    public void doExecute(TestContext context) {
        for (TestActionBuilder<?> actionBuilder : actions) {
            final TestAction action = actionBuilder.build();
            Thread t = new Thread(new ActionRunner(action, context) {
                @Override
                public void exceptionCallback(CitrusRuntimeException e) {
                    if (exceptions.isEmpty()) {
                        setActiveAction(action);
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

        if (!exceptions.isEmpty()) {
            if (exceptions.size() == 1) {
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
        private final TestAction action;

        /** Test context */
        private final TestContext context;

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
            } catch (CitrusRuntimeException e) {
                log.error("Parallel test action raised error", e);
                exceptionCallback(e);
            } catch (Exception | AssertionError e) {
                log.error("Parallel test action raised error", e);
                exceptionCallback(new CitrusRuntimeException(e));
            }
        }

        /**
         * Callback for exception tracking.
         * @param exception
         */
        public abstract void exceptionCallback(CitrusRuntimeException exception);
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractTestContainerBuilder<Parallel, Builder> {

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder parallel() {
            return new Builder();
        }

        @Override
        public Parallel doBuild() {
            return new Parallel(this);
        }
    }
}
