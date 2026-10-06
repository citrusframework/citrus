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

package org.citrusframework.validation.assertj;

import org.assertj.core.api.Condition;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.validation.assertj.AssertJ.satisfies;

public class DefaultMessageHeaderValidatorTest extends UnitTestSupport {

    private final DefaultMessageHeaderValidator validator = new DefaultMessageHeaderValidator();

    @Test
    public void shouldValidateHeadersWithAssertJ() {
        Message receivedMessage = new DefaultMessage("Hello World!")
                .setHeader("status", "OK-1")
                .setHeader("additional", "additional")
                .setHeader("count", "42");
        Message controlMessage = new DefaultMessage("Hello World!")
                .setHeader("status", new Condition<String>(s -> s.startsWith("OK"), "starts with OK"))
                .setHeader("count", satisfies(Integer.class, c -> assertThat(c).isBetween(40, 50)));

        validator.validateMessage(receivedMessage, controlMessage, context, new HeaderValidationContext());
    }

    @Test
    public void shouldFailWithAssertJDescription() {
        Message receivedMessage = new DefaultMessage("Hello World!")
                .setHeader("status", "OK-1")
                .setHeader("count", "3");
        Message controlMessage = new DefaultMessage("Hello World!")
                .setHeader("status", new Condition<String>(s -> s.startsWith("OK"), "starts with OK"))
                .setHeader("count", satisfies(Integer.class, c -> assertThat(c).isGreaterThan(5)));

        assertThatThrownBy(() -> validator.validateMessage(receivedMessage, controlMessage, context, new HeaderValidationContext()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("'count'")
                .hasMessageContaining("to be greater than");
    }
}
