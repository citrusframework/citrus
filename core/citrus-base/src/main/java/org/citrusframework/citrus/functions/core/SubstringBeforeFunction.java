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

package org.citrusframework.citrus.functions.core;

import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.exceptions.InvalidFunctionUsageException;
import org.citrusframework.citrus.functions.Function;

import java.util.List;

/**
 * Function implements substring before functionality.
 * 
 * @author Christoph Deppisch
 */
public class SubstringBeforeFunction implements Function {

    /**
     * @see org.citrusframework.citrus.functions.Function#execute(java.util.List, org.citrusframework.citrus.context.TestContext)
     * @throws InvalidFunctionUsageException
     */
    public String execute(List<String> parameterList, TestContext context) {
        if (parameterList == null || parameterList.size() < 2) {
            throw new InvalidFunctionUsageException("Function parameters not set correctly");
        }

        String resultString = parameterList.get(0);

        if (parameterList.size()>1) {
            String searchString = parameterList.get(1);
            resultString = resultString.substring(0, resultString.indexOf(searchString));
        }

        return resultString;
    }
}
