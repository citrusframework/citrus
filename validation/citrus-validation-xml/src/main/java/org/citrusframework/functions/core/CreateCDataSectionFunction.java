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

package org.citrusframework.functions.core;

import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.Function;

/**
 * Adds XML CDATA section tags to parameter value. This is extremely useful when having
 * CDATA sections in message payload. Citrus test case itself also uses CDATA sections and
 * nested CDATA sections are not allowed. This function adds the CDATA section tags
 * at runtime.
 *
 * @author Christoph Deppisch
 */
public class CreateCDataSectionFunction implements Function {

    /** CDATA section tags */
    private static final String CDATA_START = "<![CDATA[";
    private static final String CDATA_END = "]]>";

    @Override
    public String execute(List<String> parameterList, TestContext context) {
        if (parameterList == null || parameterList.size() != 1) {
            throw new InvalidFunctionUsageException("Invalid function parameter usage - missing parameter value!");
        }

        return CDATA_START + parameterList.get(0) + CDATA_END;
    }

}
