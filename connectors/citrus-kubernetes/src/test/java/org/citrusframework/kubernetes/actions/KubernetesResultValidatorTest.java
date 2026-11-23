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

package org.citrusframework.kubernetes.actions;

import java.util.Collections;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.ValidationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KubernetesResultValidatorTest {

    private final TestContext context = TestContextFactory.newInstance().getObject();

    @Test
    void shouldReportValidationConfiguration() {
        KubernetesResultValidator validator = new KubernetesResultValidator(null, Collections.emptyMap(), null, null);

        Assertions.assertFalse(validator.hasValidation());
    }

    @Test
    void shouldValidateCommandResult() {
        MessageValidator<? extends ValidationContext> jsonValidator = validator("json");
        MessageValidator<? extends ValidationContext> jsonPathValidator = validator("json-path");
        KubernetesResultValidator validator = new KubernetesResultValidator(
                "{\"result\":{\"success\":true}}",
                Map.of("$.result.success", "true"),
                jsonValidator,
                jsonPathValidator);

        Assertions.assertTrue(validator.hasValidation());
        Assertions.assertDoesNotThrow(() -> validator.validate("{\"result\":{\"success\":true}}", context));
        Assertions.assertThrows(ValidationException.class,
                () -> validator.validate("{\"result\":{\"success\":false}}", context));
    }

    private static MessageValidator<? extends ValidationContext> validator(String name) {
        return MessageValidator.lookup(name).orElseThrow();
    }
}
