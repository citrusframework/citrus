/*
 * Copyright the original author or authors.
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

import static org.citrusframework.util.StringUtils.hasText;

import com.mifmif.common.regex.Generex;
import java.util.List;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.Function;

/**
 * The RandomPatternFunction generates a random string based on a provided regular expression pattern.
 * It uses the Generex library to generate the random string.
 * <p>
 * Note: The Generex library has limitations in its ability to generate all possible expressions
 * from a given regular expression. It may not support certain complex regex features or produce all
 * possible variations.
 */
public class RandomPatternFunction implements Function {


    public String execute(List<String> parameterList, TestContext context) {

        if (parameterList == null) {
            throw new InvalidFunctionUsageException("Function parameters must not be null.");
        }

        String pattern = parameterList.get(0);

        if (!hasText(pattern)) {
            throw new InvalidFunctionUsageException("Pattern must not be empty.");
        }

        if (!Generex.isValidPattern(pattern)) {
            throw new IllegalArgumentException(
                "Function called with a pattern, the algorithm is not able to create a string for.");
        }

        Generex generex = new Generex(pattern);
        return generex.random();
    }

}