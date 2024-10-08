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

package org.citrusframework.functions;

import java.util.Arrays;
import java.util.Collections;

import org.citrusframework.context.TestContext;
import org.citrusframework.functions.core.CreateCDataSectionFunction;
import org.citrusframework.functions.core.EscapeXmlFunction;
import org.citrusframework.functions.core.XpathFunction;

public final class XmlFunctions {

    /**
     * Prevent instantiation.
     */
    private XmlFunctions() {
    }

    /**
     * Runs create CData section function with arguments.
     * @return
     */
    public static String createCDataSection(String content, TestContext context) {
        return new CreateCDataSectionFunction().execute(Collections.singletonList(content), context);
    }

    /**
     * Runs escape XML function with arguments.
     * @return
     */
    public static String escapeXml(String content, TestContext context) {
        return new EscapeXmlFunction().execute(Collections.singletonList(content), context);
    }

    /**
     * Runs Xpath function with arguments.
     * @return
     */
    public static String xPath(String content, String expression, TestContext context) {
        return new XpathFunction().execute(Arrays.asList(content, expression), context);
    }
}
