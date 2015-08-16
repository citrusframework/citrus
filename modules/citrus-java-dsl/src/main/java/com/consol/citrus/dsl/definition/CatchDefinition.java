/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.dsl.definition;

import com.consol.citrus.container.Catch;

/**
 * @author Christoph Deppisch
 * @since 1.3
 * @deprecated since 2.3 in favor of using {@link com.consol.citrus.dsl.builder.CatchExceptionBuilder}
 */
public class CatchDefinition extends AbstractActionContainerDefinition<Catch> {

    /**
     * Constructor using action field.
     * @param action
     */
    public CatchDefinition(Catch action) {
        super(action);
    }

    /**
     * Default constructor.
     */
    public CatchDefinition() {
        super(new Catch());
    }

    /**
     * Catch exception type during execution.
     * @param exception
     * @return
     */
    public CatchDefinition exception(Class<? extends Throwable> exception) {
        action.setException(exception.getName());
        return this;
    }

    /**
     * Catch exception type during execution.
     * @param type
     */
    public CatchDefinition exception(String type) {
        action.setException(type);
        return this;
    }
}
