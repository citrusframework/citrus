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

package org.citrusframework.validation;

import org.citrusframework.validation.context.DefaultMessageValidationContext;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.json.JsonMessageValidationContext;
import org.citrusframework.validation.openapi.OpenApiMessageValidationContextBuilder;
import org.citrusframework.validation.script.DefaultScriptValidationContext;
import org.citrusframework.validation.ws.SoapFaultValidationContext;
import org.citrusframework.validation.ws.SoapMessageValidationContextBuilder;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.citrusframework.validation.yaml.YamlMessageValidationContext;

/**
 * Interface combines default implementations with
 * domain specific language methods for all validations available in Citrus.
 */
public interface ValidationSupport extends Validations, ValidationContextLookupSupport {

    @Override
    default HeaderValidationContext.Builder headers() {
        return new HeaderValidationContext.Builder();
    }

    @Override
    default DefaultMessageValidationContext.Builder message() {
        return new DefaultMessageValidationContext.Builder();
    }

    @Override
    default PathExpressionValidationContext.Builder path() {
        return new PathExpressionValidationContext.Builder();
    }

    @Override
    default JsonMessageValidationContext.Builder json() {
        return new JsonMessageValidationContext.Builder();
    }

    @Override
    default DefaultScriptValidationContext.Builder script() {
        return new DefaultScriptValidationContext.Builder();
    }

    @Override
    default OpenApiMessageValidationContextBuilder<?, ?> openApi() {
        return lookup("openapi");
    }

    @Override
    default SoapMessageValidationContextBuilder soap() {
        return new SoapMessageValidationContextBuilder() {
            @Override
            public SoapFaultValidationContext.Builder fault() {
                return new SoapFaultValidationContext.Builder();
            }
        };
    }

    @Override
    default XmlMessageValidationContext.Builder xml() {
        return new XmlMessageValidationContext.Builder();
    }

    @Override
    default YamlMessageValidationContext.Builder yaml() {
        return new YamlMessageValidationContext.Builder();
    }
}
