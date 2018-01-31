/*
 * Copyright 2006-2018 the original author or authors.
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

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.AbstractAsyncTestAction;
import com.consol.citrus.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class Async extends AbstractActionContainer {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(Async.class);

    private List<TestAction> errorActions = new ArrayList<>();
    private List<TestAction> successActions = new ArrayList<>();

    public Async() {
        setName("async");
    }

    @Override
    public void doExecute(TestContext context) {
        log.debug("Async container forking action execution ...");

        AbstractAsyncTestAction asyncTestAction = new AbstractAsyncTestAction() {
            @Override
            public void doExecuteAsync(TestContext context) {
                for (TestAction action : actions) {
                    setActiveAction(action);
                    action.execute(context);
                }
            }

            @Override
            public void onError(TestContext context, Throwable error) {
                log.info("Apply error actions after async container ...");
                for (TestAction action : errorActions) {
                    action.execute(context);
                }
            }

            @Override
            public void onSuccess(TestContext context) {
                log.info("Apply success actions after async container ...");
                for (TestAction action : successActions) {
                    action.execute(context);
                }
            }
        };

        setActiveAction(asyncTestAction);
        asyncTestAction.execute(context);
    }

    /**
     * Adds a error action.
     * @param action
     * @return
     */
    public Async addErrorAction(TestAction action) {
        this.errorActions.add(action);
        return this;
    }

    /**
     * Adds a success action.
     * @param action
     * @return
     */
    public Async addSuccessAction(TestAction action) {
        this.successActions.add(action);
        return this;
    }

    /**
     * Adds one to many error actions.
     * @param actions
     * @return
     */
    public Async addErrorActions(TestAction ... actions) {
        this.errorActions.addAll(Arrays.asList(actions));
        return this;
    }

    /**
     * Adds one to many success actions.
     * @param actions
     * @return
     */
    public Async addSuccessActions(TestAction ... actions) {
        this.successActions.addAll(Arrays.asList(actions));
        return this;
    }

    /**
     * Sets the successActions.
     *
     * @param successActions
     */
    public void setSuccessActions(List<TestAction> successActions) {
        this.successActions = successActions;
    }

    /**
     * Gets the successActions.
     *
     * @return
     */
    public List<TestAction> getSuccessActions() {
        return successActions;
    }

    /**
     * Sets the errorActions.
     *
     * @param errorActions
     */
    public void setErrorActions(List<TestAction> errorActions) {
        this.errorActions = errorActions;
    }

    /**
     * Gets the errorActions.
     *
     * @return
     */
    public List<TestAction> getErrorActions() {
        return errorActions;
    }
}
