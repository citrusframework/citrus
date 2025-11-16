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

import org.citrusframework.context.TestContext;
import org.citrusframework.functions.ParameterizedFunction;
import org.citrusframework.functions.parameter.StringParameter;

import static org.apache.commons.lang3.StringEscapeUtils.escapeXml;

/**
 * Escapes XML fragment with escaped characters for '<', '>'.
 */
public class EscapeXmlFunction implements ParameterizedFunction<StringParameter> {

    @Override
    public String execute(StringParameter param, TestContext context) {
        return escapeXml(param.getValue());
    }

    @Override
    public StringParameter getParameters() {
        return new StringParameter()
                .withAllowEmpty(false)
                .withUseRawValue(true);
    }
}
