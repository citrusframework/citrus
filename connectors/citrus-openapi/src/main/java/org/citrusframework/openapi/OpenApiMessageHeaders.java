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

package org.citrusframework.openapi;

import org.citrusframework.message.MessageHeaders;

public class OpenApiMessageHeaders {

    public static final String OAS_PREFIX = MessageHeaders.PREFIX + "oas_";

    public static final String OAS_UNIQUE_OPERATION_ID = OAS_PREFIX + "unique_operation_id";

    public static final String OAS_SPECIFICATION_ID = OAS_PREFIX + "unique_specification_id";

    public static final String OAS_MESSAGE_TYPE = OAS_PREFIX + "message_type";

    public static final String RESPONSE_TYPE = OAS_PREFIX + "response";

    public static final String REQUEST_TYPE = OAS_PREFIX + "request";

    private OpenApiMessageHeaders() {
        // Static access only
    }
}
