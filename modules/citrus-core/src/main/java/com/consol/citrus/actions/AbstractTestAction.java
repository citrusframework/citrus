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

package com.consol.citrus.actions;

import com.consol.citrus.TestAction;
import com.consol.citrus.context.TestContext;

/**
 * Abstract base class for test acions. Class provides a default name and description.
 * @author Christoph Deppisch
 */
public abstract class AbstractTestAction implements TestAction {

    /** Describing the test action */
    protected String description;

    /** TestAction name injected as spring bean name */
    private String name = this.getClass().getSimpleName();

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.TestAction#execute(com.consol.citrus.context.TestContext)
     */
    public abstract void execute(TestContext context);

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.TestAction#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }
}
