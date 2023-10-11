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
import org.citrusframework.util.StringUtils;

/**
 * Function implements simple substring functionality.
 *
 * Function requires at least a target string and a beginIndex as function parameters. A
 * optional endIndex may be given as function parameter, too. The parameter usage looks
 * like this: substring(targetString, beginIndex, [endIndex]).
 *
 * @author Christoph Deppisch
 */
public class SubstringFunction implements Function {

    /**
     * @see org.citrusframework.functions.Function#execute(java.util.List, org.citrusframework.context.TestContext)
     * @throws InvalidFunctionUsageException
     */
    public String execute(List<String> parameterList, TestContext context) {
        if (parameterList == null || parameterList.size() < 2) {
            throw new InvalidFunctionUsageException("Insufficient function parameters - parameter usage: (targetString, beginIndex, [endIndex])");
        }

        String targetString = parameterList.get(0);

        String beginIndex = parameterList.get(1);
        String endIndex = null;

        if (!StringUtils.hasText(beginIndex)) {
            throw new InvalidFunctionUsageException("Invalid beginIndex - please check function parameters");
        }

        if (parameterList.size() > 2) {
            endIndex = parameterList.get(2);
        }

        if (StringUtils.hasText(endIndex)) {
            targetString = targetString.substring(Integer.valueOf(beginIndex), Integer.valueOf(endIndex));
        } else {
            targetString = targetString.substring(Integer.valueOf(beginIndex));
        }

        return targetString;
    }

}
