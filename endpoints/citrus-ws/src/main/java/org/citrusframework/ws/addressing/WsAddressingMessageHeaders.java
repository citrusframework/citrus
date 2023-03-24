/*
 * Copyright 2006-2017 the original author or authors.
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

package org.citrusframework.ws.addressing;

import org.citrusframework.ws.message.SoapMessageHeaders;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public abstract class WsAddressingMessageHeaders {

    /**
     * Prevent instantiation.
     */
    private WsAddressingMessageHeaders() {
    }
    
    /** Citrus ws addressing header prefix */
    public static final String WS_ADDRESSING_PREFIX = SoapMessageHeaders.SOAP_PREFIX + "ws_addressing_";

    /** WsAddressing message id header name */
    public static final String MESSAGE_ID = WS_ADDRESSING_PREFIX + "messageId";

    /** WsAddressing from header name */
    public static final String FROM = WS_ADDRESSING_PREFIX + "from";

    /** WsAddressing to header name */
    public static final String TO = WS_ADDRESSING_PREFIX + "to";

    /** WsAddressing replyTo header name */
    public static final String REPLY_TO = WS_ADDRESSING_PREFIX + "replyTo";

    /** WsAddressing faultTo header name */
    public static final String FAULT_TO = WS_ADDRESSING_PREFIX + "faultTo";

    /** WsAddressing action header name */
    public static final String ACTION = WS_ADDRESSING_PREFIX + "action";

}
