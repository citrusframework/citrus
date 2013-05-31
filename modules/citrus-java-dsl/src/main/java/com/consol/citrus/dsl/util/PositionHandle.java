/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.dsl.util;

import java.util.List;

import com.consol.citrus.TestAction;

/**
 * Handle saves position of last added test action in list of test actions. Later on someone can exchange the specific 
 * test action with a newly added action using this handle.
 * 
 * @author Christoph Deppisch
 */
public class PositionHandle {

    /** Last added test action position */
    private int position;
    
    /** List of test actions to handle */
    private List<TestAction> actions;

    /**
     * Default constructor using fields.
     * @param actions
     */
    public PositionHandle(List<TestAction> actions) {
        super();
        this.position = actions.size() - 1;
        this.actions = actions;
    }
    
    /**
     * Exchanges the test action on this handles saved position index with
     * given test action instance. The old test action is not used anymore.
     * @param action
     */
    public void switchTestAction(TestAction action) {
        actions.add(position, action);
        actions.remove(position + 1);
    }
}
