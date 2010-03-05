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

import java.util.ArrayList;
import java.util.List;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.AbstractTestAction;

/**
 * Abstract base class for all containers holding several embedded test actions.
 * 
 * @author Christoph Deppisch
 */
public abstract class AbstractActionContainer extends AbstractTestAction implements TestActionContainer {
    /** List of nested actions */
    protected List<TestAction> actions = new ArrayList<TestAction>();

    /**
     * @see com.consol.citrus.container.TestActionContainer#setActions(java.util.List)
     */
    public void setActions(List<TestAction> actions) {
        this. actions = actions;
    }
    
    /**
     * @see com.consol.citrus.container.TestActionContainer#getActionCount()
     */
    public long getActionCount() {
        return actions.size();
    }
}
