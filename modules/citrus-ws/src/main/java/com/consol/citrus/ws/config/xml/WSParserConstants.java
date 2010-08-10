/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.ws.config.xml;

/**
 * Parser constants used in Citrus ws namespace.
 * 
 * @author Christoph Deppisch
 */
public class WSParserConstants {
    
    public static final String AUTOSTART_ATTRIBUTE = "auto-start";
    
    public static final String AUTOSTART_PROPERTY = "autoStart";
    
    public static final String PORT_ATTRIBUTE = "port";
    
    public static final String PORT_PROPERTY = "port";

    public static final String CONTEXT_CONFIC_LOCATION_ATTRIBUTE = "context-config-location";

    public static final String CONTEXT_CONFIC_LOCATION_PROPERTY = "contextConfigLocation";

    public static final String RESOURCE_BASE_ATTRIBUTE = "resource-base";

    public static final String RESOURCE_BASE_PROPERTY = "resourceBase";
    
    public static final String REQUEST_URL_ATTRIBUTE = "request-url";
    
    public static final String REQUEST_URL_PROPERTY = "defaultUri";
    
    public static final String REPLY_HANDLER_ATTRIBUTE = "reply-handler";
    
    public static final String REPLY_HANDLER_PROPERTY = "replyMessageHandler";

    public static final String MESSAGE_FACTORY_ATTRIBUTE = "message-factory";

    public static final String MESSAGE_FACTORY_PROPERTY = "messageFactory";
    
    public static final String MESSAGE_SENDER_ATTRIBUTE = "message-sender";

    public static final String MESSAGE_SENDER_PROPERTY = "messageSender";
    
    public static final String MESSAGE_SENDERS_ATTRIBUTE = "message-senders";

    public static final String MESSAGE_SENDERS_PROPERTY = "messageSenders";

    public static final String WS_TEMPLATE_ATTRIBUTE = "web-service-template";

	public static final String WS_TEMPLATE_PROPERTY = "webServiceTemplate";

    public static final String REPLY_CORRELATOR_ATTRIBUTE = "reply-message-correlator";

    public static final String REPLY_CORRELATOR_PROPERTY = "correlator";
    
    public static final String USE_ROOT_CONTEXT_ATTRIBUTE = "root-parent-context";
    
    public static final String USE_ROOT_CONTEXT_PROPERTY = "useRootContextAsParent";
    
}
