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

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.Message;
import org.citrusframework.validation.context.DefaultValidationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DefaultEmptyMessageValidatorTest {

    @Mock
    private Message received;
    @Mock
    private Message control;

    private final DefaultEmptyMessageValidator validator = new DefaultEmptyMessageValidator();

    @Test
    public void shouldValidateEmptyMessage() {
        when(received.getPayload(String.class)).thenReturn("");
        when(control.getPayload()).thenReturn("");
        when(control.getPayload(String.class)).thenReturn("");

        validator.validateMessage(received, control, new TestContext(), new DefaultValidationContext());
    }

    @Test
    public void shouldSkipNullControlMessageMessage() {
        validator.validateMessage(received, control, new TestContext(), new DefaultValidationContext());
    }

    @Test
    public void shouldValidateNonEmptyMessage() {
        when(received.getPayload(String.class)).thenReturn("Hello");
        when(control.getPayload()).thenReturn("");
        when(control.getPayload(String.class)).thenReturn("");

        ValidationException thrown = assertThrows(ValidationException.class, () -> {
            validator.validateMessage(received, control, new TestContext(), new DefaultValidationContext());
        });
        assertThat(thrown).hasMessage("Validation failed - received message content is not empty!");
    }

    @Test
    public void shouldValidateInvalidControlMessage() {
        when(control.getPayload()).thenReturn("Hello");
        when(control.getPayload(String.class)).thenReturn("Hello");

        ValidationException thrown = assertThrows(ValidationException.class, () -> {
            validator.validateMessage(received, control, new TestContext(), new DefaultValidationContext());
        });
        assertThat(thrown).hasMessage("Empty message validation failed - control message is not empty!");
    }
}
