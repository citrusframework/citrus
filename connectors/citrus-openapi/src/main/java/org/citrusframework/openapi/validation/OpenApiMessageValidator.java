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

package org.citrusframework.openapi.validation;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;
import org.citrusframework.openapi.OpenApiRepository;
import org.citrusframework.openapi.validation.schema.OpenApiSchemaValidation;
import org.citrusframework.validation.AbstractMessageValidator;

import static org.citrusframework.openapi.OpenApiMessageHeaders.OAS_UNIQUE_OPERATION_ID;

public class OpenApiMessageValidator extends
    AbstractMessageValidator<OpenApiMessageValidationContext>  {

    private final OpenApiSchemaValidation schemaValidator;

    public OpenApiMessageValidator() {
        this(new OpenApiSchemaValidation());
    }

    public OpenApiMessageValidator(OpenApiSchemaValidation schemaValidator) {
        this.schemaValidator = schemaValidator;
    }

    @Override
    protected Class<OpenApiMessageValidationContext> getRequiredValidationContextType() {
        return OpenApiMessageValidationContext.class;
    }

    @Override
    public void validateMessage(Message receivedMessage,
        Message controlMessage,
        TestContext context,
        OpenApiMessageValidationContext validationContext) {

        logger.debug("Start OpenAPI message validation ...");

        // No control message validation - we rely on specific message validators (JSON) for content validation,
        // only OpenAPI schema validation is performed.
        if (validationContext.isSchemaValidationEnabled()) {
            schemaValidator.validate(receivedMessage, context, validationContext);
        }

        logger.debug("OpenAPI message validation successful: All values OK");
    }

    /**
     * When the correct {@link OpenApiRepository} is installed, schema validation can be performed
     * if the operationId is explicitly specified, as set by an OpenApiClient.
     *
     * @param messageType The type of message to be validated.
     * @param message     The message content for validation.
     * @return Validation result or status.
     */
    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return message != null && message.getHeader(OAS_UNIQUE_OPERATION_ID) != null;
    }

}
