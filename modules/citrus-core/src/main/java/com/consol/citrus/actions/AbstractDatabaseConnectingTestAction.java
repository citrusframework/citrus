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

package com.consol.citrus.actions;

import javax.sql.DataSource;

import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.consol.citrus.TestAction;
import com.consol.citrus.context.TestContext;

/**
 * Abstract base class for database connection test actions. Extends {@link JdbcDaoSupport} providing
 * access to a {@link DataSource}.
 * 
 * @author Christoph Deppisch
 */
public abstract class AbstractDatabaseConnectingTestAction extends JdbcDaoSupport implements TestAction {
    /** Text describing the test action */
    private String description;

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
