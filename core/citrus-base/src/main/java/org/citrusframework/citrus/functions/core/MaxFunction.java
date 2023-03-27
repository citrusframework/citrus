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
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Function returns the maximum numeric value in a set of numeric arguments.
 * 
 * @author Christoph Deppisch
 */
public class MaxFunction implements Function {

    /**
     * @see org.citrusframework.citrus.functions.Function#execute(java.util.List, org.citrusframework.citrus.context.TestContext)
     * @throws InvalidFunctionUsageException
     */
    public String execute(List<String> parameterList, TestContext context) {
        if (CollectionUtils.isEmpty(parameterList)) {
            throw new InvalidFunctionUsageException("Function parameters must not be empty");
        }

        double result = 0.0;

        for (int i = 0; i < parameterList.size(); i++) {
            String token = (String) parameterList.get(i);
            if (i==0 || Double.valueOf(token) > result) {
                result = Double.valueOf(token);
            }
        }

        return Double.valueOf(result).toString();
    }

}
