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

package org.citrusframework.yaml;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;
import org.citrusframework.variable.VariableExtractor;

public class NoopVariableExtractor implements VariableExtractor {
    @Override
    public void extractVariables(Message message, TestContext context) {
    }

    public static class Builder implements VariableExtractor.Builder<NoopVariableExtractor, Builder> {

        @Override
        public Builder expressions(Map<String, Object> expressions) {
            return this;
        }

        @Override
        public Builder expression(String expression, Object value) {
            return this;
        }

        @Override
        public NoopVariableExtractor build() {
            return new NoopVariableExtractor();
        }
    }
}
