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
package org.citrusframework.grpc.actions;

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.validation.json.JsonTextMessageValidator;

/**
 * Shared support for gRPC receive actions.
 */
final class GrpcReceiveActionSupport {

    private GrpcReceiveActionSupport() {
    }

    /**
     * Custom validators configured on a receive action replace the automatic validator lookup in the base
     * action. This would silently skip the validation of the message payload and also lead to an incomplete
     * validation error, because the message validation context is never handled by any validator.
     * <p>
     * Adding the JSON validator explicitly keeps the payload validation working. The gRPC status validation is
     * performed by the receive action itself and is therefore not affected by this.
     */
    static void ensurePayloadValidation(ReceiveMessageAction.ReceiveMessageActionBuilder<?, ?, ?> builder, ReceiveMessageAction action) {
        if (!action.getValidators().isEmpty()) {
            builder.validators(new JsonTextMessageValidator());
        }
    }
}
