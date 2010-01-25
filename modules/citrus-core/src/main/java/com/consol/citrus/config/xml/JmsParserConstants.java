/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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
