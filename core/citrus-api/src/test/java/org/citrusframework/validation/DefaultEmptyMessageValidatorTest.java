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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

public class DefaultEmptyMessageValidatorTest {

    @Mock
    private Message received;
    @Mock
    private Message control;

    DefaultEmptyMessageValidator validator = new DefaultEmptyMessageValidator();

    @BeforeMethod
    public void setupMocks() {
        MockitoAnnotations.openMocks(this);
    }

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

    @Test(expectedExceptions = ValidationException.class,
          expectedExceptionsMessageRegExp = "Validation failed - received message content is not empty!")
    public void shouldValidateNonEmptyMessage() {
        when(received.getPayload(String.class)).thenReturn("Hello");
        when(control.getPayload()).thenReturn("");
        when(control.getPayload(String.class)).thenReturn("");

        validator.validateMessage(received, control, new TestContext(), new DefaultValidationContext());
    }

    @Test(expectedExceptions = ValidationException.class,
            expectedExceptionsMessageRegExp = "Empty message validation failed - control message is not empty!")
    public void shouldValidateInvalidControlMessage() {
        when(received.getPayload(String.class)).thenReturn("");
        when(control.getPayload()).thenReturn("Hello");
        when(control.getPayload(String.class)).thenReturn("Hello");

        validator.validateMessage(received, control, new TestContext(), new DefaultValidationContext());
    }
}
