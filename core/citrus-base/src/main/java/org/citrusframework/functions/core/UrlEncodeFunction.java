/*
 * Copyright 2006-2018 the original author or authors.
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.Function;

/**
 * Encodes a character sequence to URL encoded string using given charset.
 *
 * @author Christoph Deppisch
 */
public class UrlEncodeFunction implements Function {

    @Override
    public String execute(List<String> parameterList, TestContext context) {
        if (parameterList == null || parameterList.size() < 1) {
            throw new InvalidFunctionUsageException("Invalid function parameter usage! Missing parameters!");
        }

        String charset = "UTF-8";
        if (parameterList.size() > 1) {
            charset = parameterList.get(1);
        }

        try {
            return URLEncoder.encode(parameterList.get(0), charset);
        } catch (UnsupportedEncodingException e) {
            throw new CitrusRuntimeException("Unsupported character encoding", e);
        }
    }

}
