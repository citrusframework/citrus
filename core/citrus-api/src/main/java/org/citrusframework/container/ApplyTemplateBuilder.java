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

package org.citrusframework.container;

import java.util.List;
import java.util.Map;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ActionBuilder;
import org.citrusframework.actions.ReferenceResolverAwareBuilder;

public interface ApplyTemplateBuilder<T extends TestAction, B extends TestActionBuilder<T>>
        extends ActionBuilder<T, B>, TestActionBuilder<T>, ReferenceResolverAwareBuilder<T, B> {

    ApplyTemplateBuilder<T, B> file(String filePath);

    ApplyTemplateBuilder<T, B> loader(TemplateLoader<T> loader);

    ApplyTemplateBuilder<T, B> templateName(String templateName);

    /**
     * Boolean flag marking the template variables should also affect
     * variables in test case.
     * @param globalContext the globalContext to set
     */
    ApplyTemplateBuilder<T, B> globalContext(boolean globalContext);

    /**
     * Set parameter before execution.
     * @param parameters the parameter to set
     */
    ApplyTemplateBuilder<T, B> parameters(Map<String, String> parameters);

    /**
     * Set parameter before execution.
     */
    ApplyTemplateBuilder<T, B> parameter(String name, String value);

    /**
     * Adds test actions to the template.
     */
    ApplyTemplateBuilder<T, B> actions(TestAction... actions);

    /**
     * Adds test actions to the template.
     */
    ApplyTemplateBuilder<T, B> actions(List<TestAction> actions);

    /**
     * Adds test action builders to the template.
     */
    ApplyTemplateBuilder<T, B> actions(TestActionBuilder<?>... actions);

    interface BuilderFactory {

        ApplyTemplateBuilder<?, ?> applyTemplate();

        default ApplyTemplateBuilder<?, ?> applyTemplate(String name) {
            return applyTemplate().templateName(name);
        }

    }
}
