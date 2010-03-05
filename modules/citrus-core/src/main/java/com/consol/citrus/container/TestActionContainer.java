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

import java.util.List;

import com.consol.citrus.TestAction;

/**
 * Container interface describing all test action containers that hold several embedded test actions
 * to execute.
 * 
 * @author Christoph Deppisch
 */
public interface TestActionContainer {

    /**
     * Sets the embedded test actions to execute within this container.
     * @param actions
     */
    public void setActions(List<TestAction> actions);
    
    /**
     * Get the number of embedded actions in this container.
     * @return
     */
    public long getActionCount();
}
