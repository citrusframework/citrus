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

package com.consol.citrus.config.xml;

/**
 * Parser constants used in Citrus configuration and test action 
 * bean definition parsers.
 * 
 * @author Christoph Deppisch
 */
public class JmsParserConstants {
    
    static final String REPLY_DESTINATION_ATTRIBUTE = "reply-destination";
    
    static final String REPLY_DESTINATION_PROPERTY = "replyDestination";
    
    static final String REPLY_DESTINATION_NAME_ATTRIBUTE = "reply-destination-name";
    
    static final String REPLY_DESTINATION_NAME_PROPERTY = "replyDestinationName";
    
    static final String REPLY_HANDLER_ATTRIBUTE = "reply-handler";
    
    static final String REPLY_HANDLER_PROPERTY = "replyMessageHandler";
    
    static final String REPLY_TIMEOUT_ATTRIBUTE = "reply-timeout";
    
    static final String REPLY_TIMEOUT_PROPERTY = "replyTimeout";
    
    static final String CONNECTION_FACTORY_ATTRIBUTE = "connection-factory";

    static final String CONNECTION_FACTORY_PROPERTY = "connectionFactory";
    
    static final String DESTINATION_ATTRIBUTE = "destination";

    static final String DESTINATION_PROPERTY = "destination";

    static final String DESTINATION_NAME_ATTRIBUTE = "destination-name";

    static final String DESTINATION_NAME_PROPERTY = "destinationName";
    
    static final String JMS_TEMPLATE_ATTRIBUTE = "jms-template";

    static final String JMS_TEMPLATE_PROPERTY = "jmsTemplate";
    
    static final String DESTINATION_HOLDER_ATTRIBUTE = "reply-destination-holder";

    static final String DESTINATION_HOLDER_PROPERTY = "replyDestinationHolder";

    public static final String RECEIVE_TIMEOUT_ATTRIBUTE = "receive-timeout";

    public static final String RECEIVE_TIMEOUT_PROPERTY = "receiveTimeout";

    public static final String REPLY_CORRELATOR_ATTRIBUTE = "reply-message-correlator";

    public static final String REPLY_CORRELATOR_PROPERTY = "correlator";

    public static final String PUB_SUB_DOMAIN_ATTRIBUTE = "pub-sub-domain";

    public static final String PUB_SUB_DOMAIN_PROPERTY = "pubSubDomain";
    
}
