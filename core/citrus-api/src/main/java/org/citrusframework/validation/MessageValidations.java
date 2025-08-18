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

import org.citrusframework.validation.context.MessageValidationContextBuilder;
import org.citrusframework.validation.json.JsonMessageValidationContextBuilder;
import org.citrusframework.validation.openapi.OpenApiMessageValidationContextBuilder;
import org.citrusframework.validation.ws.SoapMessageValidationContextBuilder;
import org.citrusframework.validation.xml.XmlMessageValidationContextBuilder;
import org.citrusframework.validation.yaml.YamlMessageValidationContextBuilder;

public interface MessageValidations extends
        JsonMessageValidationContextBuilder.Factory,
        MessageValidationContextBuilder.Factory,
        SoapMessageValidationContextBuilder.Factory,
        OpenApiMessageValidationContextBuilder.Factory,
        XmlMessageValidationContextBuilder.Factory,
        YamlMessageValidationContextBuilder.Factory {
}
