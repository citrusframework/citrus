package org.citrusframework.citrus.validation;

import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.exceptions.ValidationException;
import org.citrusframework.citrus.message.Message;
import org.citrusframework.citrus.validation.context.DefaultValidationContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
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
