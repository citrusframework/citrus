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

package org.citrusframework.validation.script;

import java.nio.charset.Charset;

import org.citrusframework.script.ScriptTypes;
import org.citrusframework.spi.Resource;
import org.citrusframework.validation.context.ValidationContext;

/**
 * Fluent builder
 *
 * @param <T> context type
 * @param <B> builder reference to self
 */
public interface ScriptValidationContextBuilder<T extends ScriptValidationContext, B extends ScriptValidationContextBuilder<T, B>>
        extends ValidationContext.Builder<T, B> {

    /**
     * Adds script validation.
     */
    B script(String validationScript);

    /**
     * Reads validation script file resource and sets content as validation script.
     */
    B script(Resource scriptResource);

    /**
     * Reads validation script file resource and sets content as validation script.
     */
    B script(Resource scriptResource, Charset charset);

    /**
     * Adds script validation file resource.
     */
    B scriptResource(String fileResourcePath);

    /**
     * Adds charset of script validation file resource.
     */
    B scriptResourceCharset(String charsetName);

    /**
     * Adds custom validation script type.
     */
    B scriptType(String type);

    interface Factory {

        /**
         * Fluent API action building entry method used in Java DSL.
         */
        ScriptValidationContextBuilder<?, ?> script();

        default ScriptValidationContextBuilder<?, ?> groovy() {
            return script().scriptType(ScriptTypes.GROOVY);
        }
    }
}
