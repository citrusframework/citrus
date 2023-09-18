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

package org.citrusframework.functions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General function interface.
 *
 * @author Christoph Deppisch
 */
public interface Function {

    /** Logger */
    Logger logger = LoggerFactory.getLogger(Function.class);

    /** Function resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/function";

    Map<String, Function> functions = new HashMap<>();

    /**
     * Resolves all available functions from resource path lookup. Scans classpath for function meta information
     * and instantiates those functions.
     * @return
     */
    static Map<String, Function> lookup() {
        if (functions.isEmpty()) {
            functions.putAll(new ResourcePathTypeResolver().resolveAll(RESOURCE_PATH));

            if (logger.isDebugEnabled()) {
                functions.forEach((k, v) -> logger.debug(String.format("Found function '%s' as %s", k, v.getClass())));
            }
        }

        return functions;
    }

    /**
     * Method called on execution.
     *
     * @param parameterList list of function arguments.
     * @param context
     * @return function result as string.
     */
    String execute(List<String> parameterList, TestContext context);
}
